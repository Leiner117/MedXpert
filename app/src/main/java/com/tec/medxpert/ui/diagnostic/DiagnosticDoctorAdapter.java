package com.tec.medxpert.ui.diagnostic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Diagnostic;
import com.tec.medxpert.navigation.diagnostic.DiagnosticCoordinator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiagnosticDoctorAdapter extends RecyclerView.Adapter<DiagnosticDoctorAdapter.ViewHolder> {

    private List<Diagnostic> diagnostics;

    private DiagnosticCoordinator coordinator;

    public DiagnosticDoctorAdapter(List<Diagnostic> diagnostic, DiagnosticCoordinator coordinator) {
        this.diagnostics = diagnostic;
        this.coordinator = coordinator;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diagnostic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Diagnostic diagnostic = diagnostics.get(position);
        holder.patientNameTextView.setText(diagnostic.getPatientName());
        holder.idTextView.setText(diagnostic.getIdNumber());

        // Date formatting
        SimpleDateFormat formatter = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        Date date = diagnostic.getUpdatedAt().toDate();
        String formattedDate = formatter.format(date);

        holder.dateTextView.setText(formattedDate);

        holder.itemView.setOnClickListener(v -> {
            if (coordinator != null) {
                coordinator.navigateToDiagnosticDetails(
                        diagnostic.getPatientId()
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return diagnostics.size();
    }

    public void updateDiagnostics(List<Diagnostic> newDiagnostic2s) {
        this.diagnostics = newDiagnostic2s;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView patientNameTextView, idTextView, dateTextView;
        ImageView chevronIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            patientNameTextView = itemView.findViewById(R.id.patientNameTextView);
            idTextView = itemView.findViewById(R.id.patientIdTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            chevronIcon = itemView.findViewById(R.id.chevronIcon);
        }
    }
}
