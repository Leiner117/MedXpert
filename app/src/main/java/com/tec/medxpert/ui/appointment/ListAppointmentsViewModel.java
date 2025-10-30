package com.tec.medxpert.ui.appointment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tec.medxpert.data.repository.AppointmentRepository;
import com.tec.medxpert.data.repository.profile.PatientRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class ListAppointmentsViewModel extends ViewModel {

    private final AppointmentRepository repository;

    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedFilter = new MutableLiveData<>("specialty");

    private final PatientRepository patientRepository;

    private Disposable appointmentsDisposable;

    @Inject
    public ListAppointmentsViewModel(AppointmentRepository repository, PatientRepository patientRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
    }

    public AppointmentRepository getRepository() {
        return repository;
    }

    // Method to get appointments for the logged-in user
    public Observable<List<Appointment>> getAppointments() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Observable.error(new IllegalStateException("User not logged in"));
        }
        String userId = user.getUid();

        return Observable.create(emitter -> {
            patientRepository.getPatientByUserId(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String patientDocId = documentSnapshot.getId();
                        appointmentsDisposable = repository.getAppointmentsForUser(patientDocId)
                                .map(list -> sortAppointments(list, false))
                                .subscribe(emitter::onNext, emitter::onError);
                    } else {
                        emitter.onError(new Exception("No patient profile found"));
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }

    // Method to cancel an appointment
    public void cancelAppointment(String appointmentId) {
        repository.cancelAppointment(appointmentId);
    }

    // Method to load appointments and update the LiveData
    private List<Appointment> sortAppointments(List<Appointment> list, boolean descending) {
        List<Appointment> sorted = new ArrayList<>(list);
        sorted.sort((a1, a2) -> {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "d/M/yyyy h:mm a", java.util.Locale.getDefault()
                );
                java.util.Date dt1 = sdf.parse(a1.getDate() + " " + a1.getTime());
                java.util.Date dt2 = sdf.parse(a2.getDate() + " " + a2.getTime());
                return descending ? dt2.compareTo(dt1) : dt1.compareTo(dt2);
            } catch (Exception e) {
                return 0;
            }
        });
        return sorted;
    }

    // Method to get the list of appointments
    public MutableLiveData<List<Appointment>> getAppointmentsLiveData() {
        return appointments;
    }

    // Method to get the search query for appointments
    public MutableLiveData<String> getSelectedFilter() {
        return selectedFilter;
    }

    // Method to set the search query for appointments
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    // Method to set the selected filter for appointments
    public void setSelectedFilter(String filter) {
        selectedFilter.setValue(filter);
    }

    // Method to get the list of appointments based on the current search query and filter
    public LiveData<List<Appointment>> getFilteredAppointments() {
        return Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return appointments;
            } else {
                MutableLiveData<List<Appointment>> filteredList = new MutableLiveData<>();
                List<Appointment> filtered = new ArrayList<>();
                String filter = selectedFilter.getValue();

                List<Appointment> currentAppointments = appointments.getValue();
                if (currentAppointments != null) {
                    for (Appointment app : currentAppointments) {
                        if (
                                ("specialty".equals(filter) && app.getSpecialty().toLowerCase().contains(query.toLowerCase())) ||
                                        ("date".equals(filter) && app.getDate() != null && app.getDate().toLowerCase().contains(query.toLowerCase()))
                        ) {
                            filtered.add(app);
                        }
                    }
                }
                filteredList.setValue(filtered);
                return filteredList;
            }
        });
    }

    public Observable<List<Appointment>> getUpcomingAppointments() {
        return getAppointmentsByStatus(Collections.singletonList("Pending"))
                .map(list -> sortAppointments(list, false));
    }

    public Observable<List<Appointment>> getHistoryAppointments() {
        return getAppointmentsByStatus(Arrays.asList("Completed", "Cancelled", "Missed"))
                .map(list -> sortAppointments(list, true));
    }

    private Observable<List<Appointment>> getAppointmentsByStatus(List<String> statuses) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Observable.error(new IllegalStateException("User not logged in"));
        }
        String userId = user.getUid();

        return Observable.create(emitter -> {
            patientRepository.getPatientByUserId(userId)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String patientDocId = documentSnapshot.getId();
                        appointmentsDisposable = repository.getAppointments(patientDocId, statuses)
                                .map(list -> sortAppointments(list, false))
                                .subscribe(emitter::onNext, emitter::onError);
                    } else {
                        emitter.onError(new Exception("No patient profile found"));
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }

    @Override
    protected void onCleared() {
        if (appointmentsDisposable != null && !appointmentsDisposable.isDisposed()) {
            appointmentsDisposable.dispose();
        }
        super.onCleared();
    }

}