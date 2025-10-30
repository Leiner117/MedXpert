package com.tec.medxpert.ui.profile.patient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.profile.ChangeRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying change history records in a RecyclerView
 */
public class ChangeHistoryAdapter extends RecyclerView.Adapter<ChangeHistoryAdapter.ViewHolder> {

    private List<ChangeRecord> changeRecords = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_change_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChangeRecord record = changeRecords.get(position);
        Context context = holder.itemView.getContext();

        // Set the text for each field in the change record
        String changeInText = context.getString(R.string.change_in) + " " + record.getFieldName();
        holder.changeTitle.setText(changeInText);

        String previousText = context.getString(R.string.previous) + " " + record.getPreviousValue();
        holder.previousValue.setText(previousText);

        String currentText = context.getString(R.string.current) + " " + record.getCurrentValue();
        holder.currentValue.setText(currentText);

        if (record.getDate() != null) {
            String dateText = context.getString(R.string.date_of_change) + " " + dateFormat.format(record.getDate());
            holder.changeDate.setText(dateText);

            String timeText = context.getString(R.string.time) + " " + timeFormat.format(record.getDate());
            holder.changeTime.setText(timeText);
        } else {
            String unknownDate = context.getString(R.string.date_of_change) + " Unknown";
            holder.changeDate.setText(unknownDate);

            String unknownTime = context.getString(R.string.time) + " Unknown";
            holder.changeTime.setText(unknownTime);
        }

        String changedByText = context.getString(R.string.changed_by) + " " + record.getUserName();
        holder.changedBy.setText(changedByText);
    }

    @Override
    public int getItemCount() {
        return changeRecords.size();
    }

    public void setChangeRecords(List<ChangeRecord> changeRecords) {
        this.changeRecords = changeRecords;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView changeTitle;
        TextView previousValue;
        TextView currentValue;
        TextView changeDate;
        TextView changeTime;
        TextView changedBy;

        ViewHolder(View itemView) {
            super(itemView);
            changeTitle = itemView.findViewById(R.id.change_title);
            previousValue = itemView.findViewById(R.id.previous_value);
            currentValue = itemView.findViewById(R.id.current_value);
            changeDate = itemView.findViewById(R.id.change_date);
            changeTime = itemView.findViewById(R.id.change_time);
            changedBy = itemView.findViewById(R.id.changed_by);
        }
    }
}