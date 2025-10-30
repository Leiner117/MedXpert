package com.tec.medxpert.navigation.addMedicationCoordinator;

import android.content.Context;
import android.content.Intent;

import com.tec.medxpert.ui.viewMedication.ViewMedicationActivity;

import javax.inject.Inject;

public class AddMedicationCoordinator implements IAddMedicationCoordinator {
    @Inject
    public AddMedicationCoordinator() {
    }
    @Override
    public void navigateToViewMedication(Context context) {
        if (context instanceof ViewMedicationActivity) {
            ((ViewMedicationActivity) context).finish();
        }
    }
}
