package ch.florian.tagescode;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** A bounded, silent daily import and watch transfer, persisted across reboots. */
public class GarminBackgroundJob extends JobService {
    private static final int ID = 2004;
    private static final long RETRY = 30 * 60 * 1000L;
    private static final long DAILY = 24 * 60 * 60 * 1000L;
    private static GarminBackgroundJob active;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> importTask;
    private JobParameters parameters;
    private ConnectIQ sdk;
    private boolean cloudSucceeded;
    private boolean transferConfirmed;
    private int generation;
    private final Runnable timeout = () -> finish(false);

    static void ensureScheduled(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler != null && scheduler.getPendingJob(ID) == null && active == null)
            schedule(context, 15 * 60 * 1000L);
    }

    private static void schedule(Context context, long delay) {
        if (!context.getSharedPreferences("garmin", MODE_PRIVATE).contains("device")) return;
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler != null) scheduler.schedule(new JobInfo.Builder(ID,
                new ComponentName(context, GarminBackgroundJob.class))
                .setPersisted(true).setMinimumLatency(delay)
                .setBackoffCriteria(RETRY, JobInfo.BACKOFF_POLICY_EXPONENTIAL).build());
    }

    static void interruptForForeground() {
        // ConnectIQ is a process singleton: release it before the Activity uses it.
        if (active != null) active.finish(false);
    }

    static void cancel(Context context) {
        if (active != null) active.finish(false);
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler != null) scheduler.cancel(ID);
    }

    @Override public boolean onStartJob(JobParameters params) {
        if (!getSharedPreferences("garmin", MODE_PRIVATE).contains("device")) return false;
        transferConfirmed = false;
        parameters = params;
        active = this;
        int token = ++generation;
        handler.postDelayed(timeout, 90000);
        // Let the existing foreground connection own Garmin's singleton SDK.
        if (GarminConnection.foregroundOwners > 0) {
            handler.post(() -> finish(false));
            return true;
        }
        importTask = executor.submit(() -> {
            boolean imported = false;
            try {
                CodeRepository.reload(this);
                imported = CodeRepository.importCloudCodes(this) >= 0;
                TagescodeWidget.updateAllWidgets(this);
            } finally {
                final boolean success = imported;
                dispatch(token, () -> { cloudSucceeded = success; connect(token); });
            }
        });
        return true;
    }

    private void connect(int token) {
        if (GarminConnection.foregroundOwners > 0) { finish(false); return; }
        try {
            sdk = ConnectIQ.getInstance(getApplicationContext(), ConnectIQ.IQConnectType.WIRELESS);
            sdk.initialize(getApplicationContext(), false, new ConnectIQ.ConnectIQListener() {
                public void onSdkReady() { dispatch(token, () -> transfer(token)); }
                public void onInitializeError(ConnectIQ.IQSdkErrorStatus error) {
                    dispatch(token, () -> finish(false));
                }
                public void onSdkShutDown() { dispatch(token, () -> finish(false)); }
            });
        } catch (RuntimeException error) { finish(false); }
    }

    private void transfer(int token) {
        try {
            long selected = getSharedPreferences("garmin", MODE_PRIVATE).getLong("device", -1);
            List<IQDevice> devices = sdk.getKnownDevices();
            if (devices != null) for (IQDevice device : devices) {
                if (device.getDeviceIdentifier() != selected) continue;
                if (sdk.getDeviceStatus(device) != IQDevice.IQDeviceStatus.CONNECTED) break;
                sdk.getApplicationInfo(GarminCodePacket.APP_ID, device,
                        new ConnectIQ.IQApplicationInfoListener() {
                    public void onApplicationNotInstalled(String id) { dispatch(token, () -> finish(false)); }
                    public void onApplicationInfoReceived(IQApp app) { dispatch(token, () -> {
                        try {
                            sdk.sendMessage(device, app, CodeRepository.garminPacket(GarminBackgroundJob.this),
                                (watch, sentApp, status) -> dispatch(token, () -> {
                                    boolean sent = status == ConnectIQ.IQMessageStatus.SUCCESS;
                                    transferConfirmed = sent;
                                    if (sent) GarminTransferStatus.record(GarminBackgroundJob.this, true, "Codes im Hintergrund an die Uhr gesendet.");
                                    if (sent) getSharedPreferences("garmin", MODE_PRIVATE).edit()
                                        .putLong("last_background_transfer", System.currentTimeMillis()).apply();
                                    finish(sent && cloudSucceeded);
                                }));
                        } catch (Exception error) { finish(false); }
                    }); }
                });
                return;
            }
        } catch (Exception ignored) { }
        finish(false);
    }

    private void dispatch(int token, Runnable action) {
        handler.post(() -> { if (parameters != null && generation == token) action.run(); });
    }

    private void finish(boolean success) {
        JobParameters done = parameters;
        if (done == null) return;
        if (!transferConfirmed && GarminConnection.foregroundOwners == 0)
            GarminTransferStatus.record(this, false, "Hintergrundübertragung nicht bestätigt. Uhr und Garmin Connect prüfen; erneuter Versuch folgt.");
        cleanup();
        jobFinished(done, false);
        schedule(this, success ? DAILY : RETRY);
    }

    private void cleanup() {
        parameters = null; generation++;
        if (active == this) active = null;
        handler.removeCallbacksAndMessages(null);
        if (importTask != null) importTask.cancel(true);
        if (sdk != null) {
            try { sdk.unregisterAllForEvents(); sdk.shutdown(getApplicationContext()); }
            catch (Exception ignored) { }
            sdk = null;
        }
    }

    @Override public boolean onStopJob(JobParameters params) { cleanup(); return true; }
    @Override public void onDestroy() { cleanup(); executor.shutdownNow(); super.onDestroy(); }
}
