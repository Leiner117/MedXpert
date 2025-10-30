package com.tec.medxpert.ui.profile.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for sharing patient data between fragments
 */
@HiltViewModel
public class SharedPatientViewModel extends ViewModel {

    private final MutableLiveData<String> patientId = new MutableLiveData<>();

    @Inject
    public SharedPatientViewModel() {
    }

    public void setPatientId(String id) {
        patientId.setValue(id);
    }

    public LiveData<String> getPatientId() {
        return patientId;
    }
}