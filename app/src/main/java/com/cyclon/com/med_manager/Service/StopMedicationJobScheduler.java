package com.cyclon.com.med_manager.Service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cyclon.com.med_manager.Constants.DataConstants;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class StopMedicationJobScheduler extends JobService {
    AsyncTask backgroundTask;
    String job_tag;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        job_tag = jobParameters.getExtras().getString(DataConstants.MAIN_JOB_TAG);
        backgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context = StopMedicationJobScheduler.this;
                ScheduleJob.cancelJob(context, job_tag);
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
