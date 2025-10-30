package com.tec.medxpert.navigation.chat;

import android.content.Context;
import android.content.Intent;

import com.tec.medxpert.ui.chat_profile.DoctorChatProfileActivity;
import com.tec.medxpert.ui.chat_profile.PatientChatProfileActivity;

public class ChatCoordinator implements ChatNavigation {
    private final Context context;

    public ChatCoordinator(Context context) {
        this.context = context;
    }

    @Override
    public void navigateToDoctorProfile(String doctorUid, String doctorName) {
        Intent intent = new Intent(context, DoctorChatProfileActivity.class);
        intent.putExtra("userId", doctorUid);
        intent.putExtra("doctorName", doctorName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void navigateToPatientProfile(String patientUid, String patientName) {
        Intent intent = new Intent(context, PatientChatProfileActivity.class);
        intent.putExtra("userId", patientUid);
        intent.putExtra("patientName", patientName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}