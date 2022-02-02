package com.practice.tasktracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.practice.tasktracker.db.CapturedDataEntity;
import com.practice.tasktracker.db.DataViewModel;

import java.util.Calendar;
import java.util.List;

import static com.practice.tasktracker.Util.convertTimeTo12Hour;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static private final String TAGm="MainActivity";

    Button btnTimePickerStart, btnTimePickerEnd;
    ImageButton scheduleAlarm;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    TextView alarmStartTime;
    TextView alarmEndTime;
    TextView textviewAlarmEnd;
    SharedPreferences sharedPref;
    CardView manualCardView;
    Switch defaultHoursSwitch;
    Spinner durationSpinner;
    boolean isTrackingOn;

    public static final int MULTIPLE_PERMISSIONS = 1;
    final String[] PERMISSIONS = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
    };

    //Remove if UI not needed
    private RecyclerView recyclerView;
    private static List<CapturedDataEntity> mTasksList;
    private TasksAdapter mTasksAdapter;
    //////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        btnTimePickerStart = findViewById(R.id.editTextTimeStart);
        btnTimePickerEnd = findViewById(R.id.editTextTimeEnd);
        scheduleAlarm = findViewById(R.id.button_start_cancel);
        alarmStartTime = findViewById(R.id.textview_start_time);
        alarmEndTime = findViewById(R.id.textview_end_time);
        textviewAlarmEnd = findViewById(R.id.textview_alarm);
        manualCardView = findViewById(R.id.manual_prompt);
        defaultHoursSwitch = findViewById(R.id.switch1);

        this.sharedPref = getSharedPreferences(
                getString(R.string.alarm_preference_file_key), Context.MODE_PRIVATE);
        int startHr = sharedPref.getInt(getString(R.string.preference_start_at_hour),0);
        int startMin = sharedPref.getInt(getString(R.string.preference_start_at_min),0);

        int endHr = sharedPref.getInt(getString(R.string.preference_end_at_hour),0);
        int endMin = sharedPref.getInt(getString(R.string.preference_end_at_min),0);


        if(startHr == 0 && startMin == 0 && endHr == 0 && endMin ==0) {
            // therefore all time is in default state. Hide the manaul settings for first time
            setDefaultTime();
            isTrackingOn = false;
        } else{
            alarmEndTime.setText(convertTimeTo12Hour(endHr,endMin));
            alarmStartTime.setText(convertTimeTo12Hour(startHr,startMin));
            isTrackingOn = true;
        }


        durationSpinner = (Spinner) findViewById(R.id.duration_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.duration_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);
        durationSpinner.setSelection(sharedPref.getInt(getString(R.string.preference_time_gap), 0),false);
        durationSpinner.setOnItemSelectedListener(this);



        btnTimePickerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAGm, "btnTimePickerStart clicked. ");
                showTimePicker("from");
            }
        });

        btnTimePickerEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAGm, "btnTimePickerEnd clicked. ");
                showTimePicker("to");
            }
        });

        scheduleAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAGm, "scheduleAlarm clicked. ");
                if(isTrackingOn){
                    Log.d(TAGm, "scheduleAlarm :: cancel triggered  ");
                    cancelSchdueler();
                } else {
                    Log.d(TAGm, "scheduleAlarm start alarm triggered ");
                    startAlarm();
                    changePlayToCancel();
                }

            }
        });

        defaultHoursSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAGm, "setOnCheckedChangeListener clicked. ");

                    setDefaultTime();
                } else {
                    showManualTimeInputField();
                }
            }
        });

        //For testing with UI
        recyclerView = findViewById(R.id.rv_data);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(null);
        DataViewModel mDataViewModel = ViewModelProviders.of(this).get(DataViewModel.class);
        final Observer<List<CapturedDataEntity>> tasksListObserver = new Observer<List<CapturedDataEntity>>() {
            @Override
            public void onChanged(@Nullable final List<CapturedDataEntity> updatedList) {
                if (mTasksList == null) {
                    mTasksList = updatedList;
                    mTasksAdapter = new TasksAdapter();
                    recyclerView.setAdapter(mTasksAdapter);
                } else {
                    DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {

                        @Override
                        public int getOldListSize() {
                            return mTasksList.size();
                        }

                        @Override
                        public int getNewListSize() {
                            if(updatedList!=null)
                                return updatedList.size();
                            else return 0;
                        }

                        @Override
                        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                            return mTasksList.get(oldItemPosition).getTimeStamp() .equals(updatedList.get(newItemPosition).getTimeStamp());
                        }

                        @Override
                        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                            CapturedDataEntity oldTask = mTasksList.get(oldItemPosition);
                            CapturedDataEntity newTask = updatedList.get(newItemPosition);
                            return oldTask.equals(newTask);
                        }
                    });
                    result.dispatchUpdatesTo(mTasksAdapter);
                    mTasksList = updatedList;
                }
            }
        };


        mDataViewModel.getAllData().observe(this, tasksListObserver);

    }

    // For user testing without logcat, remove recycler and below if not needed
    public static class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder> {

        @Override
        public TasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.captured_item, parent, false);
            return new TasksViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(TasksViewHolder holder, int position) {
            CapturedDataEntity favourites = mTasksList.get(position);
            holder.mTimeStamp.setText(favourites.getTimeStamp());
            holder.mDetails.setText(favourites.getCapturedDetails());
        }

        @Override
        public int getItemCount() {
            return mTasksList.size();
        }

        class TasksViewHolder extends RecyclerView.ViewHolder {

            final TextView mTimeStamp;
            final TextView mDetails;

            TasksViewHolder(View itemView) {
                super(itemView);
                mTimeStamp = itemView.findViewById(R.id.tv_capture_item_time);
                mDetails = itemView.findViewById(R.id.tv_capture_item_details);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissionsList, int[] grantResults) {
        if(requestCode == MULTIPLE_PERMISSIONS){
            boolean flag = true;
            int permissionCount = 0;
            if (grantResults.length > 0) {
                while(permissionCount <= 3){
                    if(grantResults[permissionCount] == PackageManager.PERMISSION_DENIED){
                        flag = flag && false;
                    }
                    permissionCount++;
                }
            }
            if(flag){
                startAlarm();
            }else{
                Toast.makeText(MainActivity.this,getString(R.string.permission_error),Toast.LENGTH_LONG).show();
            }
        }
    }


    private void showTimePicker(final String pickerType){
            Calendar mCurrentTime = Calendar.getInstance();
        int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mCurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        String title;
        if(pickerType.equals("from")){
            title = getString(R.string.start_tracking_from);
        }else{
            title = getString(R.string.end_tracking_at);
        }

        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                Log.d(TAGm, "TimePickerDialog() called. ");
                SharedPreferences.Editor editor = sharedPref.edit();

                if(pickerType.equals("from")){
                    editor.putInt(getString(R.string.preference_start_at_hour), selectedHour);
                    editor.putInt(getString(R.string.preference_start_at_min), selectedMinute);
                    editor.apply();
                    alarmStartTime.setText(convertTimeTo12Hour(selectedHour, selectedMinute));
                }else{
                    editor.putInt(getString(R.string.preference_end_at_hour), selectedHour);
                    editor.putInt(getString(R.string.preference_end_at_min), +selectedMinute);
                    editor.apply();
                    alarmEndTime.setText(convertTimeTo12Hour(selectedHour, selectedMinute));
                }


            }
        }, hour, minute, false);//12 hour time
        mTimePicker.setTitle(title);
        mTimePicker.show();
    }

    private void startAlarm() {
    Log.d(TAGm, "startAlarm() called. ");

        if (!Util.hasPermissions(MainActivity.this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, MULTIPLE_PERMISSIONS);
        }else{
            Log.d(TAGm, "startAlarm()::  starting first alarm");

            alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, MyBroadCastReceiver.class);
            intent.setAction(Util.ALARM_RECEIVER_PACKAGE_NAME);
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            Util.startAlarm(alarmManager,pendingIntent,Util.getStartAlarmTimeInMilliSec(this));
            Toast.makeText(this,getString(R.string.started_tracking),Toast.LENGTH_SHORT).show();
            //Also set alarm to cancel alarms at selected time
            Log.d(TAGm, "startAlarm()::  stopping  last  alarm for the day");
            intent.setAction(Util.ALARM_CANCEL_RECEIVER_PACKAGE_NAME);
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            Util.startAlarm(alarmManager,pendingIntent,Util.getEndAlarmTimeInMilliSec(this));
            isTrackingOn = true;
            changePlayToCancel();
        }
    }
    /**
     * Sets default tracking time as 7AM to 8PM everyday
     * **/
    public void setDefaultTime(){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.preference_start_at_hour), 7);
        editor.putInt(getString(R.string.preference_start_at_min), 00);
        editor.putInt(getString(R.string.preference_end_at_hour), 20);
        editor.putInt(getString(R.string.preference_end_at_min), +00);

        disableManualTimeInputFields();
    }
    /**
     * Makes manual fields disabled and gone from visibility
     * **/
    public void disableManualTimeInputFields(){
        alarmStartTime.setText("");
        alarmEndTime.setText("");

        defaultHoursSwitch.setChecked(true);
        manualCardView.setVisibility(View.GONE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(getString(R.string.preference_start_at_hour), 0);
        editor.putInt(getString(R.string.preference_start_at_min), 0);
        editor.putInt(getString(R.string.preference_end_at_hour), 0);
        editor.putInt(getString(R.string.preference_end_at_min), 0);
        editor.apply();

    }

    public void showManualTimeInputField(){
        manualCardView.setVisibility(View.VISIBLE);
        defaultHoursSwitch.setChecked(false);
    }

    public void changePlayToCancel() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.preference_tracker_is_running), true);
        textviewAlarmEnd.setText(R.string.cancel_prompt);
        scheduleAlarm.setImageResource(R.drawable.icons8_close_24);
    }

    public void cancelSchdueler(){
        Log.d(TAGm, "cancelSchdueler called");

        Intent intent = new Intent(this, MyBroadCastReceiver.class);
        intent.setAction(Util.ALARM_CANCEL_RECEIVER_PACKAGE_NAME);
        sendBroadcast(intent);

        scheduleAlarm.setImageResource(R.drawable.icons8_play_24);
        textviewAlarmEnd.setText(R.string.start_prompt);
        isTrackingOn = false;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Util.setPromptGap(pos * 5 + 5);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.preference_time_gap), pos);
        editor.apply();

    }

    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAGm, "onNothingSelected called");
    }
}