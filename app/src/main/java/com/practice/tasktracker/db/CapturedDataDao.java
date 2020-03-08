package com.practice.tasktracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;
@Dao
public interface CapturedDataDao {

    @Insert(onConflict = IGNORE)
    void insertItem(CapturedDataEntity item);

    @Delete
    void deleteItem(CapturedDataEntity person);

    //Delete one item by id
    @Query("DELETE FROM task_tracker_table WHERE id = :itemId")
    void deleteByItemId(long itemId);

    //Get all items
    @Query("SELECT * FROM task_tracker_table")
    LiveData<List<CapturedDataEntity>> getAllData();

    //Delete All
    @Query("DELETE FROM task_tracker_table")
    void deleteAll();
}
