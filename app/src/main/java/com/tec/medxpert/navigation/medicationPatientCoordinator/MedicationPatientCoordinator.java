package com.tec.medxpert.navigation.medicationPatientCoordinator;

import android.app.Activity;

import javax.inject.Inject;

public class MedicationPatientCoordinator implements IMedicationPatientCoordinator{
    private final Activity activity;

    @Inject
    public MedicationPatientCoordinator(Activity activity) {
        this.activity = activity;
    }
    @Override
    public void navigateToMain() {
        activity.finish();
    }
}
