package com.tec.medxpert.navigation.diagnostic;

import android.content.Context;
import android.content.Intent;

import com.tec.medxpert.ui.diagnosticInformation.DiagnosticInformationActivity;

import javax.inject.Inject;

public class DiagnosticCoordinator implements DiagnosticNavigation {

    private Context context;

    @Inject
    public DiagnosticCoordinator() {
        // Constructor injection
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void navigateToDiagnosticDetails(String diagnosticId) {
        if (context != null) {
            Intent intent = new Intent(context, DiagnosticInformationActivity.class);
            intent.putExtra("diagnosticId", diagnosticId);

            context.startActivity(intent);
        }
    }
}
