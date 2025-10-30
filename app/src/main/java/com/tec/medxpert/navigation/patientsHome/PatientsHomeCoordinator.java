package com.tec.medxpert.navigation.patientsHome;

import android.content.Context;
import android.content.Intent;

import com.tec.medxpert.ui.ViewMedicationPatient.ViewMedicationPatientActivity;
import com.tec.medxpert.ui.appointment.ListAppointmentsActivity;
import com.tec.medxpert.ui.diagnostic.DiagnosticPatientActivity;

public class PatientsHomeCoordinator implements PatientsHomeInterface{
    private final Context context;
    public PatientsHomeCoordinator(Context context) {
        this.context = context;
    }

    @Override
    public void navigateToAppointments() {
        // Implement navigation to appointments
        Intent intent = new Intent(context, ListAppointmentsActivity.class);
        context.startActivity(intent);

    }

    @Override
    public void navigateToDiagnoses() {
        Intent intent = new Intent(context, DiagnosticPatientActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToMedications() {
        Intent intent = new Intent(context, ViewMedicationPatientActivity.class);
        context.startActivity(intent);
    }
}
