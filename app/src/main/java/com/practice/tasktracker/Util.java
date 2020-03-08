package com.practice.tasktracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Util {
    static private final String TAGm="Util";
    static public final String ALARM_RECIEVER_PACKAGE_NAME = "com.practice.tasktracker.MY_ALARM_FINISHED";
    static public final String ALARM_CANCEL_RECIEVER_PACKAGE_NAME = "com.practice.tasktracker.ALARM_CANCEL";
    static public final String RECORDED_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordedTasks";
    static public final int PROMPT_INTERVAL_IN_MINS = 2;
    static public final int PROMPT_DURATION_IN_MILISEC = 10 * 1000;

    public static void playBeepToAlert(){
        Log.d(TAGm, "playBeepToAlert() called");
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
    }

    public static void startAlarm(AlarmManager alarmManager, PendingIntent pendingIntent, long time){
        Log.d(TAGm, "startAlarm() called. ");
        long timeInMilliSec = (time == 0) ? incrementTimeByGivenDuration(PROMPT_INTERVAL_IN_MINS,0).getTimeInMillis(): time;
        Log.d(TAGm, "startAlarm() :: next alarm in::"+ timeInMilliSec + " mili secs");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                    timeInMilliSec,
                    pendingIntent);

//            alarmManager.setAlarmClock(
//                    AlarmManager.RTC_WAKEUP,
//                    incrementTimeByGivenDuration(PROMPT_INTERVAL_IN_MINS,0).getTimeInMillis() ,
//                    pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo,pendingIntent);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0,1000 * 60 * PROMPT_INTERVAL_IN_MINS, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        }
    }

    public static void cancelAlarm(AlarmManager alarmManager,PendingIntent pendingIntent){
        Log.d(TAGm, "cancelAlarm() called. ");
        alarmManager.cancel(pendingIntent);
    }

    public static Calendar incrementTimeByGivenDuration(int noOfMinutes, int noOfHours){
        Log.d(TAGm, "incrementTimeByGivenDuration() called. ");
        Log.d(TAGm, "incrementing prompt time by  " + noOfHours+" hours and "+ noOfMinutes+" minutes.");

        Calendar now = Calendar.getInstance();
        Calendar tmp = (Calendar) now.clone();
        tmp.add(Calendar.HOUR, noOfHours);
        tmp.add(Calendar.MINUTE, noOfMinutes);
        Log.d(TAGm, "Next prompt will be at " + tmp.get(Calendar.HOUR)+":"+ tmp.get(Calendar.MINUTE)+":"+ tmp.get(Calendar.SECOND));

        return tmp;
    }

    public static void stopRecording(MediaRecorder myAudioRecorder){
        Log.d(TAGm, "stopRecording() called. ");
            try {
                Log.d(TAGm, "stopRecording() :: try block");

                myAudioRecorder.stop();
            } catch(RuntimeException stopException) {
                Log.d(TAGm, "stopRecording() :: catch block");

                myAudioRecorder.release();
                myAudioRecorder = null;
            }
        Log.d(TAGm, "stopRecording() :: Audio Recorder stopped");
    }

    public static void startRecording(MediaRecorder myAudioRecorder) {
        Log.d(TAGm, "startRecording() called. ");
        playBeepToAlert();
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAGm, "startRecording() :: Audio Recorder startRecording");
    }

    public static MediaRecorder createMediaSource(){
        Log.d(TAGm, "createMediaSource() called");
//        String  file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordedTasks";
        File file= new File(RECORDED_DIRECTORY);
        Log.d(TAGm, "file path::" + RECORDED_DIRECTORY);


        if (!file.exists()){
            file.mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        MediaRecorder  myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        myAudioRecorder.setAudioEncodingBitRate(16);
        myAudioRecorder.setAudioSamplingRate(44100);
        myAudioRecorder.setOutputFile(file.getAbsolutePath()+ "/recording"+sdf.format(new Date())+".mp3");

        return myAudioRecorder;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static long getStartAlarmTimeInMiliSec(Context context){
        //alarm logic
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.alarm_prefrence_file_key), Context.MODE_PRIVATE);
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, sharedPref.getInt(context.getString(R.string.preference_start_at_hour),0));
        calendar.set(Calendar.MINUTE, sharedPref.getInt(context.getString(R.string.preference_start_at_min),0));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getEndAlarmTimeInMiliSec(Context context){
        //alarm logic
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.alarm_prefrence_file_key), Context.MODE_PRIVATE);
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, sharedPref.getInt(context.getString(R.string.preference_end_at_hour),0));
        calendar.set(Calendar.MINUTE, sharedPref.getInt(context.getString(R.string.preference_end_at_min),0));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}

