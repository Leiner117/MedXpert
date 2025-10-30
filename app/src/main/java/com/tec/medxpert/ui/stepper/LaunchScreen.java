package com.tec.medxpert.ui.stepper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.navigation.stepper.StepperCoordinator;
import com.tec.medxpert.R;
import com.tec.medxpert.navigation.authentication.AuthenticationNavigator;
import com.tec.medxpert.navigation.authentication.AuthenticationCoordinator;

public class LaunchScreen extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000;
    private StepperCoordinator coordinator;
    private AuthenticationNavigator authenticationCoordinator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.launch_screen);
        coordinator = new StepperCoordinator(this);
        authenticationCoordinator = new AuthenticationCoordinator(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferenceslogin = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean rememberMe = sharedPreferenceslogin.getBoolean("rememberMe", false);
        boolean isLoggedIn = sharedPreferenceslogin.getBoolean("isLoggedIn", false);

        if (rememberMe && isLoggedIn) {
            // Vericate if the user is active
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean isActive = "true".equalsIgnoreCase(documentSnapshot.getString("isActive"));
                        if (isActive) {
                            String role = documentSnapshot.getString("role");
                            if ("doctor".equalsIgnoreCase(role)) {
                                authenticationCoordinator.navigateToHomeDoctor();
                            } else {
                                authenticationCoordinator.navigateToHomePatient();
                            }
                            finish();
                        } else {
                            SharedPreferences.Editor editor = sharedPreferenceslogin.edit();
                            editor.putBoolean("isLoggedIn", false);
                            editor.apply();
                            authenticationCoordinator.navigateToLogin();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    authenticationCoordinator.navigateToLogin();
                    finish();
                });
                return;
            }
        }

        SharedPreferences sharedPreferences = getSharedPreferences("stepper_prefs", MODE_PRIVATE);
        boolean alreadyShown = sharedPreferences.getBoolean("stepper_shown", false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (alreadyShown) {
                authenticationCoordinator.navigateToLogin();
                finish();
            } else {
                coordinator.navigateToWelcomeStepper();
                finish();
            }
            finish();
        }, SPLASH_DELAY);


        new Handler(Looper.getMainLooper()).postDelayed(() -> {

        }, SPLASH_DELAY);
    }
}