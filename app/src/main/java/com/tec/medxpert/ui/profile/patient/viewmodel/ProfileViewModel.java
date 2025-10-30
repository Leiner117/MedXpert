package com.tec.medxpert.ui.profile.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tec.medxpert.auth.MockAuthProvider;
import com.tec.medxpert.util.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.data.model.profile.PersonalData;
import com.tec.medxpert.data.repository.profile.PatientRepository;

/**
 * ViewModel for handling patient profile data and operations
 */
@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final PatientRepository patientRepository;
    private final MockAuthProvider mockAuthProvider;

    private final MutableLiveData<Resource<Patient>> patientData = new MutableLiveData<>();
    private final MutableLiveData<Resource<Boolean>> saveStatus = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String currentPatientId;

    @Inject
    public ProfileViewModel(PatientRepository patientRepository, MockAuthProvider mockAuthProvider) {
        this.patientRepository = patientRepository;
        this.mockAuthProvider = mockAuthProvider;
        loadCurrentPatient();
    }

    /**
     * Load the current patient data from Firestore
     */
    public void loadCurrentPatient() {
        // Use the mock auth provider to get a user ID, whether real or simulated
        String userId = mockAuthProvider.getUserId();

        patientData.setValue(Resource.loading(null));

        patientRepository.getPatientByUserId(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Patient patient = Patient.fromMap(documentSnapshot.getData(), documentSnapshot.getId());
                        currentPatientId = documentSnapshot.getId();
                        patientData.setValue(Resource.success(patient));
                    } else {
                        // Create a new patient if not exists
                        createNewPatient(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    patientData.setValue(Resource.error("Failed to load patient data: " + e.getMessage(), null));
                });
    }

    /**
     * Create a new patient in Firestore
     * @param userId The user ID
     */
    private void createNewPatient(String userId) {
        PersonalData personalData = new PersonalData();
        Patient newPatient = new Patient(userId, personalData);

        patientRepository.createPatient(newPatient)
                .addOnSuccessListener(documentReference -> {
                    newPatient.setPatientId(documentReference.getId());
                    currentPatientId = documentReference.getId();
                    patientData.setValue(Resource.success(newPatient));
                })
                .addOnFailureListener(e -> {
                    patientData.setValue(Resource.error("Failed to create patient: " + e.getMessage(), null));
                });
    }

    /**
     * Update patient ID type
     * @param idType The new ID type
     */
    public void updateIdType(String idType) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "idType", idType)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setIdType(idType);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update ID type: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient name
     * @param name The new name
     */
    public void updateName(String name) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "name", name)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setName(name);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update name: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient ID number
     * @param idNumber The new ID number
     */
    public void updateIdNumber(String idNumber) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "idNumber", idNumber)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setIdNumber(idNumber);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update ID number: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient phone
     * @param phone The new phone
     */
    public void updatePhone(String phone) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "phone", phone)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setPhone(phone);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update phone: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient blood type
     * @param bloodType The new blood type
     */
    public void updateBloodType(String bloodType) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "bloodType", bloodType)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setBloodType(bloodType);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update blood type: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient weight
     * @param weight The new weight
     */
    public void updateWeight(Double weight) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "weight", weight)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setWeight(weight);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update weight: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient height
     * @param height The new height
     */
    public void updateHeight(Double height) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "height", height)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setHeight(height);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update height: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient allergies
     * @param allergies The new allergies list
     */
    public void updateAllergies(List<String> allergies) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "allergies", allergies)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setAllergies(allergies);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update allergies: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient personal medical history
     * @param history The new personal medical history list
     */
    public void updatePersonalMedicalHistory(List<String> history) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "personalMedicalHistory", history)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setPersonalMedicalHistory(history);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update personal medical history: " + e.getMessage(), false));
                });
    }

    /**
     * Update patient family medical history
     * @param history The new family medical history list
     */
    public void updateFamilyMedicalHistory(List<String> history) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientField(currentPatientId, "familyMedicalHistory", history)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.getPersonalData().setFamilyMedicalHistory(history);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to update family medical history: " + e.getMessage(), false));
                });
    }

    /**
     * Save all patient data at once
     * @param personalData The complete personal data object
     */
    public void saveAllPatientData(PersonalData personalData) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        saveStatus.setValue(Resource.loading(null));

        patientRepository.updatePatientPersonalData(currentPatientId, personalData)
                .addOnSuccessListener(aVoid -> {
                    Patient current = patientData.getValue() != null ? patientData.getValue().data : null;
                    if (current != null) {
                        current.setPersonalData(personalData);
                        patientData.setValue(Resource.success(current));
                    }
                    saveStatus.setValue(Resource.success(true));
                })
                .addOnFailureListener(e -> {
                    saveStatus.setValue(Resource.error("Failed to save patient data: " + e.getMessage(), false));
                });
    }

    // Getters for LiveData
    public LiveData<Resource<Patient>> getPatientData() {
        return patientData;
    }

    public LiveData<Resource<Boolean>> getSaveStatus() {
        return saveStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
