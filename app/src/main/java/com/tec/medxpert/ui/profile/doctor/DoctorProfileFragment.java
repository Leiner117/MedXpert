package com.tec.medxpert.ui.profile.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.tec.medxpert.R;
import com.tec.medxpert.data.repository.Authentication;
import com.tec.medxpert.navigation.authentication.AuthenticationCoordinator;
import com.tec.medxpert.navigation.profile.IProfileCoordinator;
import com.tec.medxpert.data.model.profile.Doctor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DoctorProfileFragment extends Fragment {

    @Inject
    IProfileCoordinator profileCoordinator;

    private DoctorViewModel viewModel;

    // TextViews for the values
    private TextView nameValueTextView;
    private TextView identificationValueTextView;
    private TextView phoneValueTextView;
    private TextView emailValueTextView;
    private TextView medicalCodeValueTextView;
    private TextView consultationFocusValueTextView;
    private TextView specialtiesValueTextView;
    private LinearLayout educationContainer;
    private LinearLayout workExperienceContainer;
    private LinearLayout specialtiesContainer;
    private TextView noEducationText;
    private TextView noWorkExperienceText;
    private TextView noSpecialtiesText;
    private Button addSpecialtyButton;

    // Loading view
    private View loadingView;
    private Button saveButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DoctorViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_doctor_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        nameValueTextView = view.findViewById(R.id.nameValue);
        identificationValueTextView = view.findViewById(R.id.identificationValue);
        phoneValueTextView = view.findViewById(R.id.phoneValue);
        emailValueTextView = view.findViewById(R.id.emailValue);
        medicalCodeValueTextView = view.findViewById(R.id.medicalCodeValue);
        specialtiesValueTextView = view.findViewById(R.id.specialtiesValue);
        consultationFocusValueTextView = view.findViewById(R.id.consultationFocusValue);
        educationContainer = view.findViewById(R.id.educationContainer);
        workExperienceContainer = view.findViewById(R.id.workExperienceContainer);
        specialtiesContainer = view.findViewById(R.id.specialtiesContainer);
        noEducationText = view.findViewById(R.id.noEducationText);
        noWorkExperienceText = view.findViewById(R.id.noWorkExperienceText);
        noSpecialtiesText = view.findViewById(R.id.noSpecialtiesText);
        addSpecialtyButton = view.findViewById(R.id.addSpecialtyButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        Button deactivateAccountButton = view.findViewById(R.id.deactivateAccountButton);
        saveButton = view.findViewById(R.id.saveButton);
        loadingView = view.findViewById(R.id.loadingView);

        logoutButton.setOnClickListener(v -> logoutUser());
        deactivateAccountButton.setOnClickListener(v -> deactivateUser());

        // Set up click listeners
        setupClickableFields(view);

        // Set up save button
        saveButton.setOnClickListener(v -> saveDoctorData());

        // Set up add specialty button
        addSpecialtyButton.setOnClickListener(v -> {
            showAddSpecialtyDialog(specialty -> {
                Doctor doctor = viewModel.getDoctor().getValue();
                if (doctor != null) {
                    viewModel.addSpecialty(specialty);
                }
            });
        });

        // Observe doctor data
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
        viewModel.getDoctor().observe(getViewLifecycleOwner(), doctor -> {
            if (doctor != null) {
                updateUI(doctor);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            showLoading(isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(Doctor doctor) {
        if (doctor == null) return;

        // Update name and other fields
        nameValueTextView.setText(doctor.getName() != null ? doctor.getName() : "");
        identificationValueTextView.setText(doctor.getIdentification() != null ? doctor.getIdentification() : "");
        phoneValueTextView.setText(doctor.getPhone() != null ? doctor.getPhone() : "");
        emailValueTextView.setText(doctor.getEmail() != null ? doctor.getEmail() : "");
        medicalCodeValueTextView.setText(doctor.getMedicalCode() != null ? doctor.getMedicalCode() : "");
        consultationFocusValueTextView.setText(doctor.getConsultationFocus() != null ? doctor.getConsultationFocus() : "");

        // Update specialties
        updateSpecialtiesUI(doctor.getSpecialties());

        // Update education
        updateEducationUI(doctor.getEducation());

        // Update work experience
        updateWorkExperienceUI(doctor.getWorkExperience());
    }

    private void updateSpecialtiesUI(List<String> specialties) {
        // Clear the container
        if (specialtiesContainer != null) {
            specialtiesContainer.removeAllViews();
        }

        if (specialties == null || specialties.isEmpty()) {
            if (noSpecialtiesText != null) {
                noSpecialtiesText.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (noSpecialtiesText != null) {
            noSpecialtiesText.setVisibility(View.GONE);
        }

        // Add each specialty as a card
        for (int i = 0; i < specialties.size(); i++) {
            String specialty = specialties.get(i);
            View specialtyView = getLayoutInflater().inflate(R.layout.item_specialty, specialtiesContainer, false);

            TextView specialtyText = specialtyView.findViewById(R.id.specialty_text);
            ImageButton editButton = specialtyView.findViewById(R.id.edit_specialty);
            ImageButton deleteButton = specialtyView.findViewById(R.id.delete_specialty);

            specialtyText.setText(specialty);

            final int position = i;

            // Set click listener for edit button
            editButton.setOnClickListener(v -> {
                showEditSpecialtyDialog(specialty, position, (updatedSpecialty, pos) -> {
                    viewModel.updateSpecialty(pos, updatedSpecialty);
                });
            });

            // Set click listener for delete button
            deleteButton.setOnClickListener(v -> {
                showDeleteSpecialtyConfirmation(position, pos -> {
                    viewModel.deleteSpecialty(pos);
                });
            });

            specialtiesContainer.addView(specialtyView);
        }
    }

    private void updateEducationUI(List<Doctor.Education> educationList) {
        educationContainer.removeAllViews();

        if (educationList == null || educationList.isEmpty()) {
            noEducationText.setVisibility(View.VISIBLE);
            return;
        }

        noEducationText.setVisibility(View.GONE);

        for (int i = 0; i < educationList.size(); i++) {
            Doctor.Education education = educationList.get(i);
            View educationView = getLayoutInflater().inflate(R.layout.item_education, educationContainer, false);

            TextView degreeText = educationView.findViewById(R.id.degree_text);
            TextView institutionText = educationView.findViewById(R.id.institution_text);
            TextView yearText = educationView.findViewById(R.id.year_text);
            ImageButton editButton = educationView.findViewById(R.id.edit_education);
            ImageButton deleteButton = educationView.findViewById(R.id.delete_education);

            degreeText.setText(education.getDegree());
            institutionText.setText(education.getInstitution());

            // Use year if available, otherwise use start and end dates
            if (education.getYear() != null && !education.getYear().isEmpty()) {
                yearText.setText(education.getYear());
            } else {
                String dateRange = "";
                if (education.getStartDate() != null && !education.getStartDate().isEmpty()) {
                    dateRange += education.getStartDate();
                }
                if (education.getEndDate() != null && !education.getEndDate().isEmpty()) {
                    dateRange += " - " + education.getEndDate();
                }
                yearText.setText(dateRange);
            }

            final int position = i;

            // Set click listener for edit button
            editButton.setOnClickListener(v -> {
                showEditEducationDialog(education, position, (updatedEducation, pos) -> {
                    viewModel.updateEducation(pos, updatedEducation);
                });
            });

            // Set click listener for delete button
            deleteButton.setOnClickListener(v -> {
                showDeleteEducationConfirmation(position, pos -> {
                    viewModel.deleteEducation(pos);
                });
            });

            educationContainer.addView(educationView);
        }
    }

    private interface OnEducationUpdateListener {
        void onEducationUpdated(Doctor.Education education, int position);
    }

    private interface OnEducationDeleteListener {
        void onEducationDeleted(int position);
    }

    private interface OnSpecialtyUpdateListener {
        void onSpecialtyUpdated(String specialty, int position);
    }

    private interface OnSpecialtyDeleteListener {
        void onSpecialtyDeleted(int position);
    }

    private interface OnWorkExperienceUpdateListener {
        void onWorkExperienceUpdated(Doctor.WorkExperience workExperience, int position);
    }

    // Interface for work experience delete callback
    private interface OnWorkExperienceDeleteListener {
        void onWorkExperienceDeleted(int position);
    }

    private void updateWorkExperienceUI(List<Doctor.WorkExperience> workExperienceList) {
        workExperienceContainer.removeAllViews();

        if (workExperienceList == null || workExperienceList.isEmpty()) {
            noWorkExperienceText.setVisibility(View.VISIBLE);
            return;
        }

        noWorkExperienceText.setVisibility(View.GONE);

        for (int i = 0; i < workExperienceList.size(); i++) {
            Doctor.WorkExperience workExperience = workExperienceList.get(i);
            View workExperienceView = getLayoutInflater().inflate(R.layout.item_work_experience, workExperienceContainer, false);

            TextView positionText = workExperienceView.findViewById(R.id.position_text);
            TextView companyText = workExperienceView.findViewById(R.id.company_text);
            TextView periodText = workExperienceView.findViewById(R.id.period_text);
            ImageButton editButton = workExperienceView.findViewById(R.id.edit_work_experience);
            ImageButton deleteButton = workExperienceView.findViewById(R.id.delete_work_experience);

            positionText.setText(workExperience.getPosition());
            companyText.setText(workExperience.getCompany());

            // Use period if available, otherwise use start and end dates
            if (workExperience.getPeriod() != null && !workExperience.getPeriod().isEmpty()) {
                periodText.setText(workExperience.getPeriod());
            } else {
                String dateRange = "";
                if (workExperience.getStartDate() != null && !workExperience.getStartDate().isEmpty()) {
                    dateRange += workExperience.getStartDate();
                }
                if (workExperience.getEndDate() != null && !workExperience.getEndDate().isEmpty()) {
                    dateRange += " - " + workExperience.getEndDate();
                }
                periodText.setText(dateRange);
            }

            final int position = i;

            // Set click listener for edit button
            editButton.setOnClickListener(v -> {
                showEditWorkExperienceDialog(workExperience, position, (updatedWorkExperience, pos) -> {
                    viewModel.updateWorkExperience(pos, updatedWorkExperience);
                });
            });

            // Set click listener for delete button
            deleteButton.setOnClickListener(v -> {
                showDeleteWorkExperienceConfirmation(position, pos -> {
                    viewModel.deleteWorkExperience(pos);
                });
            });

            workExperienceContainer.addView(workExperienceView);
        }
    }

    // Add this method to show the edit education dialog
    private void showEditEducationDialog(Doctor.Education education, int position, OnEducationUpdateListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_education, null);
        builder.setView(dialogView);
        builder.setTitle("Edit Education");

        EditText degreeInput = dialogView.findViewById(R.id.edit_degree);
        EditText institutionInput = dialogView.findViewById(R.id.edit_institution);
        EditText startDateInput = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.edit_end_date);

        // Pre-fill the fields with existing data
        degreeInput.setText(education.getDegree());
        institutionInput.setText(education.getInstitution());
        startDateInput.setText(education.getStartDate());
        endDateInput.setText(education.getEndDate());

        builder.setPositiveButton("Update", (dialog, which) -> {
            String degree = degreeInput.getText().toString().trim();
            String institution = institutionInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            if (!degree.isEmpty() && !institution.isEmpty() && listener != null) {
                Doctor.Education updatedEducation = new Doctor.Education(degree, institution, startDate, endDate);
                listener.onEducationUpdated(updatedEducation, position);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Add this method to show delete confirmation dialog for education
    private void showDeleteEducationConfirmation(int position, OnEducationDeleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Education");
        builder.setMessage("Are you sure you want to delete this education record?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            if (listener != null) {
                listener.onEducationDeleted(position);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Add this method to show the edit specialty dialog
    private void showEditSpecialtyDialog(String specialty, int position, OnSpecialtyUpdateListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_specialty, null);
        builder.setView(dialogView);
        builder.setTitle("Edit Specialty");

        EditText specialtyInput = dialogView.findViewById(R.id.specialty_input);

        // Pre-fill the field with existing data
        specialtyInput.setText(specialty);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String updatedSpecialty = specialtyInput.getText().toString().trim();
            if (!updatedSpecialty.isEmpty() && listener != null) {
                listener.onSpecialtyUpdated(updatedSpecialty, position);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Add this method to show delete confirmation dialog for specialty
    private void showDeleteSpecialtyConfirmation(int position, OnSpecialtyDeleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Specialty");
        builder.setMessage("Are you sure you want to delete this specialty?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            if (listener != null) {
                listener.onSpecialtyDeleted(position);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Add this method to show the edit work experience dialog
    private void showEditWorkExperienceDialog(Doctor.WorkExperience workExperience, int position, OnWorkExperienceUpdateListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_work_experience, null);
        builder.setView(dialogView);
        builder.setTitle("Edit Work Experience");

        EditText positionInput = dialogView.findViewById(R.id.edit_position);
        EditText companyInput = dialogView.findViewById(R.id.edit_company);
        EditText startDateInput = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.edit_end_date);

        // Pre-fill the fields with existing data
        positionInput.setText(workExperience.getPosition());
        companyInput.setText(workExperience.getCompany());
        startDateInput.setText(workExperience.getStartDate());
        endDateInput.setText(workExperience.getEndDate());

        builder.setPositiveButton("Update", (dialog, which) -> {
            String jobPosition = positionInput.getText().toString().trim();
            String company = companyInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            if (!jobPosition.isEmpty() && !company.isEmpty() && listener != null) {
                Doctor.WorkExperience updatedWorkExperience = new Doctor.WorkExperience(jobPosition, company, startDate, endDate);
                listener.onWorkExperienceUpdated(updatedWorkExperience, position);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Add this method to show delete confirmation dialog
    private void showDeleteWorkExperienceConfirmation(int position, OnWorkExperienceDeleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Work Experience");
        builder.setMessage("Are you sure you want to delete this work experience?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            if (listener != null) {
                listener.onWorkExperienceDeleted(position);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupClickableFields(View view) {
        // Name
        view.findViewById(R.id.nameCard).setOnClickListener(v -> {
            showEditTextDialog("Enter Name", nameValueTextView.getText().toString(), text -> {
                nameValueTextView.setText(text);
                Doctor doctor = viewModel.getDoctor().getValue();
                if (doctor != null) {
                    doctor.setName(text);
                }
            });
        });

        // Identification
        view.findViewById(R.id.identificationCard).setOnClickListener(v -> {
            showEditTextDialog("Enter Identification", identificationValueTextView.getText().toString(), text -> {
                identificationValueTextView.setText(text);
                Doctor doctor = viewModel.getDoctor().getValue();
                if (doctor != null) {
                    doctor.setIdentification(text);
                }
            });
        });

        // Phone
        view.findViewById(R.id.phoneCard).setOnClickListener(v -> {
            showEditTextDialog("Enter Phone", phoneValueTextView.getText().toString(), text -> {
                phoneValueTextView.setText(text);
                Doctor doctor = viewModel.getDoctor().getValue();
                if (doctor != null) {
                    doctor.setPhone(text);
                }
            });
        });

        // Medical Code
        view.findViewById(R.id.medicalCodeCard).setOnClickListener(v -> {
            showEditTextDialog("Enter Medical License Code", medicalCodeValueTextView.getText().toString(), text -> {
                medicalCodeValueTextView.setText(text);
                Doctor doctor = viewModel.getDoctor().getValue();
                if (doctor != null) {
                    doctor.setMedicalCode(text);
                }
            });
        });

        // Specialties - now handled by the addSpecialtyButton

        // Consultation Focus
        view.findViewById(R.id.consultationFocusCard).setOnClickListener(v -> {
            showEditTextDialog("Enter Consultation Focus", consultationFocusValueTextView.getText().toString(), text -> {
                consultationFocusValueTextView.setText(text);
                Doctor doctor = viewModel.getDoctor().getValue();
                if (doctor != null) {
                    doctor.setConsultationFocus(text);
                }
            });
        });

        // Add Education Button
        view.findViewById(R.id.addEducationButton).setOnClickListener(v -> {
            showAddEducationDialog(education -> {
                viewModel.addEducation(education);
            });
        });

        // Add Work Experience Button
        view.findViewById(R.id.addWorkExperienceButton).setOnClickListener(v -> {
            showAddWorkExperienceDialog(workExperience -> {
                viewModel.addWorkExperience(workExperience);
            });
        });
    }

    // Custom method to show edit text dialog
    private void showEditTextDialog(String title, String initialValue, OnTextSavedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setText(initialValue);
        input.setSelection(input.getText().length());
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (listener != null) {
                listener.onTextSaved(text);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Custom method to show add specialty dialog
    private void showAddSpecialtyDialog(OnSpecialtySavedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Specialty");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setHint("Enter specialty");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String specialty = input.getText().toString().trim();
            if (!specialty.isEmpty() && listener != null) {
                listener.onSpecialtySaved(specialty);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Custom method to show add education dialog
    private void showAddEducationDialog(OnEducationSavedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_education, null);
        builder.setView(dialogView);
        builder.setTitle("Add Education");

        EditText degreeInput = dialogView.findViewById(R.id.edit_degree);
        EditText institutionInput = dialogView.findViewById(R.id.edit_institution);
        EditText startDateInput = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.edit_end_date);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String degree = degreeInput.getText().toString().trim();
            String institution = institutionInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            if (!degree.isEmpty() && !institution.isEmpty() && listener != null) {
                Doctor.Education education = new Doctor.Education(degree, institution, startDate, endDate);
                listener.onEducationSaved(education);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Custom method to show add work experience dialog
    private void showAddWorkExperienceDialog(OnWorkExperienceSavedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_work_experience, null);
        builder.setView(dialogView);
        builder.setTitle("Add Work Experience");

        EditText positionInput = dialogView.findViewById(R.id.edit_position);
        EditText companyInput = dialogView.findViewById(R.id.edit_company);
        EditText startDateInput = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.edit_end_date);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String position = positionInput.getText().toString().trim();
            String company = companyInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            if (!position.isEmpty() && !company.isEmpty() && listener != null) {
                Doctor.WorkExperience workExperience = new Doctor.WorkExperience(position, company, startDate, endDate);
                listener.onWorkExperienceSaved(workExperience);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Interface for text saved callback
    private interface OnTextSavedListener {
        void onTextSaved(String text);
    }

    // Interface for specialty saved callback
    private interface OnSpecialtySavedListener {
        void onSpecialtySaved(String specialty);
    }

    // Interface for education saved callback
    private interface OnEducationSavedListener {
        void onEducationSaved(Doctor.Education education);
    }

    // Interface for work experience saved callback
    private interface OnWorkExperienceSavedListener {
        void onWorkExperienceSaved(Doctor.WorkExperience workExperience);
    }

    private void saveDoctorData() {
        Doctor doctor = viewModel.getDoctor().getValue();
        if (doctor != null) {
            // Get updated values from UI
            doctor.setName(nameValueTextView.getText().toString().trim());
            doctor.setIdentification(identificationValueTextView.getText().toString().trim());
            doctor.setPhone(phoneValueTextView.getText().toString().trim());
            doctor.setMedicalCode(medicalCodeValueTextView.getText().toString().trim());
            doctor.setConsultationFocus(consultationFocusValueTextView.getText().toString().trim());

            viewModel.updateDoctor(doctor);
            Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (saveButton != null) {
            saveButton.setEnabled(!isLoading);
        }
    }
}
