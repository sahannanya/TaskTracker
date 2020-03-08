package com.practice.tasktracker.db;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CapturedDataEntity.class}, version = 1, exportSchema = false)
public abstract class TaskTrackerRoomDB
        extends RoomDatabase {
    private static TaskTrackerRoomDB INSTANCE;

    public abstract CapturedDataDao CapturedDataDao();

    public static TaskTrackerRoomDB getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    TaskTrackerRoomDB.class, "task_tracker_table")
                    .build();
        }
        return INSTANCE;
    }

}
