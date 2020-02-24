package com.practice.tasktracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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

    public static void playBeepToAlert(){
        Log.d(TAGm, "playBeepToAlert() called");
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
    }

    public static void startAlarm(AlarmManager alarmManager, PendingIntent pendingIntent){
        Log.d(TAGm, "startAlarm() called. ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    incrementTimeByGivenDuration(2,0).getTimeInMillis() ,
                    pendingIntent);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
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

        Calendar now = Calendar.getInstance();
        Calendar tmp = (Calendar) now.clone();
        tmp.add(Calendar.HOUR, noOfHours);
        tmp.add(Calendar.MINUTE, noOfMinutes);
        Log.d(TAGm, "Next Alarm will be at " + tmp.get(Calendar.HOUR)+":"+ tmp.get(Calendar.MINUTE)+":"+ tmp.get(Calendar.SECOND));

        return tmp;
    }

    public static void stopRecording(MediaRecorder myAudioRecorder){
        Log.d(TAGm, "stopRecording() called. ");

//        myAudioRecorder.stop();
//        myAudioRecorder.release();
        myAudioRecorder = null;
        Log.d(TAGm, "stopRecording() :: Audio Recorder stopped");
    }

    public static void startRecording(MediaRecorder myAudioRecorder) {
        Log.d(TAGm, "startRecording() called. ");
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
        String  file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recordedTasks";
        File file= new File(file_path);

        if (!file.exists()){
            file.mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//        System.out.println(sdf.format(new Date()));
        MediaRecorder  myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        myAudioRecorder.setOutputFile(file.getAbsolutePath()+ "/recording.mp3");
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
}

