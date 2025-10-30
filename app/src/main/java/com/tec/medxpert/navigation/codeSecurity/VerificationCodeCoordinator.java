package com.tec.medxpert.navigation.codeSecurity;


import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.tec.medxpert.R;
import com.tec.medxpert.ui.codeSecurity.VerificationCodeActivity;
import com.tec.medxpert.ui.home.PatientHomeFragment;


public class VerificationCodeCoordinator implements VerificationCodeNavigator {

    private final Activity activity;

    public VerificationCodeCoordinator(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void navigateToVerificationCode() {
        Intent intent = new Intent(activity, VerificationCodeActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public void navigateToHomePatient() {
        PatientHomeFragment patientHomeFragment = new PatientHomeFragment();
        ((AppCompatActivity) activity).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, patientHomeFragment)
                .addToBackStack(null)
                .commit();
    }
}

