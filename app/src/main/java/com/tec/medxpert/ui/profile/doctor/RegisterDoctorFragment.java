package com.tec.medxpert.ui.profile.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tec.medxpert.R;
import com.tec.medxpert.navigation.profile.IProfileCoordinator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterDoctorFragment extends Fragment {

    @Inject
    IProfileCoordinator profileCoordinator;

    private TextView titleTextView;
    private DoctorViewModel doctorViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doctorViewModel = new ViewModelProvider(this).get(DoctorViewModel.class);

        // Observe doctor data from DoctorViewModel
        doctorViewModel.getDoctor().observe(this, doctor -> {
            if (doctor != null) {
                // Doctor data loaded successfully
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_doctor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        titleTextView = view.findViewById(R.id.title);

        // Load doctor profile fragment by default
        if (savedInstanceState == null) {
            loadDoctorProfileFragment();
        }
    }

    private void loadDoctorProfileFragment() {
        // Create and add the DoctorProfileFragment directly
        DoctorProfileFragment fragment = new DoctorProfileFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}