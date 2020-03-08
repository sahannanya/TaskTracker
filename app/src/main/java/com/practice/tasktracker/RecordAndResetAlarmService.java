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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.practice.tasktracker.db.CapturedDataDao;
import com.practice.tasktracker.db.CapturedDataEntity;
import com.practice.tasktracker.db.DataRepository;
import com.practice.tasktracker.db.DataViewModel;
import com.practice.tasktracker.db.TaskTrackerRoomDB;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RecordAndResetAlarmService extends IntentService {
    static private final String TAGm="RecordAndResetAlarmSer";
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;
    boolean isDataUsable;
    private CapturedDataDao mDataDao;
    ExecutorService mExecutor;
//    Date currentTime;


    public RecordAndResetAlarmService() {
        super("RecordAndResetAlarmService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
        Bundle extras = intent.getExtras();
        if(extras == null) {
            Log.d(TAGm,"intent extra null ! abort");
        } else {
            String from = (String) extras.get("from");
            if(from.equalsIgnoreCase("normal")){
                if ((ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAGm, "onHandleIntent() :: Permission missing. Prompt user to give permission");
                    Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                    startActivity(i);
                }else{
                    Log.d(TAGm, "onHandleIntent()  else. ");
                    captureData();
                }
            }else if(from.equalsIgnoreCase("rebooted")){
                modifyAlarm("rebooted");
            }else if(from.equalsIgnoreCase("cancelAlarm")){
                modifyAlarm("cancel");
                modifyAlarm("resetToNextDay");
            }
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
                    Log.d(TAGm,"initalizing speech recognizer");
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
                            isDataUsable = false;
                            if(mSpeechRecognizer!=null){
//                                mSpeechRecognizer.stopListening();
//                                mSpeechRecognizer.cancel();
                                mSpeechRecognizer.destroy();

                            }
                            mSpeechRecognizer = null;
                            final MediaRecorder mr = Util.createMediaSource();
                            Util.startRecording(mr);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                Util.stopRecording(mr);
                                insertItemInDB(Calendar.getInstance().getTime().toString()," Captured audio recording" +
                                        "");

                                }}, Util.PROMPT_DURATION_IN_MILISEC);
                        }

                        @Override
                        public void onResults(Bundle bundle) {
                            ArrayList<String> matches = bundle
                                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                            if (matches != null){
                                Log.d(TAGm,"doWork :: onResults :"+matches.get(0));
                                insertItemInDB(Calendar.getInstance().getTime().toString(),matches.get(0));
                                isDataUsable = true;
//                            mSpeechRecognizer.stopListening();
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
//                            mSpeechRecognizer.stopListening();
//                            Log.d(TAGm,"doWork :: stopListening() called at:: "+ Calendar.getInstance().getTime());
                            modifyAlarm("reset");
                        }}, Util.PROMPT_DURATION_IN_MILISEC);
            }
        };
        mainHandler.post(myRunnable);
    }

    public void modifyAlarm(String action){
        //reset alarm
        Log.d(TAGm, "onHandleIntent() ::resetting alarm ");
        alarmManager = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentTest = new Intent(getBaseContext(), MyBroadCastReceiver.class);
        intentTest.setAction(Util.ALARM_RECIEVER_PACKAGE_NAME);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intentTest, 0);
        switch (action){
            case "rebooted" : {
                Util.startAlarm(alarmManager,pendingIntent,Util.getStartAlarmTimeInMiliSec(this));
                break;
            }
            case "cancel" : {
                Util.cancelAlarm(alarmManager,pendingIntent);
                break;
            }
            case "resetToNextDay" : {
                Util.startAlarm(alarmManager, pendingIntent, Util.getStartAlarmTimeInMiliSec(this) + 86400000L);
                break;
            }
            case "reset" : {
                Util.startAlarm(alarmManager,pendingIntent,0);
                break;
            }
            default : break;
        }
    }
}

