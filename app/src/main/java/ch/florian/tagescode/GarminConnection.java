package ch.florian.tagescode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import java.util.List;

/** Optional, foreground companion connection through Garmin Connect. No notifications. */
final class GarminConnection {
    private final Activity activity;
    private final SharedPreferences preferences;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ConnectIQ sdk;
    private IQDevice selected;
    private boolean ready, starting, closed, chooseWhenReady, sending, pending;
    private boolean manualSend;
    private int session, attempt;
    private final Runnable timeout = () -> {
        if (sending) finish(false, "Übertragung nicht bestätigt. Uhr verbinden und erneut versuchen.");
    };

    GarminConnection(Activity activity) {
        this.activity = activity;
        preferences = activity.getSharedPreferences("garmin", Context.MODE_PRIVATE);
    }

    void sync() {
        if (closed || !preferences.contains("device")) return;
        if (!ready) { start(false); return; }
        if (selected == null) findSelected();
        if (selected != null) send(false);
    }

    void showMenu() {
        new AlertDialog.Builder(activity).setTitle("Garmin Tagescode")
            .setItems(new String[]{"Codes jetzt übertragen", "Uhr auswählen", "Verbindung deaktivieren"},
                (dialog, item) -> {
                    if (item == 2) {
                        preferences.edit().remove("device").apply();
                        selected = null;
                        stopSdk();
                        toast("Garmin-Verbindung deaktiviert. Bereits gespeicherte Codes bleiben auf der Uhr.");
                    } else if (item == 1 || !preferences.contains("device")) {
                        if (ready) chooseDevice(); else start(true);
                    } else {
                        manualSend = true;
                        if (ready) { findSelected(); send(true); } else start(false);
                    }
                }).setNegativeButton("Schliessen", null).show();
    }

    private void start(boolean choose) {
        chooseWhenReady |= choose;
        if (starting || closed) return;
        starting = true;
        int currentSession = ++session;
        try {
            sdk = ConnectIQ.getInstance(activity, ConnectIQ.IQConnectType.WIRELESS);
            sdk.initialize(activity, false, new ConnectIQ.ConnectIQListener() {
                public void onSdkReady() { ui(() -> {
                    if (currentSession != session) return;
                    ready = true; starting = false;
                    if (chooseWhenReady) { chooseWhenReady = false; chooseDevice(); }
                    else { findSelected(); send(manualSend); }
                }); }
                public void onInitializeError(ConnectIQ.IQSdkErrorStatus status) { ui(() -> {
                    if (currentSession != session) return;
                    starting = false; ready = false;
                    if (chooseWhenReady || manualSend) toast("Garmin Connect installieren bzw. öffnen und die Uhr dort verbinden.");
                    chooseWhenReady = false; manualSend = false;
                }); }
                public void onSdkShutDown() { ui(() -> {
                    if (currentSession == session) { ready = false; starting = false; }
                }); }
            });
        } catch (RuntimeException exception) {
            starting = false;
            if (chooseWhenReady || manualSend) toast("Garmin Connect ist momentan nicht verfügbar.");
            chooseWhenReady = false; manualSend = false;
        }
    }

    private void chooseDevice() {
        try {
            List<IQDevice> devices = sdk.getKnownDevices();
            if (devices == null || devices.isEmpty()) {
                toast("Zuerst die Uhr in Garmin Connect koppeln."); return;
            }
            String[] names = new String[devices.size()];
            for (int i = 0; i < devices.size(); i++) names[i] = devices.get(i).getFriendlyName();
            new AlertDialog.Builder(activity).setTitle("Uhr auswählen")
                .setItems(names, (dialog, index) -> {
                    handler.removeCallbacks(timeout);
                    attempt++; sending = false; pending = false;
                    preferences.edit().putLong("device", devices.get(index).getDeviceIdentifier()).apply();
                    findSelected(); send(true);
                }).setNegativeButton("Abbrechen", null).show();
        } catch (Exception exception) { toast("Uhren konnten nicht geladen werden. Garmin Connect öffnen."); }
    }

    private void findSelected() {
        try {
            long id = preferences.getLong("device", -1);
            List<IQDevice> devices = sdk.getKnownDevices();
            if (devices == null) return;
            for (IQDevice device : devices) if (device.getDeviceIdentifier() == id) {
                if (selected != null && selected.getDeviceIdentifier() == id) return;
                if (selected != null) sdk.unregisterForDeviceEvents(selected);
                selected = device;
                sdk.registerForDeviceEvents(device, (changed, status) -> ui(() -> {
                    if (status == IQDevice.IQDeviceStatus.CONNECTED) sync();
                }));
                return;
            }
            selected = null;
        } catch (Exception ignored) { selected = null; }
    }

    private void send(boolean manual) {
        manualSend |= manual;
        if (sending) { pending = true; return; }
        if (!ready || selected == null) {
            if (manualSend) toast("Uhr nicht verfügbar. Bitte Uhr auswählen.");
            manualSend = false; return;
        }
        try {
            if (sdk.getDeviceStatus(selected) != IQDevice.IQDeviceStatus.CONNECTED) {
                if (manualSend) toast("Uhr nicht verbunden. Garmin Connect öffnen und erneut versuchen.");
                manualSend = false; return;
            }
            sending = true;
            int currentAttempt = ++attempt;
            handler.postDelayed(timeout, 30000);
            IQDevice target = selected;
            sdk.getApplicationInfo(GarminCodePacket.APP_ID, target, new ConnectIQ.IQApplicationInfoListener() {
                public void onApplicationNotInstalled(String id) { ui(() -> {
                    if (currentAttempt == attempt && sending)
                        finish(false, "Tagescode muss zuerst als Garmin-App auf der Uhr installiert werden.");
                }); }
                public void onApplicationInfoReceived(IQApp app) { ui(() -> {
                    if (!sending || currentAttempt != attempt) return;
                    try {
                        sdk.sendMessage(target, app, CodeRepository.garminPacket(activity),
                            (device, sentApp, status) -> ui(() -> {
                                if (sending && currentAttempt == attempt) finish(
                                    status == ConnectIQ.IQMessageStatus.SUCCESS,
                                    "Übertragung fehlgeschlagen. Uhr verbinden und erneut versuchen.");
                            }));
                    } catch (Exception exception) { finish(false, "Garmin-Übertragung nicht möglich."); }
                }); }
            });
        } catch (Exception exception) { finish(false, "Garmin Connect ist momentan nicht verfügbar."); }
    }

    private void finish(boolean success, String error) {
        handler.removeCallbacks(timeout);
        sending = false;
        if (manualSend) toast(success ? "Codes übertragen. Tagescode auf der Uhr öffnen." : error);
        manualSend = false;
        boolean again = pending; pending = false;
        if (success && again) sync();
    }

    private void ui(Runnable action) { handler.post(() -> { if (!closed && !activity.isDestroyed()) action.run(); }); }
    private void toast(String text) { Toast.makeText(activity, text, Toast.LENGTH_LONG).show(); }
    private void stopSdk() {
        session++; attempt++;
        handler.removeCallbacks(timeout);
        sending = false; pending = false; ready = false; starting = false;
        if (sdk != null) try { sdk.unregisterAllForEvents(); sdk.shutdown(activity); } catch (Exception ignored) { }
    }
    void close() { closed = true; stopSdk(); handler.removeCallbacksAndMessages(null); }
}
