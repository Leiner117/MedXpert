package com.tec.medxpert.ui.ViewMedicationPatient;


import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.Medication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ViewMedicationPatientViewModel extends ViewModel {

    public enum TabType {
        IN_USE, TERMINATED
    }

    private final MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<TabType> selectedTab = new MutableLiveData<>(TabType.IN_USE);
    private final MutableLiveData<List<Medication>> medications = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final List<Medication> allMedications = new ArrayList<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public Application application;

    @Inject
    public ViewMedicationPatientViewModel() {
        fetchPatientMedications();
    }

    private void fetchPatientMedications() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        isLoading.setValue(true);
        String uid = user.getUid();
        getPatientId(uid);
    }

    private void getPatientId(String uid) {
        firestore.collection("patients")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot patientDoc = snapshot.getDocuments().get(0);
                        String patientId = patientDoc.getId();
                        getDiagnosticsForPatient(patientId);
                    } else {
                        Log.w("ViewModel", "No patient found for the user with UID: " + uid);
                        isLoading.setValue(false);
                        statusMessage.setValue(application.getString(R.string.no_assigned_medications));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ViewModel", "Error fetching patient", e);
                    statusMessage.setValue(application.getString(R.string.error_loading_patient_info));
                });
    }

    private void getDiagnosticsForPatient(String patientId) {
        firestore.collection("diagnostic")
                .whereEqualTo("patientId", patientId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    isLoading.setValue(false);
                    if (!snapshot.isEmpty()) {
                        processMedicineList(snapshot);
                    } else {
                        Log.i("ViewModel", "No diagnostics found for patient: " + patientId);
                        statusMessage.setValue(application.getString(R.string.no_assigned_medications));
                        medications.setValue(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ViewModel", "Error loading diagnostics", e);
                    isLoading.setValue(false);
                    statusMessage.setValue(application.getString(R.string.error_loading_diagnostics));
                    medications.setValue(new ArrayList<>());
                });
    }

    private void processMedicineList(QuerySnapshot diagnosticsSnapshot) {
        allMedications.clear();
        boolean foundAnyMedicine = false;

        try {
            for (DocumentSnapshot diagnosticDoc : diagnosticsSnapshot) {
                Timestamp updatedAt = diagnosticDoc.getTimestamp("updatedAt");

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> medicineList = (List<Map<String, Object>>) diagnosticDoc.get("medicineList");

                if (medicineList != null && !medicineList.isEmpty()) {
                    foundAnyMedicine = true;
                    for (Map<String, Object> medicineEntry : medicineList) {
                        if (medicineEntry != null) {
                            processMedicineEntry(medicineEntry, updatedAt);
                        }
                    }
                }
            }

            medications.setValue(new ArrayList<>(allMedications));

            if (!foundAnyMedicine) {
                Log.i("ViewModel", "No medicines found in any diagnostic");
                statusMessage.setValue(application.getString(R.string.no_assigned_medications));
            } else {
                Log.i("ViewModel", "Found " + allMedications.size() + " medications");
                statusMessage.setValue("");
            }

        } catch (Exception e) {
            Log.e("ViewModel", "Error processing medicine list", e);
            statusMessage.setValue(application.getString(R.string.no_assigned_medications));
            medications.setValue(new ArrayList<>());
        }
    }

    private void processMedicineEntry(Map<String, Object> medicineEntry, Timestamp updatedAt) {
        try {
            if (medicineEntry == null) {
                Log.w("ViewModel", "Medicine entry is null, skipping");
                return;
            }

            String medicineId = (String) medicineEntry.get("id");
            String medicineName = (String) medicineEntry.get("name");
            String dosage = (String) medicineEntry.get("dosage");

            if (medicineId == null || medicineName == null) {
                Log.w("ViewModel", "Medicine missing essential data (id or name), skipping");
                return;
            }

            Object daysObj = medicineEntry.get("days");
            Object hoursObj = medicineEntry.get("hours");

            int days = 0;
            int hours = 0;

            if (daysObj instanceof Number) {
                days = ((Number) daysObj).intValue();
            }

            if (hoursObj instanceof Number) {
                hours = ((Number) hoursObj).intValue();
            }

            Map<String, Integer> frequencyMap = new HashMap<>();
            frequencyMap.put("everyHours", hours);
            frequencyMap.put("days", days);

            Medication medication = new Medication(
                    updatedAt,
                    medicineId,
                    medicineName,
                    "", // description
                    dosage != null ? dosage : "No especificado",
                    frequencyMap
            );

            allMedications.add(medication);
            Log.d("ViewModel", "Added medication: " + medicineName);

        } catch (Exception e) {
            Log.e("ViewModel", "Error processing medicine entry", e);
        }
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query != null ? query : "");
    }

    public void setSelectedTab(TabType tabType) {
        selectedTab.setValue(tabType != null ? tabType : TabType.IN_USE);
    }

    public LiveData<List<Medication>> getFilteredMedications() {
        return Transformations.switchMap(selectedTab, tab ->
                Transformations.switchMap(searchQuery, query ->
                        Transformations.map(medications, allMeds -> {
                            if (allMeds == null) {
                                allMeds = new ArrayList<>();
                            }

                            List<Medication> filteredByTab = new ArrayList<>();

                            if (!allMeds.isEmpty()) {
                                for (Medication med : allMeds) {
                                    if (med != null) {
                                        try {
                                            if (tab == TabType.IN_USE && med.isInUse()) {
                                                filteredByTab.add(med);
                                            } else if (tab == TabType.TERMINATED && med.isTerminated()) {
                                                filteredByTab.add(med);
                                            }
                                        } catch (Exception e) {
                                            Log.e("ViewModel", "Error checking medication status", e);
                                        }
                                    }
                                }
                            }

                            if (query == null || query.trim().isEmpty()) {
                                updateStatusMessage(filteredByTab, tab);
                                return filteredByTab;
                            } else {
                                List<Medication> filteredBySearch = new ArrayList<>();
                                String lowerQuery = query.toLowerCase().trim();

                                for (Medication med : filteredByTab) {
                                    if (med != null) {
                                        try {
                                            if ((med.getName() != null && med.getName().toLowerCase().contains(lowerQuery)) ||
                                                    (med.getDescription() != null && med.getDescription().toLowerCase().contains(lowerQuery)) ||
                                                    (med.getDosage() != null && med.getDosage().toLowerCase().contains(lowerQuery))) {
                                                filteredBySearch.add(med);
                                            }
                                        } catch (Exception e) {
                                            Log.e("ViewModel", "Error filtering medication", e);
                                        }
                                    }
                                }

                                updateStatusMessage(filteredBySearch, tab);
                                return filteredBySearch;
                            }
                        })
                )
        );
    }

    private void updateStatusMessage(List<Medication> filteredMeds, TabType currentTab) {
        if (filteredMeds == null || filteredMeds.isEmpty()) {
            List<Medication> allMeds = medications.getValue();
            if (allMeds == null || allMeds.isEmpty()) {
                statusMessage.setValue(application.getString(R.string.no_assigned_medications));
            } else {
                String message = currentTab == TabType.IN_USE ?
                        application.getString(R.string.no_medications_in_use) :
                        application.getString(R.string.no_medications_terminated);
                statusMessage.setValue(message);
            }
        } else {
            statusMessage.setValue("");
        }
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void onBackClicked() {
        navigateToMain.setValue(true);
    }

    public LiveData<Boolean> getNavigateToMain() {
        return navigateToMain;
    }

    public void resetNavigationStates() {
        navigateToMain.setValue(false);
    }
}
