package com.tec.medxpert.navigation.profile;

import com.tec.medxpert.data.model.profile.Doctor;

public interface IDoctorCoordinator {
    void navigateToDoctorProfile(String doctorId);

    void showAddEducationDialog(Doctor.Education education, OnEducationSavedListener listener);

    void showAddWorkExperienceDialog(Doctor.WorkExperience workExperience, OnWorkExperienceSavedListener listener);

    void showAddSpecialtyDialog(OnSpecialtySavedListener listener);

    interface OnEducationSavedListener {
        void onEducationSaved(Doctor.Education education);
    }

    interface OnWorkExperienceSavedListener {
        void onWorkExperienceSaved(Doctor.WorkExperience workExperience);
    }

    interface OnSpecialtySavedListener {
        void onSpecialtySaved(String specialty);
    }
}
