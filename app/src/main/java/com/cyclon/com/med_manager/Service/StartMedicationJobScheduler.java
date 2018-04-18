package com.cyclon.com.med_manager.Service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cyclon.com.med_manager.Constants.DataConstants;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class StartMedicationJobScheduler extends JobService {
    AsyncTask backgroundTask;
    String job_tag;
    int job_interval;
    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        job_tag = jobParameters.getExtras().getString(DataConstants.MAIN_JOB_TAG, null);
        job_interval = jobParameters.getExtras().getInt(DataConstants.MAIN_JOB_INTERVAL);

        backgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context = StartMedicationJobScheduler.this;
                ScheduleJob.scheduleMedicationreminder(context, job_interval, job_tag);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(jobParameters, false);
            }
        };
        backgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if(backgroundTask != null) backgroundTask.cancel(true);
        return true ;
    }
}
