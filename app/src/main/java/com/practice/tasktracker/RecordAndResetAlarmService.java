package com.practice.tasktracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


public class RecordAndResetAlarmService extends IntentService {
    static private final String TAGm="RecordAndResetAlarmSer";
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    MediaRecorder myMediaRecorder;

    public RecordAndResetAlarmService() {
        super("RecordAndResetAlarmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAGm, "onCreate() called. ");
    }



    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAGm, "onStartCommand() called. ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAGm, "onHandleIntent() called. ");
        myMediaRecorder = new MediaRecorder();

        if ((ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
                &&(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
                &&(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {
            Log.d(TAGm, "onHandleIntent() :: Permission missing. Prompt user to give permission");
//            Toast.makeText(this,"Please provide microphone and storage permission",Toast.LENGTH_LONG).show();
            Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
            startActivity(i);
        }else{
            Log.d(TAGm, "onHandleIntent() MediaRecorder started. ");
            Util.playBeepToAlert();
            myMediaRecorder = Util.createMediaSource();

            Util.startRecording(myMediaRecorder);
            Log.d(TAGm, "onHandleIntent() :: recording started. ");

            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAGm, "onHandleIntent() ::postDelayed():: recording stopped. ");

//                    Util.stopRecording(myMediaRecorder);
                    Util.playBeepToAlert();

                    //reset alarm
                    Log.d(TAGm, "onHandleIntent() ::resetting alarm ");
                    alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intentTest = new Intent(getBaseContext(), MyBroadCastReceiver.class);
                    intentTest.setAction("com.practice.tasktracker.MY_ALARM_FINISHED");
                    pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intentTest, 0);
                    Util.startAlarm(alarmManager,pendingIntent);
                }
            }, 1000 * 10 );


        }



    }
}

