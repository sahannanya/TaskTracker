package com.practice.tasktracker;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.practice.tasktracker.db.CapturedDataEntity;

import java.util.List;

// For user testing without logcat, remove recycler and below if not needed
public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder> {
    private List<CapturedDataEntity> mTasksList;

    public TasksAdapter(List<CapturedDataEntity> mTasksList) {
        this.mTasksList = mTasksList;
    }

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

        TextView mTimeStamp;
        TextView mDetails;

        TasksViewHolder(View itemView) {
            super(itemView);
            mTimeStamp = itemView.findViewById(R.id.tv_capture_item_time);
            mDetails = itemView.findViewById(R.id.tv_capture_item_details);
        }
    }
}
