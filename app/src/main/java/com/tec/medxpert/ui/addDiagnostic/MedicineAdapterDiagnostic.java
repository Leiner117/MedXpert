package com.tec.medxpert.ui.addDiagnostic;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Medicine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicineAdapterDiagnostic extends RecyclerView.Adapter<MedicineAdapterDiagnostic.MedicineViewHolder> {
    private final List<Medicine> medicineList;
    private final List<Medicine> medicineListFull;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private Medicine selectedMedicine = null;
    private final OnMedicineSelectedListener listener;

    public interface OnMedicineSelectedListener {
        void onMedicineSelected(@Nullable Medicine medicine);
    }

    public MedicineAdapterDiagnostic(@NonNull List<Medicine> medicineList,
                                     @NonNull OnMedicineSelectedListener listener) {
        this.medicineList = new ArrayList<>(medicineList);
        this.medicineListFull = new ArrayList<>(medicineList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_medicine_diagnostic, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);

        if (medicine == null || medicine.getName() == null) {
            return;
        }

        holder.tvMedicineName.setText(medicine.getName());
        holder.rbSelect.setChecked(position == selectedPosition);

        holder.rbSelect.setOnClickListener(v -> selectMedicine(holder.getAdapterPosition()));
        holder.itemView.setOnClickListener(v -> selectMedicine(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return medicineList != null ? medicineList.size() : 0;
    }

    private void selectMedicine(int position) {
        if (position == RecyclerView.NO_POSITION || position >= medicineList.size()) {
            return;
        }

        int previousPosition = selectedPosition;
        selectedPosition = position;
        selectedMedicine = medicineList.get(position); // Guardar referencia al medicamento

        listener.onMedicineSelected(selectedMedicine);

        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        }
        notifyItemChanged(selectedPosition);
    }

    public void setSelectedMedicine(Medicine medicine) {
        this.selectedMedicine = medicine;
        updateSelectedPosition();
    }

    private void updateSelectedPosition() {
        selectedPosition = RecyclerView.NO_POSITION;

        if (selectedMedicine != null) {
            for (int i = 0; i < medicineList.size(); i++) {
                Medicine currentMedicine = medicineList.get(i);
                if (currentMedicine != null &&
                        currentMedicine.getId() != null &&
                        currentMedicine.getId().equals(selectedMedicine.getId())) {
                    selectedPosition = i;
                    break;
                }
            }
        }
    }

    public Filter getFilter() {
        return medicineFilter;
    }

    private final Filter medicineFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Medicine> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(medicineListFull);
            } else {
                String filterPattern = constraint.toString()
                        .toLowerCase(Locale.getDefault())
                        .trim();

                for (Medicine medicine : medicineListFull) {
                    if (medicine != null &&
                            medicine.getName() != null &&
                            medicine.getName().toLowerCase(Locale.getDefault())
                                    .contains(filterPattern)) {
                        filteredList.add(medicine);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        @SuppressLint("NotifyDataSetChanged")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values instanceof List) {
                medicineList.clear();
                medicineList.addAll((List<Medicine>) results.values);

                updateSelectedPosition();

                notifyDataSetChanged();
            }
        }
    };

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMedicineName;
        final RadioButton rbSelect;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            rbSelect = itemView.findViewById(R.id.rbSelect);
        }
    }
}