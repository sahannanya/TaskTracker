package com.practice.tasktracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Locale;


public class PlaySoundListenableWorker extends ListenableWorker {
    private final String TAGm = "ListenableWorker";

    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;
    VoiceRecognizerCallBack callback;

    public PlaySoundListenableWorker(Context context, WorkerParameters params){
        super(context,params);
    }

    public ListenableFuture<Result> startWork() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
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
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.d(TAGm,"doWork :: onResults called()");

                //getting all the matches
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                //displaying the first match
                if (matches != null){
                    String capturedWords = matches.get(0);
                    Log.d(TAGm,"doWork :: onResults : "+capturedWords);
                    Toast.makeText(getApplicationContext(),capturedWords,Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        return CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull final CallbackToFutureAdapter.Completer<Result> completer) throws Exception {
                 callback = new VoiceRecognizerCallBack() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d(TAGm,"startWork :: onFailure called()");
                        completer.setException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAGm,"startWork :: onResponse called()");
                        if (response != null){
                            Log.d(TAGm,"startWork :: onResponse : "+response);
                            completer.set(Result.success());
                        }
                    }
                };

                PlaySoundListenableWorker.this.record();

                return callback;
            }
        });
    }

    public void record(){
        Log.d(TAGm,"record() called.");

        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());

        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        Log.d(TAGm,"doWork :: startListening() called.");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSpeechRecognizer.stopListening();
                Log.d(TAGm,"doWork :: stopListening() called");
            }
        }, 8000 );
    }
}
