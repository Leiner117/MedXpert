package com.tec.medxpert.ui.profile.patient.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.data.repository.profile.PatientRepository;
import com.tec.medxpert.util.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for handling patient list data and operations
 */
@HiltViewModel
public class PatientListViewModel extends ViewModel {

    private final PatientRepository patientRepository;

    private final MutableLiveData<Resource<List<Patient>>> patientList = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public PatientListViewModel(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
        loadPatients();
    }

    /**
     * Load all patients from Firestore
     */
    public void loadPatients() {
        patientList.setValue(Resource.loading(null));

        patientRepository.getAllPatients()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Patient> patients = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Patient patient = Patient.fromMap(document.getData(), document.getId());
                        patients.add(patient);
                    }
                    patientList.setValue(Resource.success(patients));
                })
                .addOnFailureListener(e -> {
                    patientList.setValue(Resource.error("Failed to load patients: " + e.getMessage(), null));
                });
    }

    /**
     * Search patients by name or ID
     * @param query The search query
     */
    public void searchPatients(String query) {
        searchQuery.setValue(query);

        if (query == null || query.trim().isEmpty()) {
            loadPatients();
            return;
        }

        patientList.setValue(Resource.loading(null));

        patientRepository.searchPatients(query)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Patient> patients = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Patient patient = Patient.fromMap(document.getData(), document.getId());
                        patients.add(patient);
                    }
                    patientList.setValue(Resource.success(patients));
                })
                .addOnFailureListener(e -> {
                    patientList.setValue(Resource.error("Failed to search patients: " + e.getMessage(), null));
                });
    }

    /**
     * Sort patients by name
     * @param ascending True for ascending order, false for descending
     */
    public void sortPatientsByName(boolean ascending) {
        patientList.setValue(Resource.loading(null));

        Query.Direction direction = ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;

        patientRepository.getPatientsSortedByName(direction)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Patient> patients = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Patient patient = Patient.fromMap(document.getData(), document.getId());
                        patients.add(patient);
                    }
                    patientList.setValue(Resource.success(patients));
                })
                .addOnFailureListener(e -> {
                    patientList.setValue(Resource.error("Failed to sort patients: " + e.getMessage(), null));
                });
    }

    /**
     * Sort patients by registration date
     * @param ascending True for ascending order, false for descending
     */
    public void sortPatientsByDate(boolean ascending) {
        patientList.setValue(Resource.loading(null));

        Query.Direction direction = ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;

        patientRepository.getPatientsSortedByDate(direction)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Patient> patients = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Patient patient = Patient.fromMap(document.getData(), document.getId());
                        patients.add(patient);
                    }
                    patientList.setValue(Resource.success(patients));
                })
                .addOnFailureListener(e -> {
                    patientList.setValue(Resource.error("Failed to sort patients: " + e.getMessage(), null));
                });
    }

    // Getters for LiveData
    public LiveData<Resource<List<Patient>>> getPatientList() {
        return patientList;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
