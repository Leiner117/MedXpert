package com.tec.medxpert.navigation.stepper;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.tec.medxpert.R;
import com.tec.medxpert.ui.home.DoctorFragment;
import com.tec.medxpert.ui.home.PatientHomeFragment;
import com.tec.medxpert.ui.stepper.WelcomeStepper;
import com.tec.medxpert.ui.stepper.DoctorStepper;
import com.tec.medxpert.ui.stepper.ConsultationApproachStepper;

public class StepperCoordinator implements StepperCoordinatorInterface {
    private final Context context;

    public StepperCoordinator(Context context) {
        this.context = context;
    }

    @Override
    public void navigateToWelcomeStepper() {
        Intent intent = new Intent(context, WelcomeStepper.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToDoctorStepper() {
        Intent intent = new Intent(context, DoctorStepper.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToConsultationApproachStepper() {
        Intent intent = new Intent(context, ConsultationApproachStepper.class);
        context.startActivity(intent);
    }

    @Override
    public void navigateToLogin() {
        // Implementation for navigating to login
    }

    @Override
    public void navigateToDoctorHome() {
        // Implement navigation to Doctor Home
        DoctorFragment doctorFragment = new DoctorFragment();
        ((AppCompatActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, doctorFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateToPatientHome() {
        // Implement navigation to Patient Home
        PatientHomeFragment patientHomeFragment = new PatientHomeFragment();
        ((AppCompatActivity) context).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, patientHomeFragment)
                .addToBackStack(null)
                .commit();
    }
}