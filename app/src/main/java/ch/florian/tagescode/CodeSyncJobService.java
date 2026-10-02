package ch.florian.tagescode;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Cloud providers may be slow: keep their I/O out of broadcast receivers. */
public class CodeSyncJobService extends JobService {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> pending;

    static void enqueue(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler != null) {
            scheduler.schedule(new JobInfo.Builder(2003,
                    new ComponentName(context, CodeSyncJobService.class))
                    .setMinimumLatency(0)
                    .setBackoffCriteria(30 * 60 * 1000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                    .build());
        }
    }

    @Override public boolean onStartJob(JobParameters parameters) {
        pending = executor.submit(() -> {
            boolean retry = false;
            try {
                CodeRepository.reload(getApplicationContext());
                retry = CodeRepository.importCloudCodes(getApplicationContext()) < 0;
                TagescodeWidget.updateAllWidgets(getApplicationContext());
            } finally {
                if (!Thread.currentThread().isInterrupted()) jobFinished(parameters, retry);
            }
        });
        return true;
    }

    @Override public boolean onStopJob(JobParameters parameters) {
        if (pending != null) pending.cancel(true);
        return true;
    }

    @Override public void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
