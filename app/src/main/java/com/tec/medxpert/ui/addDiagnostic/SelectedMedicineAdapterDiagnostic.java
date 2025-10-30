package com.tec.medxpert.ui.addDiagnostic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Medicine;

import java.util.List;

public class SelectedMedicineAdapterDiagnostic extends RecyclerView.Adapter<SelectedMedicineAdapterDiagnostic.SelectedMedicineViewHolder> {
    private List<Medicine> selectedMedicines;
    private OnMedicineRemovedListener listener;

    public interface OnMedicineRemovedListener {
        void onMedicineRemoved(int position);
    }

    public SelectedMedicineAdapterDiagnostic(List<Medicine> selectedMedicines, OnMedicineRemovedListener listener) {
        this.selectedMedicines = selectedMedicines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SelectedMedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_medicine_diagnostic, parent, false);
        return new SelectedMedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedMedicineViewHolder holder, int position) {
        Medicine medicine = selectedMedicines.get(position);
        holder.tvMedicineName.setText(medicine.getName());
        holder.tvHours.setText(holder.itemView.getContext().getString(R.string.hours_format, medicine.getHours()));
        holder.tvDays.setText(holder.itemView.getContext().getString(R.string.days_format, medicine.getDays()));
        holder.tvDosage.setText(medicine.getDosage());

        holder.btnRemove.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onMedicineRemoved(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedMedicines.size();
    }

    public void updateSelectedMedicines(List<Medicine> selectedMedicines) {
        this.selectedMedicines = selectedMedicines;
        notifyDataSetChanged();
    }

    static class SelectedMedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvHours, tvDays, tvDosage;
        ImageButton btnRemove;

        public SelectedMedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvHours = itemView.findViewById(R.id.tvHours);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
