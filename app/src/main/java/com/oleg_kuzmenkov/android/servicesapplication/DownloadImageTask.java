package com.oleg_kuzmenkov.android.servicesapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class DownloadImageTask extends AsyncTask<String, Void, Boolean> {
    private final String BROADCAST_ACTION = "download_image";
    private final String LOG_TAG = "Message";
    private final String FOLDER_NAME = "Downloads";

    private int mTaskCode;
    private Context mContext;
    private String mFileName;
    private BindServiceCallbacks mBindServiceCallbacks;

    public DownloadImageTask(int taskCode,String filename,Context context,BindServiceCallbacks bindServiceCallbacks ) {
        mTaskCode = taskCode;
        mFileName = filename;
        mContext = context;
        mBindServiceCallbacks = bindServiceCallbacks;
    }

    protected Boolean doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap bitmap;

        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }

        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + FOLDER_NAME);
        boolean doSave = true;
        if (!dir.exists()) {
            //create directory
            doSave = dir.mkdirs();
        }

        if (doSave) {
            // if don't save file
            if(saveFile(dir,mFileName,bitmap) == false){
                return false;
            }
        }
        else {
            Log.e(LOG_TAG,"Couldn't create target directory.");
            return false;
        }
        return true;
    }

    protected void onPostExecute(Boolean result) {
        if(result == true) {
            if (mTaskCode == 1) {
                Intent intent = new Intent(BROADCAST_ACTION);
                mContext.sendBroadcast(intent);
                //stop service
                Intent stopServiceIntent = new Intent(mContext, StartedService.class);
                mContext.stopService(stopServiceIntent);

            }
            if (mTaskCode == 2) {
                mBindServiceCallbacks.finishDownloadImage();
            }
        }
    }

    /**
     * Save bitmap into the file
     */
    private boolean saveFile(File dir, String fileName, Bitmap bitmap){
        File imageFile = new File(dir,fileName);
        FileOutputStream out;
        try {
            out = new FileOutputStream(imageFile);
            if(bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            out.close();
            return true;
        } catch (IOException e) {
            Log.e(LOG_TAG,e.getMessage());
            return false;
        }
    }
}
