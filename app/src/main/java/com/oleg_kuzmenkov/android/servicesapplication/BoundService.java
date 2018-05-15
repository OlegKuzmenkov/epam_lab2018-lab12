package com.oleg_kuzmenkov.android.servicesapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BoundService extends Service {

    private final String LOG_TAG = "Message";
    private final String URL = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSTkXbFPsRsqZPbJpnRBA9RyD4xFnv1rWeIqXFn6J43YBo2B0Za";
    private final String STATUS_DOWNLOADING = "Downloading the image";
    private final String FILENAME = "2.jpeg";
    private final int TASK_CODE = 2;

    private final IBinder myBinder = new MyLocalBinder();
    BindServiceCallbacks mBindServiceCallbacks;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "OnCreateBoundService");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return myBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "OnDestroyBoundService");
        super.onDestroy();
    }

    public void setBindServiceCallbacks(BindServiceCallbacks bindServiceCallbacks){
        mBindServiceCallbacks = bindServiceCallbacks;
    }

    public String downloadImage() {
        new DownloadImageTask(TASK_CODE,FILENAME,this,mBindServiceCallbacks).execute(URL);
        return STATUS_DOWNLOADING;
    }

    public class MyLocalBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }
    }
}