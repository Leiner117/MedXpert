package com.tec.medxpert.ui.profile.patient;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tec.medxpert.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PatientListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PatientListFragment())
                    .commit();
        }
    }
}
