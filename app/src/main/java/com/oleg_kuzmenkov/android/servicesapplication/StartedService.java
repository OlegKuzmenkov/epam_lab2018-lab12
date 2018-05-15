package com.oleg_kuzmenkov.android.servicesapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StartedService extends Service {

    private final String LOG_TAG = "Message";
    private final String URL = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRm2CM_Tr-pw7iPdUF7DvMShYIotS9XM3zlorKDUz1hQJKlO5yZLQ";
    private final String FILENAME = "1.jpeg";
    private final int TASK_CODE = 1;


    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "OnCreateStartedService");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new DownloadImageTask(TASK_CODE,FILENAME,this,null).execute(URL);
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "OnDestroyStartedService");
        super.onDestroy();
    }
}

