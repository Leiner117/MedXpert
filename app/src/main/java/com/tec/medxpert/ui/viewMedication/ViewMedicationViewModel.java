package com.tec.medxpert.ui.viewMedication;


import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.Medication;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ViewMedicationViewModel extends ViewModel {
    private final MutableLiveData<List<Medication>> medications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToAddMedication = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedFilter = new MutableLiveData<>("name");
    private final MutableLiveData<Medication> navigateToEditMedication = new MutableLiveData<>();
    @Inject
    Application application;
    private final FirebaseFirestore firestore;


    @Inject
    public ViewMedicationViewModel(FirebaseFirestore firestore) {
        this.firestore = firestore;
        loadData();
    }

    private void loadData() {
        firestore.collection("medications")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        System.out.println(application.getString(R.string.no_documents_found_message));
                        medications.setValue(new ArrayList<>());
                    } else {
                        List<Medication> medicationList = createMedicationList(querySnapshot.getDocuments());
                        medications.setValue(medicationList);
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println(application.getString(R.string.error_fetching_data) + e.getMessage());
                    medications.setValue(new ArrayList<>());
                });
    }

    private List<Medication> createMedicationList(List<DocumentSnapshot> documents) {
        List<Medication> medicationList = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            Medication medication = document.toObject(Medication.class);
            if (medication != null) {
                medication.setId(document.getId());
                medicationList.add(medication);
            }
        }
        return medicationList;
    }

    public void deleteMedication(Medication medication) {
        firestore.collection("medications")
                .document(medication.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    List<Medication> updatedList = new ArrayList<>(medications.getValue());
                    updatedList.remove(medication);
                    medications.setValue(updatedList);
                })
                .addOnFailureListener(e -> {
                    System.out.println(application.getString(R.string.error_deleting_document) + e.getMessage());
                });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSelectedFilter(String filter) {
        selectedFilter.setValue(filter);
    }

    public LiveData<String> getSelectedFilter() {
        return selectedFilter;
    }

    public LiveData<List<Medication>> getFilteredMedications() {
        return Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return medications;
            } else {
                MutableLiveData<List<Medication>> filteredList = new MutableLiveData<>();
                List<Medication> filtered = new ArrayList<>();
                String filter = selectedFilter.getValue();

                for (Medication med : medications.getValue()) {
                    if (filter.equals("name") && med.getName().toLowerCase().contains(query.toLowerCase()) ||
                            filter.equals("date") && med.getRegistrationDate() != null &&
                                    med.getRegistrationDate().toDate().toString().toLowerCase().contains(query.toLowerCase()) ||
                            filter.equals("dosage") && med.getDosage().toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(med);
                    }
                }
                filteredList.setValue(filtered);
                return filteredList;
            }
        });
    }

    public LiveData<Medication> getNavigateToEditMedication() {
        return navigateToEditMedication;
    }

    public void onEditMedication(Medication medication) {
        navigateToEditMedication.setValue(medication);
    }

    public LiveData<Boolean> getNavigateToMain() {
        return navigateToMain;
    }

    public LiveData<Boolean> getNavigateToAddMedication() {
        return navigateToAddMedication;
    }

    public void onBackClicked() {
        navigateToMain.setValue(true);
    }

    public void onAddMedicationClicked() {
        navigateToAddMedication.setValue(true);
    }

    public void resetNavigationStates() {
        navigateToMain.setValue(false);
        navigateToAddMedication.setValue(false);
    }
}
