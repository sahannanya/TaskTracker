package com.practice.tasktracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.practice.tasktracker.db.CapturedDataDao;
import com.practice.tasktracker.db.CapturedDataEntity;
import com.practice.tasktracker.db.TaskTrackerRoomDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RecordAndResetAlarmService extends IntentService {
    static private final String TAGm="RecordAndResetAlarmSer";
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;
    private CapturedDataDao mDataDao;
    ExecutorService mExecutor;
    String filePath;


    public RecordAndResetAlarmService() {
        super("RecordAndResetAlarmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        filePath = getBaseContext().getExternalFilesDir(null).getAbsolutePath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "task_tracker_service";
            String description = "Record audio for task tracker";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("task_tracker_service", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this,"task_tracker_service")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();
            startForeground(101, notification);
        }
        TaskTrackerRoomDB mDB = TaskTrackerRoomDB.getDatabase( this.getApplication() );
        mDataDao = mDB.CapturedDataDao();
        mExecutor = Executors.newSingleThreadExecutor();

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAGm, "onHandleIntent() called. ");

        if(intent != null){
            Bundle extras = intent.getExtras();
            if(extras == null) {
                Log.d(TAGm,"intent extra null ! abort");
            } else {
                String from = (String) extras.get("from");
                if(from != null && from.equalsIgnoreCase("normal")){
                    if ((ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED)) {
                        Log.d(TAGm, "onHandleIntent() :: Permission missing. Prompt user to give permission");
                        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }else{
                        Log.d(TAGm, "onHandleIntent()  else. ");
                        captureData();
                    }
                } else if(from.equalsIgnoreCase("rebooted")){
                    modifyAlarm("rebooted");
                } else if(from.equalsIgnoreCase("cancelAlarm")){
                    modifyAlarm("cancel");
                } else if(from.equalsIgnoreCase("resetToNextDay")){
                    modifyAlarm("resetToNextDay");
                }
            }
        }else{
            Log.d(TAGm,"intent is null !! abort");
        }


    }

    private void insertItemInDB(final String timestamp, final String data) {
        mExecutor.execute(new Runnable(){
            @Override
            public void run(){
                mDataDao.insertItem(new CapturedDataEntity(timestamp, data));
            }
        });
    }

    public void captureData(){
        Log.d(TAGm,"doWork :: captureData() called");
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {

                if(mSpeechRecognizer == null){
                    Log.d(TAGm,"init speech recognizer");
                    mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                    mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                        @Override
                        public void onReadyForSpeech(Bundle bundle) {

                        }

                        @Override
                        public void onBeginningOfSpeech() {

                        }

                        @Override
                        public void onRmsChanged(float v) {

                        }

                        @Override
                        public void onBufferReceived(byte[] bytes) {

                        }

                        @Override
                        public void onEndOfSpeech() {

                        }

                        @Override
                        public void onError(int error) {
                            String message;
                            switch (error) {
                                case SpeechRecognizer.ERROR_AUDIO:
                                    message = "Audio recording error";
                                    break;
                                case SpeechRecognizer.ERROR_CLIENT:
                                    message = "Client side error";
                                    break;
                                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                    message = "Insufficient permissions";
                                    break;
                                case SpeechRecognizer.ERROR_NETWORK:
                                    message = "Network error";
                                    break;
                                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                    message = "Network timeout";
                                    break;
                                case SpeechRecognizer.ERROR_NO_MATCH:
                                    message = "No match";
                                    break;
                                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                    message = "RecognitionService busy";
                                    break;
                                case SpeechRecognizer.ERROR_SERVER:
                                    message = "error from server";
                                    break;
                                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                    message = "No speech input";
                                    break;
                                default:
                                    message = "Didn't understand, please try again.";
                                    break;
                            }
                            Log.d(TAGm,"Error :: " +message);
                            if(mSpeechRecognizer!=null){
//                                mSpeechRecognizer.stopListening();
//                                mSpeechRecognizer.cancel();
                                mSpeechRecognizer.destroy();

                            }
                            mSpeechRecognizer = null;
                            final MediaRecorder mr = Util.createMediaSource(filePath);
                            if(mr != null ){
                                Util.startRecording(mr);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Util.stopRecording(mr);
                                        insertItemInDB(Calendar.getInstance().getTime().toString()," Captured audio recording");
                                    }}, Util.promptDurationInMiliSeconds);
                            }
                        }

                        @Override
                        public void onResults(Bundle bundle) {
                            ArrayList<String> matches = bundle
                                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                            if (matches != null){
                                Log.d(TAGm,"doWork :: onResults :"+matches.get(0));
                                insertItemInDB(Calendar.getInstance().getTime().toString(),matches.get(0));
                            }
                        }

                        @Override
                        public void onPartialResults(Bundle bundle) {

                        }

                        @Override
                        public void onEvent(int i, Bundle bundle) {

                        }
                    });
                    mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                }else{
                    Log.d(TAGm,"mSpeechRecognizer is not null");

                }

                Log.d(TAGm,"doWork :: started Listening() called at:: "+ Calendar.getInstance().getTime());
                Log.d(TAGm,"doWork :: is this main thread::"+  (Looper.myLooper() == Looper.getMainLooper()));
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //do something
                        Log.d(TAGm,"doWork :: is this main thread::"+  (Looper.myLooper() == Looper.getMainLooper()));
                        modifyAlarm("reset");
                    }}, Util.promptDurationInMiliSeconds);
            }
        };
        mainHandler.post(myRunnable);
    }

    public void modifyAlarm(String action){
        //reset alarm
        Log.d(TAGm, "onHandleIntent() ::resetting alarm " + action);
        alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentTest = new Intent(getBaseContext(), MyBroadCastReceiver.class);
        intentTest.setAction(Util.ALARM_RECEIVER_PACKAGE_NAME);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intentTest, 0);
        switch (action){
            case "rebooted" : {
                Util.startAlarm(alarmManager,pendingIntent,Util.getStartAlarmTimeInMilliSec(this));
                break;
            }
            case "cancel" : {
                Log.d(TAGm , "modifyAlarm called :: cancel triggered");
                Util.cancelAlarm(alarmManager,pendingIntent);
                break;
            }
            case "resetToNextDay" : {
                Log.d(TAGm , "modifyAlarm called :: resetToNextDay triggered");
                Util.startAlarm(alarmManager, pendingIntent, Util.getStartAlarmTimeInMilliSec(this) + 86400000L);
                break;
            }
            case "reset" : {
                Util.startAlarm(alarmManager,pendingIntent,0);
                break;
            }
            default : break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAGm,"onDestroy() called");

    }
}

