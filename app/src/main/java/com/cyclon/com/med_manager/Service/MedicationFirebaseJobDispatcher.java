package com.cyclon.com.med_manager.Service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class MedicationFirebaseJobDispatcher extends JobService {

    AsyncTask backgroundTask;

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d("JOBSCHEDULER", "Main job service");
        backgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context = MedicationFirebaseJobDispatcher.this;
                NotificationUtilities.notifyUserForDrugIntake(context);
                Log.d("JOBSCHEDULER", "Main doInBackground");
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                Log.d("JOBSCHEDULER", "Main doInbackgorund finished");
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
