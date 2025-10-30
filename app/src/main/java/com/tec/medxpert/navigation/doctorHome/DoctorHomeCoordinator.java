package com.tec.medxpert.navigation.doctorHome;

import android.content.Context;
import android.content.Intent;

import com.tec.medxpert.ui.appointment.DoctorAppointmentsActivity;
import com.tec.medxpert.ui.availability.AddAvailabilityActivity;
import com.tec.medxpert.ui.diagnostic.DiagnosticDoctorActivity;
import com.tec.medxpert.ui.profile.patient.PatientListActivity;
import com.tec.medxpert.ui.viewMedication.ViewMedicationActivity;

public class DoctorHomeCoordinator implements DoctorHomeCoordinatorInterface {
    private final Context context;
    public DoctorHomeCoordinator(Context context) {
        this.context = context;
    }

    @Override
    public void navigateToAppointments() {
        Intent intent = new Intent(context, DoctorAppointmentsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToDiagnoses() {
        Intent intent = new Intent(context, DiagnosticDoctorActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToMedications() {
        Intent intent = new Intent(context, ViewMedicationActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToPatients() {
        Intent intent = new Intent(context, PatientListActivity.class);
        context.startActivity(intent);
    }
    public void navigateToSchedule() {
        // Implement navigation to Schedule
        Intent Intent = new Intent(context, AddAvailabilityActivity.class);
        context.startActivity(Intent);
    }
}
