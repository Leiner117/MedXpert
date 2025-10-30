package com.tec.medxpert.ui.availability;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.ViewHolder> {

    private final List<Availability> data = new ArrayList<>();
    private OnDeleteClickListener deleteClickListener;
    public void setData(List<Availability> dataSet) {
        data.clear();
        data.addAll(dataSet);
        notifyDataSetChanged();
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Availability availability);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeText;
        ImageView deleteIcon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.tv_time);
            deleteIcon = itemView.findViewById(R.id.iv_delete);
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public AvailabilityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view
    @Override
    public void onBindViewHolder(@NonNull AvailabilityAdapter.ViewHolder holder, final int position) {

        Availability availability = data.get(position);
        holder.timeText.setText(availability.getTime());

        holder.deleteIcon.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(availability);
            }
        });
    }

    // Get the size of the list
    @Override
    public int getItemCount() {
        return data.size();
    }
}
