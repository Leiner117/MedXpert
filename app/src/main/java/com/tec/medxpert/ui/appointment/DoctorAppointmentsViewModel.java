package com.tec.medxpert.ui.appointment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.data.repository.AppointmentRepository;
import com.tec.medxpert.data.repository.profile.DoctorRepository;
import com.tec.medxpert.data.repository.profile.PatientRepository;
import com.tec.medxpert.ui.appointment.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

@HiltViewModel
public class DoctorAppointmentsViewModel extends ViewModel {

    @Inject
    DoctorRepository doctorRepository;
    private final AppointmentRepository repository;
    private Disposable appointmentsDisposable;
    private final MutableLiveData<List<Appointment>> appointments = new MutableLiveData<>();
    @Inject
    public DoctorAppointmentsViewModel(AppointmentRepository repository, DoctorRepository doctorRepository) {
        this.repository = repository;
        this.doctorRepository = doctorRepository;
    }

    // Method to get appointments for the doctor
    public Observable<List<Appointment>> getAppointmentsForDoctor() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Observable.error(new IllegalStateException("User not logged in"));
        }
        String userId = user.getUid();

        // Get the doctor ID based on the logged-in user's ID
        return Observable.create(emitter -> {
            doctorRepository.getDoctorByUserId(userId)
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String doctorId = snapshot.getDocuments().get(0).getId();
                        appointmentsDisposable = repository.getAppointmentsForDoctor(doctorId)
                                .map(list -> sortAppointments(list, false))
                                .subscribe(emitter::onNext, emitter::onError);
                    } else {
                        emitter.onError(new Exception("No doctor profile found"));
                    }
                })
                .addOnFailureListener(emitter::onError);
        });
    }

    // Method to get future appointments for the doctor
    public Observable<List<Appointment>> getUpcomingAppointments() {
        return getAppointmentsForDoctor()
            .doOnNext(list -> {
                if (list.isEmpty()) {
                    System.out.println("No upcoming appointments found.");
                } else {
                    System.out.println("Appointments found: " + list.size());
                }
            })
            .doOnError(error -> System.err.println("Error fetching appointments: " + error.getMessage()))
            .map(list -> {
                List<Appointment> filtered = new ArrayList<>();
                for (Appointment a : list) {
                    if ("Pending".equalsIgnoreCase(a.getStatus())) {
                        filtered.add(a);
                    }
                }
                return filtered;
            });
    }

    // Method to get cancelled or completed appointments for the doctor
    public Observable<List<Appointment>> getHistoryAppointments() {
        return getAppointmentsForDoctor()
            .map(list -> {
                List<Appointment> filtered = new ArrayList<>();
                for (Appointment a : list) {
                    if ("Cancelled".equalsIgnoreCase(a.getStatus()) || "Completed".equalsIgnoreCase(a.getStatus())
                            || "Missed".equalsIgnoreCase(a.getStatus())) {
                        filtered.add(a);
                    }
                }
                return filtered;
            });
    }

    // Method to sort appointments by date and time
    public List<Appointment> sortAppointments(List<Appointment> list, boolean descending) {
        List<Appointment> sorted = new ArrayList<>(list);
        sorted.sort((a1, a2) -> {
            try {
                String[] dateFormats = {"d/M/yyyy", "yyyy-MM-dd"};
                String[] timeFormats = {"h:mm a", "HH:mm"};
                Date dt1 = null, dt2 = null;
                for (String df : dateFormats) {
                    for (String tf : timeFormats) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(df + " " + tf, Locale.getDefault());
                            dt1 = sdf.parse(a1.getDate() + " " + a1.getTime());
                            dt2 = sdf.parse(a2.getDate() + " " + a2.getTime());
                            if (dt1 != null && dt2 != null) break;
                        } catch (Exception ignored) {}
                    }
                    if (dt1 != null && dt2 != null) break;
                }
                if (dt1 == null || dt2 == null) return 0;
                return descending ? dt2.compareTo(dt1) : dt1.compareTo(dt2);
            } catch (Exception e) {
                return 0;
            }
        });
        return sorted;
    }

    // Method to get appointments live data
    public void setAppointments(List<Appointment> list) {
        appointments.setValue(list);
    }

    @Override
    protected void onCleared() {
        if (appointmentsDisposable != null && !appointmentsDisposable.isDisposed()) {
            appointmentsDisposable.dispose();
        }
        super.onCleared();
    }
}
