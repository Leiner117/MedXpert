package com.tec.medxpert.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tec.medxpert.R;
import com.tec.medxpert.navigation.patientsHome.PatientsHomeCoordinator;
import com.tec.medxpert.navigation.patientsHome.PatientsHomeInterface;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for displaying the patient home screen with navigation buttons
 */
@AndroidEntryPoint
public class PatientHomeFragment extends Fragment {

    private PatientsHomeInterface coordinator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize coordinator
        coordinator = new PatientsHomeCoordinator(requireContext());

        // Set up button click listeners
        view.findViewById(R.id.appointmentsButton).setOnClickListener(v -> {
            coordinator.navigateToAppointments();
        });

        view.findViewById(R.id.diagnosesButton).setOnClickListener(v -> {
            coordinator.navigateToDiagnoses();
        });

        view.findViewById(R.id.medicationsButton).setOnClickListener(v -> {
            coordinator.navigateToMedications();
        });
    }
}
