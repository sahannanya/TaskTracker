package com.practice.tasktracker.db;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DataViewModel
        extends AndroidViewModel {

    private DataRepository mDataRepository;
    private LiveData<List<CapturedDataEntity>> mListLiveData;

    public DataViewModel(@NonNull Application application) {
        super(application);
        mDataRepository = new DataRepository((application));
        mListLiveData = mDataRepository.getAllData();
    }

    public LiveData<List<CapturedDataEntity>> getAllData() {
        return mListLiveData;
    }

    public void insertItem(CapturedDataEntity dataItem) {
        mDataRepository.insert(dataItem);
    }

}
