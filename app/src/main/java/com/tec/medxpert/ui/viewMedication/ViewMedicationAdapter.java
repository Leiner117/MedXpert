package com.tec.medxpert.ui.viewMedication;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.Medication;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class  ViewMedicationAdapter extends RecyclerView.Adapter<ViewMedicationAdapter.MedicationViewHolder> {
    private final List<Medication> medicationList;

    public ViewMedicationAdapter(List<Medication> medicationList) {
        this.medicationList = medicationList;
    }

    public void updateData(List<Medication> newMedicationList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new MedicationDiffCallback(medicationList, newMedicationList)
        );
        medicationList.clear();
        medicationList.addAll(newMedicationList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication med = medicationList.get(position);
        holder.nameTextView.setText(med.getName());
        holder.descriptionTextView.setText(med.getDescription());

        Timestamp timestamp = med.getRegistrationDate();
        if (timestamp != null) {
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(timestamp.toDate());
            holder.registrationDateTextView.setText(formattedDate);
        } else {
            holder.registrationDateTextView.setText("N/A");
        }

        holder.editButton.setOnClickListener(v -> {
            if (holder.itemView.getContext() instanceof ViewMedicationActivity) {
                ViewMedicationActivity activity = (ViewMedicationActivity) holder.itemView.getContext();
                activity.getViewModel().onEditMedication(med);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog(holder.itemView.getContext(), med);
        });
    }

    private void showDeleteConfirmationDialog(Context context, Medication medication) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_deletion_title))
                .setMessage(context.getString(R.string.confirm_deletion_message))
                .setPositiveButton(context.getString(R.string.delete_button_text), (dialog, which) -> {
                    if (context instanceof ViewMedicationActivity) {
                        ViewMedicationViewModel viewModel = ((ViewMedicationActivity) context).getViewModel();
                        viewModel.deleteMedication(medication);
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel_button_text), null)
                .show();
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, registrationDateTextView;
        ImageButton editButton, deleteButton;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            registrationDateTextView = itemView.findViewById(R.id.registrationDateTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private static class MedicationDiffCallback extends DiffUtil.Callback {
        private final List<Medication> oldList;
        private final List<Medication> newList;

        public MedicationDiffCallback(List<Medication> oldList, List<Medication> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getName().equals(newList.get(newItemPosition).getName());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}
