package com.tec.medxpert.ui.profile.doctor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tec.medxpert.data.model.profile.Doctor;
import com.tec.medxpert.data.repository.profile.DoctorRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DoctorViewModel extends ViewModel {

    private final DoctorRepository doctorRepository;
    private final MutableLiveData<Doctor> doctorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final FirebaseAuth firebaseAuth;

    @Inject
    public DoctorViewModel(DoctorRepository doctorRepository, FirebaseAuth firebaseAuth) {
        this.doctorRepository = doctorRepository;
        this.firebaseAuth = firebaseAuth;
        loadCurrentDoctor();
    }

    public void loadCurrentDoctor() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("No user is currently logged in");
            return;
        }

        String userId = currentUser.getUid();
        isLoading.setValue(true);

        doctorRepository.getDoctorByUserId(userId)
                .addOnSuccessListener(documentSnapshots -> {
                    isLoading.setValue(false);
                    if (documentSnapshots.isEmpty()) {
                        // No doctor found for this user ID, create a new one
                        createNewDoctor(userId, currentUser.getEmail());
                    } else {
                        // Doctor exists, load the data
                        DocumentSnapshot documentSnapshot = documentSnapshots.getDocuments().get(0);
                        Doctor doctor = documentSnapshot.toObject(Doctor.class);
                        if (doctor != null) {
                            doctor.setId(documentSnapshot.getId());
                            doctorLiveData.setValue(doctor);
                        } else {
                            errorMessage.setValue("Error converting document to Doctor");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error loading doctor: " + e.getMessage());
                });
    }

    private void createNewDoctor(String userId, String email) {
        Doctor newDoctor = new Doctor();
        newDoctor.setUserId(userId);
        newDoctor.setName("");
        newDoctor.setEmail(email);
        newDoctor.setIdentification("");
        newDoctor.setPhone("");
        newDoctor.setMedicalCode("");
        newDoctor.setConsultationFocus("");
        newDoctor.setSpecialties(new ArrayList<>());
        newDoctor.setEducation(new ArrayList<>());
        newDoctor.setWorkExperience(new ArrayList<>());

        doctorRepository.createDoctor(newDoctor)
                .addOnSuccessListener(documentReference -> {
                    newDoctor.setId(documentReference.getId());
                    doctorLiveData.setValue(newDoctor);
                    // Also update the user's role in the users collection
                    doctorRepository.updateUserRole(userId, "doctor");
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Error creating doctor: " + e.getMessage());
                });
    }

    public void updateDoctor(Doctor doctor) {
        if (doctor.getId() == null || doctor.getId().isEmpty()) {
            errorMessage.setValue("Cannot update doctor without ID");
            return;
        }

        isLoading.setValue(true);
        doctorRepository.updateDoctor(doctor)
                .addOnSuccessListener(aVoid -> {
                    isLoading.setValue(false);
                    doctorLiveData.setValue(doctor);
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorMessage.setValue("Error updating doctor: " + e.getMessage());
                });
    }

    public LiveData<Doctor> getDoctor() {
        return doctorLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addEducation(Doctor.Education education) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<Doctor.Education> educationList = doctor.getEducation();
            if (educationList == null) {
                educationList = new ArrayList<>();
                doctor.setEducation(educationList);
            }
            educationList.add(education);
            updateDoctor(doctor);
        }
    }

    public void updateEducation(int index, Doctor.Education updatedEducation) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<Doctor.Education> educationList = doctor.getEducation();
            if (educationList != null && index >= 0 && index < educationList.size()) {
                educationList.set(index, updatedEducation);
                updateDoctor(doctor);
            } else {
                errorMessage.setValue("Invalid education index");
            }
        }
    }

    public void deleteEducation(int index) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<Doctor.Education> educationList = doctor.getEducation();
            if (educationList != null && index >= 0 && index < educationList.size()) {
                educationList.remove(index);
                updateDoctor(doctor);
            } else {
                errorMessage.setValue("Invalid education index");
            }
        }
    }

    public void addWorkExperience(Doctor.WorkExperience workExperience) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<Doctor.WorkExperience> workExperienceList = doctor.getWorkExperience();
            if (workExperienceList == null) {
                workExperienceList = new ArrayList<>();
                doctor.setWorkExperience(workExperienceList);
            }
            workExperienceList.add(workExperience);
            updateDoctor(doctor);
        }
    }

    public void updateWorkExperience(int index, Doctor.WorkExperience updatedWorkExperience) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<Doctor.WorkExperience> workExperienceList = doctor.getWorkExperience();
            if (workExperienceList != null && index >= 0 && index < workExperienceList.size()) {
                workExperienceList.set(index, updatedWorkExperience);
                updateDoctor(doctor);
            } else {
                errorMessage.setValue("Invalid work experience index");
            }
        }
    }

    public void deleteWorkExperience(int index) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<Doctor.WorkExperience> workExperienceList = doctor.getWorkExperience();
            if (workExperienceList != null && index >= 0 && index < workExperienceList.size()) {
                workExperienceList.remove(index);
                updateDoctor(doctor);
            } else {
                errorMessage.setValue("Invalid work experience index");
            }
        }
    }

    public void addSpecialty(String specialty) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<String> specialties = doctor.getSpecialties();
            if (specialties == null) {
                specialties = new ArrayList<>();
                doctor.setSpecialties(specialties);
            }
            specialties.add(specialty);
            updateDoctor(doctor);
        }
    }

    public void updateSpecialty(int index, String updatedSpecialty) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<String> specialties = doctor.getSpecialties();
            if (specialties != null && index >= 0 && index < specialties.size()) {
                specialties.set(index, updatedSpecialty);
                updateDoctor(doctor);
            } else {
                errorMessage.setValue("Invalid specialty index");
            }
        }
    }

    public void deleteSpecialty(int index) {
        Doctor doctor = doctorLiveData.getValue();
        if (doctor != null) {
            List<String> specialties = doctor.getSpecialties();
            if (specialties != null && index >= 0 && index < specialties.size()) {
                specialties.remove(index);
                updateDoctor(doctor);
            } else {
                errorMessage.setValue("Invalid specialty index");
            }
        }
    }
}
