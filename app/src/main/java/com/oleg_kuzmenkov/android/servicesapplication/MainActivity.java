package com.oleg_kuzmenkov.android.servicesapplication;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;


public class MainActivity extends AppCompatActivity implements BindServiceCallbacks{
    private final String LOG_TAG = "Message";
    private final String FINISH_DOWNLOADING = "Image successfully downloaded";
    private final String STATUS_DOWNLOADING = "Downloading the image";
    private final String BROADCAST_ACTION = "download_image";

    private Button mStartStartedServiceButton;
    private Button mStartBoundServiceButton;
    private Button mStartJobSchedulerButton;
    private ImageView mStartedServiceImageView;
    private ImageView mBoundServiceImageView;
    private TextView mStartedServiceTextView;
    private TextView mBoundServiceTextView;

    private JobScheduler mJobScheduler;
    private BroadcastReceiver mBroadcastReceiver;
    private BoundService mBoundService;
    private boolean isBound = false;

    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BoundService.MyLocalBinder binder = (BoundService.MyLocalBinder) service;
            mBoundService = binder.getService();
            isBound = true;
            mBoundService.setBindServiceCallbacks(MainActivity.this);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartedServiceImageView = findViewById(R.id.started_service_image);
        mBoundServiceImageView = findViewById(R.id.bound_service_image);
        mStartedServiceTextView = findViewById(R.id.started_service_text);
        mBoundServiceTextView = findViewById(R.id.bound_service_text);

        mStartStartedServiceButton = findViewById(R.id.start_started_service_button);
        mStartStartedServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission() == true) {
                    mStartedServiceTextView.setText(STATUS_DOWNLOADING);
                    Intent intent = new Intent(getApplicationContext(), StartedService.class);
                    // start service
                    startService(intent);
                } else{
                    startRequestForPermission();
                }
            }
        });

        mStartBoundServiceButton = findViewById(R.id.start_bound_service_button);
        mStartBoundServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission() == true) {
                    downloadImage();
                } else{
                    startRequestForPermission();
                }
            }
        });

        mStartJobSchedulerButton = findViewById(R.id.start_job_scheduler_button);
        mStartJobSchedulerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission() == true) {
                    mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    ComponentName componentName = new ComponentName(MainActivity.this, JobSchedulerService.class);
                    JobInfo.Builder jobInfo = new JobInfo.Builder(101, componentName);
                    jobInfo.setPeriodic(5000);
                    //jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
                    jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                    mJobScheduler.schedule(jobInfo.build());
                } else{
                    startRequestForPermission();
                }
            }
        });

        // create BoundService
        Intent intent = new Intent(this, BoundService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

        // create and register BroadcastReceiver
        createBroadcastReceiver();
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(mBroadcastReceiver, intFilt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unbind Bound Service
        if (isBound) {
            mBoundService.setBindServiceCallbacks(null);
            unbindService(myConnection);
            isBound = false;
        }
        // unregister BroadcastReceiver
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0) && (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Call Permission Not Granted", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void finishDownloadImage(){
        Log.d(LOG_TAG, "FinishDownloadImage");
        File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Downloads/2.jpeg");
        mBoundServiceImageView.setImageBitmap(BitmapFactory.decodeFile(dir.getPath()));
        mBoundServiceTextView.setText(FINISH_DOWNLOADING);
    }

    /**
     * Check permission
     */
    private boolean checkPermission(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //location Permission already granted
                return true;
            } else {
                return false;
            }
        } else {
            //location Permission already granted
            return true;
        }
    }

    /**
     * Request permission
     */
    private void startRequestForPermission () {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    /**
     * Call downloading the image in BoundService
     */
    private void downloadImage() {
        String response = mBoundService.downloadImage();
        mBoundServiceTextView.setText(response);
    }

    /**
     * Create BroadcastReceiver
     */
    private void createBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "OnReceive");
                File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Downloads/1.jpeg");
                mStartedServiceImageView.setImageBitmap(BitmapFactory.decodeFile(dir.getPath()));
                mStartedServiceTextView.setText(FINISH_DOWNLOADING);
            }
        };
    }
}
