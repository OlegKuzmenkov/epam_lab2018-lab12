package com.oleg_kuzmenkov.android.servicesapplication;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class JobSchedulerService extends JobService {

    private final String LOG_TAG = "Message";
    private final String SAVED_TEXT = "saved_text";
    private final String NAME_PREFERENCE = "preference";
    private final String URL = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR3IASy3QquZZ4numyxTxKFbD7pLB2FPJSLgk5kG6gyhTS1ZTqu";
    private final int TASK_CODE = 0;

    private JobScheduler mJobScheduler;
    private SharedPreferences mSharedPreferences;

    @Override
    public boolean onStartJob(JobParameters params) {
        int countJobs = getCountJobs();
        Log.d(LOG_TAG, "onStartJob "+countJobs);

        if(countJobs < 6){
            String filename = String.format("file_%d.jpeg",countJobs);
            new DownloadImageTask(TASK_CODE,filename,null,null).execute(URL);
        }
        else {
            mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            mJobScheduler.cancelAll();
            setNullCountJobs();
            Log.d(LOG_TAG, "Job cancelled");
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    /**
     * Get count of executed jobs from SharedPreferences
     */
    private int getCountJobs(){
        mSharedPreferences = getSharedPreferences(NAME_PREFERENCE, MODE_PRIVATE);
        int countJobs = mSharedPreferences.getInt(SAVED_TEXT,0);
        countJobs++;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(SAVED_TEXT, countJobs);
        editor.commit();

        return countJobs;
    }

    /**
     * Refreshing the SharedPreferences after cancellation of all jobs
     */
    private void setNullCountJobs(){
        mSharedPreferences = getSharedPreferences(NAME_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(SAVED_TEXT, 0);
        editor.commit();
    }

}
