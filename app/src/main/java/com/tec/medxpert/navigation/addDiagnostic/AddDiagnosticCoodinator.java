package com.tec.medxpert.navigation.addDiagnostic;

import android.content.Context;

import com.tec.medxpert.ui.viewMedication.ViewMedicationActivity;

import javax.inject.Inject;

public class AddDiagnosticCoodinator implements IAddDiagnosticCoordinator {

    @Inject
    public AddDiagnosticCoodinator() {
    }

    @Override
    public void navigateToBack(Context context) {
        if (context instanceof ViewMedicationActivity) {
            ((ViewMedicationActivity) context).finish();
        }
    }
}
