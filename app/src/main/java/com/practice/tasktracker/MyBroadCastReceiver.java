package com.practice.tasktracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class MyBroadCastReceiver extends BroadcastReceiver {
    static private final String TAGm="MyBroadCastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAGm, "onReceive() called. ");
        Log.d(TAGm, "with intent.getAction():: "+intent.getAction());
        Log.d(TAGm, "onCreate() called. ");

        if (intent.getAction() != null
                && (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
                || intent.getAction().equals("com.practice.tasktracker.MY_ALARM_FINISHED"))) {
            Intent serviceIntent = new Intent(context, RecordAndResetAlarmService.class);
            Log.d(TAGm, "starting service.. ");

            context.startService(serviceIntent);
        } else {
            Log.d(TAGm, "onRecieve() else part ");
            Toast.makeText(context.getApplicationContext(), "Alarm Manager just ran", Toast.LENGTH_LONG).show();
        }

    }
}

