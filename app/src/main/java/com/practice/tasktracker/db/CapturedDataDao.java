package com.practice.tasktracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;
@Dao
public interface CapturedDataDao {

    @Insert(onConflict = IGNORE)
    void insertItem(CapturedDataEntity item);

    //Get all items
    @Query("SELECT * FROM task_tracker_table")
    LiveData<List<CapturedDataEntity>> getAllData();


}
