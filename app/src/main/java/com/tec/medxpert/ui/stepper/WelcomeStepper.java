package com.tec.medxpert.ui.stepper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.tec.medxpert.navigation.authentication.AuthenticationCoordinator;
import com.tec.medxpert.navigation.stepper.StepperCoordinator;
import com.tec.medxpert.R;

public class WelcomeStepper extends AppCompatActivity {
    private StepperCoordinator coordinator;
    private AuthenticationCoordinator AuthenticationCoordinator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_stepper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AuthenticationCoordinator = new AuthenticationCoordinator(this);
        coordinator = new StepperCoordinator(this);
        Button nextButton = findViewById(R.id.nextButton);
        Button skipButton = findViewById(R.id.skipButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coordinator.navigateToDoctorStepper();
            }
        });
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("stepper_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("stepper_shown", true);
                editor.apply();
                AuthenticationCoordinator.navigateToLogin();
            }
        });



    }
}