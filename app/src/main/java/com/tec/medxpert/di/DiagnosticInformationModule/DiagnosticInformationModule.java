package com.tec.medxpert.di.DiagnosticInformationModule;

import android.app.Activity;

import com.tec.medxpert.navigation.diagnosticInformation.DiagnosticInformationCoordinator;
import com.tec.medxpert.navigation.diagnosticInformation.DiagnosticInformationNavigator;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;

@Module
@InstallIn(ActivityComponent.class)
public class DiagnosticInformationModule {

    @Provides
    public DiagnosticInformationNavigator provideDiagnosticInformationCoordinator(Activity activity) {
        return new DiagnosticInformationCoordinator(activity);
    }
}
