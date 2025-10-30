package com.tec.medxpert.navigation.viewMedicationCoordinator;

import android.content.Context;

import com.tec.medxpert.data.model.Medication;

public interface IViewMedicationCoordinator {
    void navigateToMain();
    void navigateToAddMedication(Context context, Medication medication);
}
