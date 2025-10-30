package com.tec.medxpert.ui.stepper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tec.medxpert.R;
import com.tec.medxpert.navigation.stepper.StepperCoordinator;
import com.tec.medxpert.ui.stepper.adapter.AccordionAdapter;
import com.tec.medxpert.ui.stepper.model.AccordionItem;
import com.tec.medxpert.navigation.authentication.AuthenticationNavigator;
import com.tec.medxpert.navigation.authentication.AuthenticationCoordinator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsultationApproachStepper extends AppCompatActivity {
    private StepperCoordinator coordinator;
    private AuthenticationNavigator AuthenticationCoordinator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_consultation_approach_stepper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        List<AccordionItem> items = Arrays.asList(
                new AccordionItem(getString(R.string.general_consultation), getString(R.string.general_consultation_description)),
                new AccordionItem(getString(R.string.pediatric_consultation), getString(R.string.pediatric_consultation_description)),
                new AccordionItem(getString(R.string.gynecological_consultation), getString(R.string.gynecological_consultation_description)),
                new AccordionItem(getString(R.string.gynecological_consultation), getString(R.string.gynecological_consultation_description)),
                new AccordionItem(getString(R.string.internal_medicine_consultation), getString(R.string.internal_medicine_consultation_description)),
                new AccordionItem(getString(R.string.general_consultation), getString(R.string.general_consultation_description)),
                new AccordionItem(getString(R.string.emergency_medicine_consultation), getString(R.string.emergency_medicine_consultation_description)),
                new AccordionItem(getString(R.string.sports_medicine_consultation), getString(R.string.sports_medicine_consultation_description)),
                new AccordionItem(getString(R.string.endocannabinoid_medicine_consultation), getString(R.string.endocannabinoid_medicine_consultation_description)),
                new AccordionItem(getString(R.string.diabetes_education_consultation), getString(R.string.diabetes_education_consultation_description))
        );
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AccordionAdapter(items));
        coordinator = new StepperCoordinator(this);
        AuthenticationCoordinator = new AuthenticationCoordinator(this);
        Button nextButton = findViewById(R.id.nextButton);
        Button skipButton = findViewById(R.id.skipButton);
        Button previousButton = findViewById(R.id.previousButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("stepper_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("stepper_shown", true);
                editor.apply();
                AuthenticationCoordinator.navigateToLogin();
                finish();
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
                finish();
            }

        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}