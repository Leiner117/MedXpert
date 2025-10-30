package com.tec.medxpert.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.tec.medxpert.data.model.patientChats.PatientChat;
import com.tec.medxpert.data.repository.patientChats.PatientsChatRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatsListViewModel extends ViewModel {
    private final MutableLiveData<List<PatientChat>> patients = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedFilter = new MutableLiveData<>("name");
    private final PatientsChatRepository repository;

    @Inject
    public ChatsListViewModel(PatientsChatRepository repository) {
        this.repository = repository;
        loadPatients();
    }
    public LiveData<String> getSelectedFilter() {
        return selectedFilter;
    }

    public void loadPatients() {
        repository.getPatients(new PatientsChatRepository.OnPatientsLoadedCallback() {
            @Override
            public void onSuccess(List<PatientChat> loadedPatients) {
                patients.setValue(loadedPatients);
                searchQuery.setValue("");
            }

            @Override
            public void onFailure(Exception e) {
                patients.setValue(new ArrayList<>());
            }
        });
    }

    public LiveData<List<PatientChat>> getFilteredPatients() {
        return Transformations.switchMap(searchQuery, query -> {
            MutableLiveData<List<PatientChat>> filteredList = new MutableLiveData<>();
            List<PatientChat> filtered = new ArrayList<>();
            String filter = selectedFilter.getValue() != null ? selectedFilter.getValue() : "name";

            List<PatientChat> allPatients = patients.getValue();
            if (allPatients != null) {
                if (query == null || query.isEmpty()) {
                    filteredList.setValue(allPatients);
                } else {
                    for (PatientChat patient : allPatients) {
                        if (filter.equals("name") && patient.getName() != null &&
                                patient.getName().toLowerCase().contains(query.toLowerCase())) {
                            filtered.add(patient);
                        } else if (filter.equals("idNumber") && patient.getIdNumber() != null &&
                                patient.getIdNumber().toLowerCase().contains(query.toLowerCase())) {
                            filtered.add(patient);
                        }
                    }
                    filteredList.setValue(filtered);
                }
            }
            return filteredList;
        });
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSelectedFilter(String filter) {
        selectedFilter.setValue(filter);
    }
}