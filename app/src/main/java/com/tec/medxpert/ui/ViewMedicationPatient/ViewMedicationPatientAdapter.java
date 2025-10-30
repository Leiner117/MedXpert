package com.tec.medxpert.ui.ViewMedicationPatient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.Medication;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ViewMedicationPatientAdapter extends RecyclerView.Adapter<ViewMedicationPatientAdapter.MedicationViewHolder>{
    private List<Medication> medicationList;

    public ViewMedicationPatientAdapter(List<Medication> medicationList) {
        this.medicationList = medicationList;
    }

    public void updateData(List<Medication> newMedicationList) {
        this.medicationList = newMedicationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_patient_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medicationList.get(position);

        holder.nameTextView.setText(medication.getName());
        holder.descriptionTextView.setText(medication.getDescription());
        holder.dosageTextView.setText(medication.getDosage());

        if (medication.getDiagnosticUpdatedAt() != null) {
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(medication.getDiagnosticUpdatedAt().toDate());
            holder.registrationDateTextView.setText(formattedDate);
        } else {
            holder.registrationDateTextView.setText("N/A");
        }

        holder.frecuencyUsageTextView.setText(medication.getFrecuencyUsageForPatientView());
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, descriptionTextView, dosageTextView, registrationDateTextView, frecuencyUsageTextView;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            dosageTextView = itemView.findViewById(R.id.dosageMedicationTextView);
            registrationDateTextView = itemView.findViewById(R.id.registrationDateTextView);
            frecuencyUsageTextView = itemView.findViewById(R.id.frecuencyUsageTextView);
        }
    }
}
