package com.tec.medxpert.navigation.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.tec.medxpert.R;
import com.tec.medxpert.data.model.profile.Doctor;
import com.tec.medxpert.ui.profile.doctor.DoctorProfileFragment;

public class DoctorCoordinator implements IDoctorCoordinator {
    private final FragmentManager fragmentManager;
    private final Context context;

    public DoctorCoordinator(FragmentManager fragmentManager, Context context) {
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    @Override
    public void navigateToDoctorProfile(String doctorId) {
        DoctorProfileFragment fragment = new DoctorProfileFragment();

        // Pass doctor ID to the fragment if provided
        if (doctorId != null && !doctorId.isEmpty()) {
            Bundle args = new Bundle();
            args.putString("doctorId", doctorId);
            fragment.setArguments(args);
        }

        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void showAddEducationDialog(Doctor.Education education, OnEducationSavedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_education, null);
        builder.setView(dialogView);

        EditText degreeInput = dialogView.findViewById(R.id.edit_degree);
        EditText institutionInput = dialogView.findViewById(R.id.edit_institution);
        EditText startDateInput = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.edit_end_date);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        //Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        // If editing existing education, pre-fill the fields
        if (education != null) {
            degreeInput.setText(education.getDegree());
            institutionInput.setText(education.getInstitution());
            if (education.getStartDate() != null) {
                startDateInput.setText(education.getStartDate());
            }
            if (education.getEndDate() != null) {
                endDateInput.setText(education.getEndDate());
            }
        }

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String degree = degreeInput.getText().toString().trim();
            String institution = institutionInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            if (degree.isEmpty() || institution.isEmpty()) {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Doctor.Education newEducation = new Doctor.Education(degree, institution, startDate, endDate);

            if (listener != null) {
                listener.onEducationSaved(newEducation);
            }

            dialog.dismiss();
        });

        //cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void showAddWorkExperienceDialog(Doctor.WorkExperience workExperience, OnWorkExperienceSavedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_work_experience, null);
        builder.setView(dialogView);

        EditText positionInput = dialogView.findViewById(R.id.edit_position);
        EditText companyInput = dialogView.findViewById(R.id.edit_company);
        EditText startDateInput = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.edit_end_date);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        //Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        // If editing existing work experience, pre-fill the fields
        if (workExperience != null) {
            positionInput.setText(workExperience.getPosition());
            companyInput.setText(workExperience.getCompany());
            if (workExperience.getStartDate() != null) {
                startDateInput.setText(workExperience.getStartDate());
            }
            if (workExperience.getEndDate() != null) {
                endDateInput.setText(workExperience.getEndDate());
            }
        }

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String position = positionInput.getText().toString().trim();
            String company = companyInput.getText().toString().trim();
            String startDate = startDateInput.getText().toString().trim();
            String endDate = endDateInput.getText().toString().trim();

            if (position.isEmpty() || company.isEmpty()) {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Doctor.WorkExperience newWorkExperience = new Doctor.WorkExperience(position, company, startDate, endDate);

            if (listener != null) {
                listener.onWorkExperienceSaved(newWorkExperience);
            }

            dialog.dismiss();
        });

        //cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void showAddSpecialtyDialog(OnSpecialtySavedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_specialty, null);
        builder.setView(dialogView);

        EditText specialtyInput = dialogView.findViewById(R.id.specialty_input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String specialty = specialtyInput.getText().toString().trim();

            if (specialty.isEmpty()) {
                Toast.makeText(context, "Please enter a specialty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onSpecialtySaved(specialty);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
