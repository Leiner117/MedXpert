package com.tec.medxpert.ui.profile.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tec.medxpert.auth.MockAuthProvider;
import com.tec.medxpert.data.model.profile.ChangeRecord;
import com.tec.medxpert.data.repository.profile.ChangeHistoryRepository;
import com.tec.medxpert.data.repository.profile.PatientRepository;
import com.tec.medxpert.util.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for handling change history data and operations
 */
@HiltViewModel
public class ChangeHistoryViewModel extends ViewModel {

    private final ChangeHistoryRepository changeHistoryRepository;
    private final PatientRepository patientRepository;
    private final MockAuthProvider mockAuthProvider;

    private final MutableLiveData<Resource<List<ChangeRecord>>> changeHistory = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String currentPatientId;

    @Inject
    public ChangeHistoryViewModel(ChangeHistoryRepository changeHistoryRepository,
                                  PatientRepository patientRepository,
                                  MockAuthProvider mockAuthProvider) {
        this.changeHistoryRepository = changeHistoryRepository;
        this.patientRepository = patientRepository;
        this.mockAuthProvider = mockAuthProvider;
        loadCurrentPatient();
    }

    /**
     * Set the patient ID and load change history
     */
    public void setPatientId(String patientId) {
        if (patientId != null && !patientId.isEmpty()) {
            this.currentPatientId = patientId;
            loadChangeHistory();
        } else {
            loadCurrentPatient();
        }
    }

    /**
     * Load the current patient ID
     */
    private void loadCurrentPatient() {
        String userId = mockAuthProvider.getUserId();

        patientRepository.getPatientByUserId(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        currentPatientId = documentSnapshot.getId();
                        loadChangeHistory();
                    } else {
                        errorMessage.setValue("Patient not found");
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to load patient: " + e.getMessage());
                });
    }

    /**
     * Load change history for the current patient
     */
    public void loadChangeHistory() {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        changeHistory.setValue(Resource.loading(null));

        changeHistoryRepository.getChangeHistoryForPatient(currentPatientId)
                .addOnSuccessListener(querySnapshot -> {
                    List<ChangeRecord> records = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        ChangeRecord record = ChangeRecord.fromMap(document.getData(), document.getId());
                        records.add(record);
                    }
                    // Sort records by timestamp in descending order (newest first)
                    Collections.sort(records, (r1, r2) -> {
                        if (r1.getTimestamp() == null || r2.getTimestamp() == null) {
                            return 0;
                        }
                        return r2.getTimestamp().compareTo(r1.getTimestamp());
                    });
                    changeHistory.setValue(Resource.success(records));
                })
                .addOnFailureListener(e -> {
                    changeHistory.setValue(Resource.error("Failed to load change history: " + e.getMessage(), null));
                });
    }

    /**
     * Load change history for a specific field
     * @param fieldName The field name
     */
    public void loadChangeHistoryForField(String fieldName) {
        if (currentPatientId == null) {
            errorMessage.setValue("Patient not loaded");
            return;
        }

        changeHistory.setValue(Resource.loading(null));

        changeHistoryRepository.getChangeHistoryForField(currentPatientId, fieldName)
                .addOnSuccessListener(querySnapshot -> {
                    List<ChangeRecord> records = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        ChangeRecord record = ChangeRecord.fromMap(document.getData(), document.getId());
                        records.add(record);
                    }
                    // Sort records by timestamp in descending order (newest first)
                    Collections.sort(records, (r1, r2) -> {
                        if (r1.getTimestamp() == null || r2.getTimestamp() == null) {
                            return 0;
                        }
                        return r2.getTimestamp().compareTo(r1.getTimestamp());
                    });
                    changeHistory.setValue(Resource.success(records));
                })
                .addOnFailureListener(e -> {
                    changeHistory.setValue(Resource.error("Failed to load change history: " + e.getMessage(), null));
                });
    }

    // Getters for LiveData
    public LiveData<Resource<List<ChangeRecord>>> getChangeHistory() {
        return changeHistory;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
