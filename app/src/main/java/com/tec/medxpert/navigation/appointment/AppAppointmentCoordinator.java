package com.tec.medxpert.navigation.appointment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.tec.medxpert.ui.addDiagnostic.AddDiagnosticActivity;
import com.tec.medxpert.ui.appointment.BookAppointmentActivity;
import com.tec.medxpert.ui.appointment.ListAppointmentsActivity;
import com.tec.medxpert.ui.diagnosticInformation.DiagnosticInformationActivity;

public class AppAppointmentCoordinator implements AppointmentCoordinator {

    private final Context context;

    public AppAppointmentCoordinator(Context context) {
        this.context = context;
    }

    @Override
    public void navigateToBookAppointment() {
        Intent intent = new Intent(context, BookAppointmentActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToListAppointments() {
        Intent intent = new Intent(context, ListAppointmentsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToDiagnostic(Activity activity, int requestCode,String appointmentId, String patientId, String patientName) {
        Intent intent = new Intent(activity, AddDiagnosticActivity.class);
        intent.putExtra("appointmentId", appointmentId);
        intent.putExtra("patientId", patientId);
        intent.putExtra("patientName", patientName);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void navigateToDiagnosticInformation(Activity activity, String documentId) {
        Intent intent = new Intent(activity, DiagnosticInformationActivity.class);
        intent.putExtra("documentId", documentId);
        activity.startActivity(intent);
    }


}