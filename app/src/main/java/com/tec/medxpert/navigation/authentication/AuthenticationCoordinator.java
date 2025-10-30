package com.tec.medxpert.navigation.authentication;

import android.app.Activity;
import android.content.Intent;

import com.tec.medxpert.MainApplication.MainActivity;
import com.tec.medxpert.ui.login.LoginActivity;
import com.tec.medxpert.ui.register.RegisterActivity;

public class AuthenticationCoordinator implements AuthenticationNavigator {
    private final Activity activity;

    public AuthenticationCoordinator(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void navigateToRegister() {
        Intent intent = new Intent(activity, RegisterActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public void navigateToHomeDoctor() {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("role", "doctor");
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    public void navigateToHomePatient() {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("role", "patient");
        activity.startActivity(intent);
        activity.finish();
    }
}