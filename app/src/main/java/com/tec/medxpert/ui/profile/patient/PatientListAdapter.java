package com.tec.medxpert.ui.profile.patient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.profile.Patient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Adapter for displaying patient list in a RecyclerView
 */
public class PatientListAdapter extends RecyclerView.Adapter<PatientListAdapter.ViewHolder> {

    private List<Patient> patients = new ArrayList<>();
    private final OnPatientClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public PatientListAdapter(OnPatientClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Patient patient = patients.get(position);

        // Set patient name
        holder.patientName.setText(patient.getPersonalData().getName() != null ?
                patient.getPersonalData().getName() : "No name");

        // Set patient ID
        holder.patientId.setText(patient.getPersonalData().getIdNumber() != null ?
                patient.getPersonalData().getIdNumber() : "No ID");

        // Set registration date
        String registrationDate = patient.getCreatedAt() != null ?
                dateFormat.format(patient.getCreatedAt().toDate()) : "Unknown date";
        holder.registrationDate.setText(registrationDate);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPatientClick(patient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
        notifyDataSetChanged();
    }

    public void filterPatients(String query, List<Patient> originalList) {
        if (query == null || query.isEmpty()) {
            this.patients = originalList;
        } else {
            List<Patient> filteredList = new ArrayList<>();
            String lowercaseQuery = query.toLowerCase();

            for (Patient patient : originalList) {
                String name = patient.getPersonalData().getName();
                String id = patient.getPersonalData().getIdNumber();

                if ((name != null && name.toLowerCase().contains(lowercaseQuery)) ||
                        (id != null && id.toLowerCase().contains(lowercaseQuery))) {
                    filteredList.add(patient);
                }
            }

            this.patients = filteredList;
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView patientName;
        TextView patientId;
        TextView registrationDate;

        ViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patient_name);
            patientId = itemView.findViewById(R.id.patient_id);
            registrationDate = itemView.findViewById(R.id.registration_date);
        }
    }
}
