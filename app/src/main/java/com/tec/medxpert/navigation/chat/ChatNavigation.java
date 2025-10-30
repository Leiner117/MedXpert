package com.tec.medxpert.navigation.chat;

public interface ChatNavigation {
    void navigateToDoctorProfile(String doctorUid, String doctorName);
    void navigateToPatientProfile(String patientUid, String patientName);
}