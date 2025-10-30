package com.tec.medxpert.data.repository;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.ui.appointment.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

@Singleton
public class AppointmentRepository {

    private final FirebaseFirestore firestore;
    private Disposable disposableAvailability;

    @Inject
    public AppointmentRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    // Get available times for a specific date
    public Observable<List<String>> getAvailableTimes(String date) {
        return Observable.create(emitter -> {
            disposableAvailability = new AvailabilityRepository(firestore).deletePastAvailabilities()
                    .subscribe(() -> {
                        firestore.collection("availabilities")
                                .whereEqualTo("date", date)
                                .get()
                                .addOnSuccessListener(availabilitySnapshot -> {
                                    List<String> availableTimes = new ArrayList<>();
                                    for (DocumentSnapshot doc : availabilitySnapshot.getDocuments()) {
                                        String time = doc.getString("time");
                                        if (time != null) availableTimes.add(time);
                                    }
                                    firestore.collection("appointments")
                                            .whereEqualTo("date", date)
                                            .get()
                                            .addOnSuccessListener(appointmentSnapshot -> {
                                                List<String> reservedTimes = new ArrayList<>();
                                                for (DocumentSnapshot doc : appointmentSnapshot.getDocuments()) {
                                                    String time = doc.getString("time");
                                                    if (time != null) reservedTimes.add(time);
                                                }
                                                availableTimes.removeAll(reservedTimes);
                                                emitter.onNext(availableTimes);
                                                emitter.onComplete();
                                            })
                                            .addOnFailureListener(emitter::onError);
                                })
                                .addOnFailureListener(emitter::onError);
                    }, emitter::onError);
        });
    }

    // Get the specialties of the doctor
    public Single<List<String>> getDoctorSpecialties() {
        return Single.create(emitter ->
                firestore.collection("doctors")
                        .limit(1) // First doctor because just exists one
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                DocumentSnapshot doc = snapshot.getDocuments().get(0);
                                List<String> specialties = (List<String>) doc.get("specialties");
                                emitter.onSuccess(specialties != null ? specialties : new ArrayList<>());
                            } else {
                                emitter.onError(new Exception("No doctor found"));
                            }
                        })
                        .addOnFailureListener(emitter::onError)
        );
    }

    // Get doctor ID
    public Single<String> getDoctorId() {
        return Single.create(emitter ->
                firestore.collection("doctors")
                        .limit(1) // First doctor because just exists one
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (!snapshot.isEmpty()) {
                                DocumentSnapshot doc = snapshot.getDocuments().get(0);
                                String doctorId = doc.getId();
                                emitter.onSuccess(doctorId);
                            } else {
                                emitter.onError(new Exception("No doctor found"));
                            }
                        })
                        .addOnFailureListener(emitter::onError)
        );
    }

    // Book an appointment
    public Single<String> bookAppointment(Appointment appointment) {
        return Single.create(emitter -> {
            firestore.collection("appointments")
                    .whereEqualTo("date", appointment.getDate())
                    .whereEqualTo("time", appointment.getTime())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {

                        if (!querySnapshot.isEmpty()) {
                            emitter.onError(new Exception("Time slot already booked"));
                        } else {
                            firestore.collection("appointments")
                                    .add(appointment)
                                    .addOnSuccessListener(documentReference -> {
                                        String appointmentId = documentReference.getId();
                                        emitter.onSuccess(appointmentId);
                                    })
                                    .addOnFailureListener(emitter::onError);
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    // Get appointments for the current user
    public Observable<List<Appointment>> getAppointmentsForUser( String patientId) {
        return Observable.create(emitter -> {
            firestore.collection("appointments")
                    .whereEqualTo("patientId", patientId)
                    .whereIn("status", Collections.singletonList("Pending"))
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Appointment> appointments = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Appointment appointment = doc.toObject(Appointment.class);
                            if (appointment != null) {
                                appointment.setId(doc.getId());
                                appointments.add(appointment);
                            }
                        }
                        emitter.onNext(appointments);
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    // Cancel an appointment
    public void cancelAppointment(String appointmentId) {
        firestore.collection("appointments")
            .document(appointmentId)
            .update("status", "Cancelled");
    }

    public Observable<List<Appointment>> getAppointments(String patientId, List<String> statuses) {
        return Observable.create(emitter -> {
            firestore.collection("appointments")
                    .whereEqualTo("patientId", patientId)
                    .whereIn("status", statuses)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Appointment> appointments = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Appointment appointment = doc.toObject(Appointment.class);
                            if (appointment != null) {
                                appointment.setId(doc.getId());
                                appointments.add(appointment);
                            }
                        }
                        emitter.onNext(appointments);
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    // Get appointments for a specific doctor
    public Observable<List<Appointment>> getAppointmentsForDoctor(String doctorId) {
        return Observable.create(emitter -> {
            firestore.collection("appointments")
                    .whereEqualTo("doctorId", doctorId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Appointment> appointments = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Appointment appointment = doc.toObject(Appointment.class);
                            if (appointment != null) {
                                appointment.setId(doc.getId());
                                appointments.add(appointment);
                            }
                        }
                        emitter.onNext(appointments);
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    // Get upcoming appointments for a specific doctor
    public Observable<Object> getPatientId(String userId) {
        return Observable.create(emitter -> {
                firestore.collection("patients")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                                String patientId = doc.getId();
                                emitter.onNext(patientId);
                                emitter.onComplete();
                            } else {
                                emitter.onError(new Exception("No patient profile found"));
                            }
                        })
                        .addOnFailureListener(emitter::onError);
            });
        }

    public Completable updateMissedAppointments() {
        return Completable.create(emitter -> {
            firestore.collection("appointments")
                    .whereEqualTo("status", "Pending")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Completable> updates = new ArrayList<>();
                        Date now = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy h:mm a", Locale.getDefault());
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String date = doc.getString("date");
                            String time = doc.getString("time");
                            try {
                                Date appointmentDate = sdf.parse(date + " " + time);
                                if (appointmentDate != null && appointmentDate.before(now)) {
                                    updates.add(Completable.create(e ->
                                            doc.getReference().update("status", "Missed")
                                                    .addOnSuccessListener(a -> e.onComplete())
                                                    .addOnFailureListener(e::onError)
                                    ));
                                }
                            } catch (Exception ignored) {}
                        }
                        if (updates.isEmpty()) {
                            emitter.onComplete();
                        } else {
                            Completable.merge(updates).subscribe(emitter::onComplete, emitter::onError);
                        }
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    public void clear() {
        if (disposableAvailability != null && !disposableAvailability.isDisposed()) {
            disposableAvailability.dispose();
        }
    }
}