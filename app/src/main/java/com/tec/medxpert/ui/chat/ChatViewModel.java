package com.tec.medxpert.ui.chat;

import androidx.lifecycle.ViewModel;

import com.tec.medxpert.navigation.chat.ChatNavigation;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatViewModel extends ViewModel {
    private final ChatNavigation navigation;

    @Inject
    public ChatViewModel(ChatNavigation navigation) {
        this.navigation = navigation;
    }

    public void onDoctorNameClicked(String doctorUid, String doctorName) {
        navigation.navigateToDoctorProfile(doctorUid, doctorName);
    }

    public void onPatientNameClicked(String patientUid, String patientName) {
        navigation.navigateToPatientProfile(patientUid, patientName);
    }
}