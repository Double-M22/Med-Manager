package com.cyclon.com.med_manager.Service;

import android.content.Context;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

public class ScheduleJob {

    /**
     * Main Job scheduler that schedules job event for medication intake
     * It is set to recurring with the interval given by the user.
     *
     * However this does not work on my mobile phone. I have installed the latest version of google play service and still does not.
     * It works on the android studio emulator thats how i know it works. But it dosent work on my real android device.
     */

    public static void scheduleMedicationreminder(final Context context, int intervals, String jobSchedulerTag){
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job reminderJob = dispatcher.newJobBuilder()
                .setService(MedicationFirebaseJobDispatcher.class)
                .setTag(jobSchedulerTag)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(intervals, intervals+5))
                .setReplaceCurrent(true)
                .build();
        dispatcher.schedule(reminderJob);
    }

    /**
     *Cancels specific jobs scheduled according to the specified tag.
     * This is called when the end date of a certain medication is reached
     */
    public static void cancelJob(Context context, String jobSchedulerTag){
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        dispatcher.cancel(jobSchedulerTag);
    }

    /**
     * This cancels all job and is called when the user signs out.
     */
    public static void cancelAllJob(Context context){
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        dispatcher.cancelAll();
    }
}
