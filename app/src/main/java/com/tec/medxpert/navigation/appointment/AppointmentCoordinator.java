package com.tec.medxpert.navigation.appointment;

import android.app.Activity;

public interface AppointmentCoordinator {

    void navigateToBookAppointment();

    void navigateToListAppointments();

    void navigateToDiagnostic(Activity activity, int requestCode, String appointmentId, String patientId, String patientName);

    void navigateToDiagnosticInformation(Activity activity, String documentId);
}
