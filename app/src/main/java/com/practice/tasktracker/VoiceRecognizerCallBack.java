package com.practice.tasktracker;

public interface VoiceRecognizerCallBack {
    void onResponse(String response);
    void onFailure(Exception failure);
}
