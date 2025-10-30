package com.tec.medxpert.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tec.medxpert.R;
import com.tec.medxpert.navigation.doctorHome.DoctorHomeCoordinator;

import dagger.hilt.android.AndroidEntryPoint;
import com.tec.medxpert.data.repository.Authentication;
import com.tec.medxpert.navigation.authentication.AuthenticationCoordinator;
/**
 * Fragment for displaying the doctor home screen with navigation buttons
 */
@AndroidEntryPoint
public class DoctorFragment extends Fragment {

    private DoctorHomeCoordinator coordinator;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize coordinator
        coordinator = new DoctorHomeCoordinator(requireContext());

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

        view.findViewById(R.id.patientsButton).setOnClickListener(v -> {
            coordinator.navigateToPatients();
        });

        view.findViewById(R.id.scheduleButton).setOnClickListener(v -> {
            coordinator.navigateToSchedule();
        });







    }


}