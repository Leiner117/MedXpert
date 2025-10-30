package com.tec.medxpert.ui.profile.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tec.medxpert.R;
import com.tec.medxpert.data.repository.Authentication;
import com.tec.medxpert.navigation.authentication.AuthenticationCoordinator;
import com.tec.medxpert.navigation.profile.IProfileCoordinator;
import com.tec.medxpert.util.Resource;

import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.data.model.profile.PersonalData;
import com.tec.medxpert.ui.profile.patient.viewmodel.ProfileViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment for displaying and editing patient profile
 */
@AndroidEntryPoint
public class PatientProfileFragment extends Fragment {

    @Inject
    IProfileCoordinator profileCoordinator;

    private ProfileViewModel viewModel;

    // TextViews for the values
    private TextView idTypeValue;
    private TextView idValue;
    private TextView nameValue;
    private TextView phoneValue; // Renamed from emailValue to phoneValue
    private TextView bloodTypeValue;
    private TextView weightValue;
    private TextView heightValue;
    private TextView allergiesValue;
    private TextView personalMedicalHistoryValue;
    private TextView familyMedicalHistoryValue;


    // Loading view
    private View loadingView;
    private Button registerButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        idTypeValue = view.findViewById(R.id.idTypeValue);
        idValue = view.findViewById(R.id.idValue);
        nameValue = view.findViewById(R.id.nameValue);
        phoneValue = view.findViewById(R.id.emailValue); // The view ID is still emailValue
        bloodTypeValue = view.findViewById(R.id.bloodTypeValue);
        weightValue = view.findViewById(R.id.weightValue);
        heightValue = view.findViewById(R.id.heightValue);
        allergiesValue = view.findViewById(R.id.allergiesValue);
        personalMedicalHistoryValue = view.findViewById(R.id.personalMedicalHistoryValue);
        familyMedicalHistoryValue = view.findViewById(R.id.familyMedicalHistoryValue);

        loadingView = view.findViewById(R.id.loadingView);
        registerButton = view.findViewById(R.id.registerButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        Button deactivateAccountButton = view.findViewById(R.id.deactivateAccountButton);
        // Set up click listeners
        setupClickableFields(view);

        // Set up register button
        registerButton.setOnClickListener(v -> savePatientData());
        logoutButton.setOnClickListener(v -> logoutUser());
        deactivateAccountButton.setOnClickListener(v -> deactivateUser());
        // Observe patient data
        observeViewModel();
    }
    private void logoutUser() {
        Authentication auth = new Authentication();
        auth.logout(requireActivity());

        AuthenticationCoordinator loginCoordinator = new AuthenticationCoordinator(requireActivity());
        loginCoordinator.navigateToLogin();
    }
    private void deactivateUser() {
        Authentication auth = new Authentication();
        auth.deactivateAccount(requireActivity());

        AuthenticationCoordinator loginCoordinator = new AuthenticationCoordinator(requireActivity());
        loginCoordinator.navigateToLogin();
    }

    private void observeViewModel() {
        viewModel.getPatientData().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else {
                showLoading(false);

                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    updateUI(resource.data);
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.LOADING) {
                showLoading(true);
            } else {
                showLoading(false);

                if (resource.status == Resource.Status.SUCCESS && Boolean.TRUE.equals(resource.data)) {
                    Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Patient patient) {
        PersonalData personalData = patient.getPersonalData();

        // Update UI with patient data
        idTypeValue.setText(personalData.getIdType() != null ? personalData.getIdType() : "");
        idValue.setText(personalData.getIdNumber() != null ? personalData.getIdNumber() : "");
        nameValue.setText(personalData.getName() != null ? personalData.getName() : "");
        phoneValue.setText(personalData.getPhone() != null ? personalData.getPhone() : "");
        bloodTypeValue.setText(personalData.getBloodType() != null ? personalData.getBloodType() : "");

        if (personalData.getWeight() != null) {
            weightValue.setText(String.valueOf(personalData.getWeight()));
        } else {
            weightValue.setText("");
        }

        if (personalData.getHeight() != null) {
            heightValue.setText(String.valueOf(personalData.getHeight()));
        } else {
            heightValue.setText("");
        }

        // Update allergies count
        int allergiesCount = personalData.getAllergies() != null ? personalData.getAllergies().size() : 0;
        allergiesValue.setText(allergiesCount > 0 ? allergiesCount + " allergies" : "No allergies");

        // Update personal medical history count
        int personalHistoryCount = personalData.getPersonalMedicalHistory() != null ?
                personalData.getPersonalMedicalHistory().size() : 0;
        personalMedicalHistoryValue.setText(personalHistoryCount > 0 ?
                personalHistoryCount + " conditions" : "No conditions");

        // Update family medical history count
        int familyHistoryCount = personalData.getFamilyMedicalHistory() != null ?
                personalData.getFamilyMedicalHistory().size() : 0;
        familyMedicalHistoryValue.setText(familyHistoryCount > 0 ?
                familyHistoryCount + " conditions" : "No conditions");
    }

    private void setupClickableFields(View view) {
        // ID Type
        view.findViewById(R.id.idTypeCard).setOnClickListener(v -> {
            profileCoordinator.showIdTypeSelectionDialog(
                    getContext(),
                    idTypeValue.getText().toString(),
                    idType -> {
                        idTypeValue.setText(idType);
                        viewModel.updateIdType(idType);
                    }
            );
        });

        // ID
        view.findViewById(R.id.idCard).setOnClickListener(v -> {
            profileCoordinator.showEditTextDialog(
                    getContext(),
                    "Enter ID",
                    idValue.getText().toString(),
                    text -> {
                        idValue.setText(text);
                        viewModel.updateIdNumber(text);
                    }
            );
        });

        // Name
        view.findViewById(R.id.nameCard).setOnClickListener(v -> {
            profileCoordinator.showEditTextDialog(
                    getContext(),
                    "Enter Name",
                    nameValue.getText().toString(),
                    text -> {
                        nameValue.setText(text);
                        viewModel.updateName(text);
                    }
            );
        });

        // Phone (previously Email)
        view.findViewById(R.id.emailCard).setOnClickListener(v -> {
            profileCoordinator.showEditTextDialog(
                    getContext(),
                    "Enter Phone",
                    phoneValue.getText().toString(),
                    text -> {
                        phoneValue.setText(text);
                        viewModel.updatePhone(text);
                    }
            );
        });

        // Blood Type
        view.findViewById(R.id.bloodTypeCard).setOnClickListener(v -> {
            profileCoordinator.showBloodTypeSelectionDialog(
                    getContext(),
                    bloodTypeValue.getText().toString(),
                    bloodType -> {
                        bloodTypeValue.setText(bloodType);
                        viewModel.updateBloodType(bloodType);
                    }
            );
        });

        // Weight
        view.findViewById(R.id.weightCard).setOnClickListener(v -> {
            Double currentWeight = null;
            try {
                if (!weightValue.getText().toString().isEmpty()) {
                    currentWeight = Double.parseDouble(weightValue.getText().toString());
                }
            } catch (NumberFormatException e) {
                // Ignore
            }

            profileCoordinator.showNumberEditDialog(
                    getContext(),
                    "Enter Weight (kg)",
                    currentWeight,
                    weight -> {
                        if (weight != null) {
                            weightValue.setText(String.valueOf(weight));
                            viewModel.updateWeight(weight);
                        } else {
                            weightValue.setText("");
                        }
                    }
            );
        });

        // Height
        view.findViewById(R.id.heightCard).setOnClickListener(v -> {
            Double currentHeight = null;
            try {
                if (!heightValue.getText().toString().isEmpty()) {
                    currentHeight = Double.parseDouble(heightValue.getText().toString());
                }
            } catch (NumberFormatException e) {
                // Ignore
            }

            profileCoordinator.showNumberEditDialog(
                    getContext(),
                    "Enter Height (cm)",
                    currentHeight,
                    height -> {
                        if (height != null) {
                            heightValue.setText(String.valueOf(height));
                            viewModel.updateHeight(height);
                        } else {
                            heightValue.setText("");
                        }
                    }
            );
        });

        // Allergies
        view.findViewById(R.id.allergiesCard).setOnClickListener(v -> {
            profileCoordinator.navigateToAllergiesScreen(this);
        });

        // Personal Medical History
        view.findViewById(R.id.personalMedicalHistoryCard).setOnClickListener(v -> {
            profileCoordinator.navigateToPersonalMedicalHistory(this);
        });

        // Family Medical History
        view.findViewById(R.id.familyMedicalHistoryCard).setOnClickListener(v -> {
            profileCoordinator.navigateToFamilyMedicalHistory(this);
        });
    }

    private void savePatientData() {
        // Create a new PersonalData object with all the current values
        PersonalData personalData = new PersonalData();
        personalData.setIdType(idTypeValue.getText().toString());
        personalData.setIdNumber(idValue.getText().toString());
        personalData.setName(nameValue.getText().toString());
        personalData.setPhone(phoneValue.getText().toString());
        personalData.setBloodType(bloodTypeValue.getText().toString());

        try {
            if (!weightValue.getText().toString().isEmpty()) {
                personalData.setWeight(Double.parseDouble(weightValue.getText().toString()));
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        try {
            if (!heightValue.getText().toString().isEmpty()) {
                personalData.setHeight(Double.parseDouble(heightValue.getText().toString()));
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        // Save all data at once
        viewModel.saveAllPatientData(personalData);
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (registerButton != null) {
            registerButton.setEnabled(!isLoading);
        }
    }
}
