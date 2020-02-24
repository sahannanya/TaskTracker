package com.practice.tasktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    static private final String TAGm="MainActivity";

    Button btnStartAlarm, btnCancelAlarm;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStartAlarm = findViewById(R.id.btnStartAlarm);
        btnCancelAlarm = findViewById(R.id.btnCancelAlarm);

        //alarm logic
        alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyBroadCastReceiver.class);
        intent.setAction("com.practice.tasktracker.MY_ALARM_FINISHED");
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);


        btnStartAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAGm, "btnStartAlarm clicked. ");
                startAlarm();
//                    Util.playBeepToAlert();

            }
        });

        btnCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAGm, "btnCancelAlarm clicked. ");
                cancelAlarm();
            }
        });

//        PeriodicWorkRequest beepWorkRequest = new PeriodicWorkRequest.Builder(PlaySoundListenableWorker.class,15, TimeUnit.MINUTES)
//                .build();
//        WorkManager.getInstance(getApplicationContext())
//                .enqueue(beepWorkRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    private void checkPermission(){
        Log.d(TAGm, "checkPermission() called. ");

        if (!Util.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }
    private void startAlarm() {
    Log.d(TAGm, "startAlarm() called. ");
    Util.startAlarm(alarmManager,pendingIntent);
    Toast.makeText(getApplicationContext(), "Started tracking tasks", Toast.LENGTH_LONG).show();

    }

    private void cancelAlarm() {
        Util.cancelAlarm(alarmManager,pendingIntent);
        Toast.makeText(getApplicationContext(), "Stopped tracking tasks", Toast.LENGTH_LONG).show();
    }

}
