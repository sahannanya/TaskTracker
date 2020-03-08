package com.practice.tasktracker.db;

//import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_tracker_table")
public class CapturedDataEntity {
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String timeStamp;
    private String capturedDetails;

    @Ignore
    public CapturedDataEntity(String timeStamp, String capturedDetails) {
        this.timeStamp = timeStamp;
        this.capturedDetails = capturedDetails;
    }

    public CapturedDataEntity() {
        this.timeStamp = "timeStamp";
        this.capturedDetails = "capturedDetails";
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCapturedDetails() {
        return capturedDetails;
    }

    public void setCapturedDetails(String capturedDetails) {
        this.capturedDetails = capturedDetails;
    }
}
