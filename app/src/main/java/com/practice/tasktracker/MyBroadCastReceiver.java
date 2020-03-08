package com.practice.tasktracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;


public class MyBroadCastReceiver extends BroadcastReceiver {
    static private final String TAGm="MyBroadCastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAGm, "onReceive() called. ");
        Log.d(TAGm, "onReceive() :: " + intent.getAction());
        Intent serviceIntent = new Intent(context, RecordAndResetAlarmService.class);

        if (intent.getAction() != null
                && intent.getAction().equals("com.practice.tasktracker.MY_ALARM_FINISHED")) {
            Log.d(TAGm, "Recieved : android.intent.action.MY_ALARM_FINISHED");
            serviceIntent.putExtra("from","normal");
            Log.d(TAGm, "starting service.. ");
            context.startService(serviceIntent);
        } else if(intent.getAction() != null
                && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            Log.d(TAGm, "Recieved : android.intent.action.BOOT_COMPLETED");
            serviceIntent.putExtra("from","rebooted");
            context.startService(serviceIntent);
        }else if(intent.getAction() != null
                && intent.getAction().equals("com.practice.tasktracker.ALARM_CANCEL")){
            Log.d(TAGm, "Recieved : com.practice.tasktracker.ALARM_CANCEL");
            serviceIntent.putExtra("from","cancelAlarm");
            context.startService(serviceIntent);
        }else {
            Log.d(TAGm, "onRecieve() else part ");
            Toast.makeText(context.getApplicationContext(), "Alarm Manager just ran", Toast.LENGTH_LONG).show();
        }

    }
}

