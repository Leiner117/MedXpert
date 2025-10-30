package com.tec.medxpert.ui.codeSecurity;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;

import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class VerificationCodeViewModel extends ViewModel {

    private final SharedPreferences sharedPreferences;

    @Inject
    public VerificationCodeViewModel(Application application) {
        sharedPreferences = application.getSharedPreferences("MedXpertPrefs", Context.MODE_PRIVATE);
    }
}

