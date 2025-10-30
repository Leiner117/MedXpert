package com.tec.medxpert.ui.profile.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.tec.medxpert.R;

import dagger.hilt.android.AndroidEntryPoint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;
import com.tec.medxpert.ui.profile.patient.viewmodel.ProfileViewModel;
import com.tec.medxpert.ui.profile.patient.viewmodel.SharedPatientViewModel;

import com.tec.medxpert.R;
import com.tec.medxpert.navigation.profile.IProfileCoordinator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterPatientFragment extends Fragment {

    @Inject
    IProfileCoordinator profileCoordinator;

    private TextView profileTab;
    private TextView historyTab;
    private View tabIndicator;

    private ProfileViewModel profileViewModel;
    private SharedPatientViewModel sharedPatientViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        sharedPatientViewModel = new ViewModelProvider(requireActivity()).get(SharedPatientViewModel.class);

        // Observe patient ID from ProfileViewModel and share it
        profileViewModel.getPatientData().observe(this, resource -> {
            if (resource.status == com.tec.medxpert.util.Resource.Status.SUCCESS && resource.data != null) {
                sharedPatientViewModel.setPatientId(resource.data.getPatientId());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_patient, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        profileTab = view.findViewById(R.id.profile_tab);
        historyTab = view.findViewById(R.id.history_tab);
        tabIndicator = view.findViewById(R.id.tab_indicator);

        // Configure listeners for tabs
        profileTab.setOnClickListener(v -> selectTab(0));
        historyTab.setOnClickListener(v -> selectTab(1));

        // Show the first fragment by default
        if (savedInstanceState == null) {
            selectTab(0);
        }
    }

    private void selectTab(int tabIndex) {
        // Update the style of the selected tab and the indicator
        if (tabIndex == 0) {
            profileTab.setTextColor(getResources().getColor(R.color.blue));
            historyTab.setTextColor(getResources().getColor(R.color.gray));
            moveIndicator(profileTab);
            profileCoordinator.navigateToProfileTab(this);
        } else {
            profileTab.setTextColor(getResources().getColor(R.color.gray));
            historyTab.setTextColor(getResources().getColor(R.color.blue));
            moveIndicator(historyTab);
            profileCoordinator.navigateToHistoryTab(this);
        }
    }

    private void moveIndicator(View tab) {
        ConstraintLayout mainLayout = getView().findViewById(R.id.main);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mainLayout);

        constraintSet.connect(
                tabIndicator.getId(),
                ConstraintSet.START,
                tab.getId(),
                ConstraintSet.START
        );

        constraintSet.connect(
                tabIndicator.getId(),
                ConstraintSet.END,
                tab.getId(),
                ConstraintSet.END
        );

        constraintSet.applyTo(mainLayout);
    }
}