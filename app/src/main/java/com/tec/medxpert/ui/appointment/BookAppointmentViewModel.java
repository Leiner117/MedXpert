package com.tec.medxpert.ui.appointment;


import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import com.tec.medxpert.data.repository.AppointmentRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tec.medxpert.data.repository.profile.PatientRepository;

import io.reactivex.rxjava3.core.Single;

@HiltViewModel
public class BookAppointmentViewModel extends ViewModel {

    private final AppointmentRepository repository;

    private final PatientRepository patientRepository;

    private static final String NO_AVAILABLE_TEXT = "There are no available times";

    @Inject
    public BookAppointmentViewModel(AppointmentRepository repository, PatientRepository patientRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
    }

    // Get available times for a specific date
    public Observable<List<String>> getAvailableTimes(String date) {
        return repository.getAvailableTimes(date)
                .map(this::sortTimes); // Sort the times in ascending order
    }

    // Get the specialties of the doctor
    public Single<List<String>> getDoctorSpecialties() {
        return repository.getDoctorSpecialties();
    }

    // Get patient ID
    public Single<String> getPatientId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Single.error(new IllegalStateException("User not logged in"));
        }
        java.lang.String userId = user.getUid();
        return Single.create(emitter -> {
            patientRepository.getPatientByUserId(userId)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            emitter.onSuccess(documentSnapshot.getId());
                        } else {
                            emitter.onError(new Exception("No patient profile found"));
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    public Single<String> getPatientName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Single.error(new IllegalStateException("User not logged in"));
        }
        String userId = user.getUid();

        return Single.create(emitter -> {
            patientRepository.getPatientByUserId(userId)
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            String patientName = documentSnapshot.getString("personalData.name");

                            if (patientName != null && !patientName.isEmpty()) {
                                emitter.onSuccess(patientName);
                            } else {
                                emitter.onError(new Exception("Patient name not found"));
                            }
                        } else {
                            emitter.onError(new Exception("No patient profile found"));
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    // Book an appointment
    public Single<String> bookAppointment(String patientId, String date, String time, String specialty, String comment) {
        if (!isFormValid(date, time, specialty)) {
            return Single.error(new IllegalArgumentException("Form is not valid"));
        }

        return repository.getDoctorId()
                .flatMap(doctorId -> {
                    Appointment a = new Appointment(date, time, specialty, comment, patientId, doctorId);
                    a.setStatus("Pending");
                    return repository.bookAppointment(a);
                });
    }

    // Sort the times in ascending order
    private List<String> sortTimes(List<String> times) {
        List<String> sorted = new ArrayList<>(times);
        sorted.sort((t1, t2) -> {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US);
                return sdf.parse(t1).compareTo(sdf.parse(t2));
            } catch (Exception e) {
                return 0;
            }
        });
        return sorted;
    }


    //  Check if the form doesn't contain empty fields
    private boolean isFormValid(String date, String time, String specialty) {
        return specialty != null && !specialty.isEmpty()
                && date != null && !date.isEmpty()
                && time != null && !time.isEmpty()
                && !NO_AVAILABLE_TEXT.equals(time);
    }
}

