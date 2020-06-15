package com.practice.tasktracker.db;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class DataRepository {
    private final CapturedDataDao mDataDao;
    private final LiveData<List<CapturedDataEntity>> mAllData;

    public DataRepository(Application application) {
        TaskTrackerRoomDB tasksRoomDatabase = TaskTrackerRoomDB.getDatabase(application);
        this.mDataDao = tasksRoomDatabase.CapturedDataDao();
        this.mAllData = mDataDao.getAllData();
    }

    LiveData<List<CapturedDataEntity>> getAllData() {
        return mAllData;
    }

    public void insert(CapturedDataEntity dataItem) {
        new insertAsyncTask(mDataDao).execute(dataItem);
    }

    private static class insertAsyncTask extends AsyncTask<CapturedDataEntity, Void, Void> {
        private final CapturedDataDao mAsyncTaskDao;
        insertAsyncTask(CapturedDataDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final CapturedDataEntity... params) {
            mAsyncTaskDao.insertItem(params[0]);
            return null;
        }
    }


}
