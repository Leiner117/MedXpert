package com.tec.medxpert.navigation.diagnosticInformation;

import android.app.Activity;
import android.content.Intent;

import com.tec.medxpert.ui.diagnosticInformation.DiagnosticInformationActivity;

import javax.inject.Inject;

public class DiagnosticInformationCoordinator implements DiagnosticInformationNavigator {
    private final Activity activity;

    @Inject
    public DiagnosticInformationCoordinator(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void navigateToPreviousScreen() {
        activity.finish();
    }


}