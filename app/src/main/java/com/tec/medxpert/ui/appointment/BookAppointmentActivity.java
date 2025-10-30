package com.tec.medxpert.ui.appointment;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.tec.medxpert.R;
import com.tec.medxpert.service.AppointmentNotificationScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
public class BookAppointmentActivity extends AppCompatActivity {

    private AutoCompleteTextView actvSpecialty, actvTime;
    private TextInputEditText etDate, etComments;
    private final List<String> availableTimes = new ArrayList<>();
    private String selectedDate;
    private BookAppointmentViewModel viewModel;
    private Disposable bookDisposable;
    private Disposable loadDisposable;
    private Disposable specialtiesDisposable;
    private ProgressBar progressBar;
    private static final String NO_AVAILABLE_TEXT = "There are no available times";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        // Initialization of UI components
        actvSpecialty = findViewById(R.id.actvSpecialty);
        actvTime = findViewById(R.id.actvTime);
        etDate = findViewById(R.id.tilDate);
        etComments = findViewById(R.id.etComments);
        Button btnBook = findViewById(R.id.btn_add_time_slot);
        ImageButton btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        // ViewModel initialization
        viewModel = new ViewModelProvider(this).get(BookAppointmentViewModel.class);

        // Event to navigate back
        btnBack.setOnClickListener(v -> finish());

        // Load specialties from the database
        loadDoctorSpecialties();

        // Event to select date
        etDate.setOnClickListener(v -> showDatePicker());

        // Event to select time
        btnBook.setOnClickListener(v -> bookAppointment());
    }

    // Method to load doctor specialties
    private void loadDoctorSpecialties() {
        specialtiesDisposable = viewModel.getDoctorSpecialties()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        specialties -> {
                            if (specialties.isEmpty()) {
                                Snackbar.make(findViewById(android.R.id.content),
                                        "No specialties found. Please update your profile.",
                                        Snackbar.LENGTH_LONG).show();
                            } else {
                                ArrayAdapter<String> specialtyAdapter =
                                        new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, specialties);
                                actvSpecialty.setAdapter(specialtyAdapter);
                            }
                        },
                        error -> Snackbar.make(findViewById(android.R.id.content),
                                "Error loading specialties", Snackbar.LENGTH_LONG).show()
                );
    }

    // Method to show the date picker dialog
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance(); // Get the current date

        DatePickerDialog dialog = new DatePickerDialog(this, R.style.CustomDatePickerDialog, (view, year, month, day) -> {
            selectedDate = day + "/" + (month + 1) + "/" + year;
            etDate.setText(selectedDate);

            loadAvailableTimes(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void loadAvailableTimes(String date) {
        progressBar.setVisibility(View.VISIBLE);
        loadDisposable = viewModel.getAvailableTimes(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(times -> {
                    progressBar.setVisibility(View.GONE);
                    availableTimes.clear();
                    actvTime.setText("");

                    if (times.isEmpty()) {
                        availableTimes.add(NO_AVAILABLE_TEXT);
                        Snackbar.make(findViewById(android.R.id.content), "No available time slots for this date", Snackbar.LENGTH_SHORT).show();
                    } else {
                        availableTimes.addAll(times);
                    }

                    actvTime.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, availableTimes));
                }, error -> {
                    progressBar.setVisibility(View.GONE);
                    availableTimes.clear();
                    actvTime.setText("");
                    actvTime.setAdapter(null);
                    Snackbar.make(findViewById(android.R.id.content), "Error loading available times", Snackbar.LENGTH_SHORT).show();
                });
    }

    // Method to book an appointment
    private void bookAppointment() {
        String specialty = actvSpecialty.getText().toString();
        String time = actvTime.getText().toString();
        String comment = etComments.getText().toString();

        if (specialty.isEmpty() || selectedDate == null || time.isEmpty() || NO_AVAILABLE_TEXT.equals(time)) {
            Snackbar.make(findViewById(android.R.id.content), "Please complete all fields correctly", Snackbar.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        bookDisposable = Single.zip(
                        viewModel.getPatientId(),
                        viewModel.getPatientName(),
                        (patientId, patientName) -> new PatientInfo(patientId, patientName)
                )
                .flatMap(patientInfo ->
                        viewModel.bookAppointment(patientInfo.id, selectedDate, time, specialty, comment)
                                .map(appointmentId -> new BookingResult(patientInfo, appointmentId))
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            progressBar.setVisibility(View.GONE);

                            scheduleAppointmentNotifications(
                                    result.patientInfo.name,
                                    selectedDate,
                                    time,
                                    result.appointmentId
                            );

                            Snackbar.make(findViewById(android.R.id.content), "Appointment booked successfully!", Snackbar.LENGTH_LONG).show();
                            clearForm();
                        },
                        error -> {
                            progressBar.setVisibility(View.GONE);
                            Log.e("BookAppointment", "Error booking appointment", error);
                            Snackbar.make(findViewById(android.R.id.content), "Error booking appointment: " + error.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                );
    }

    private void scheduleAppointmentNotifications(String patientName, String date, String time, String appointmentId) {
        AppointmentNotificationScheduler.scheduleAppointmentNotifications(
                this, patientName, date, time, appointmentId
        );
    }

    private static class BookingResult {
        final PatientInfo patientInfo;
        final String appointmentId;

        BookingResult(PatientInfo patientInfo, String appointmentId) {
            this.patientInfo = patientInfo;
            this.appointmentId = appointmentId;
        }
    }

    private static class PatientInfo {
        final String id;
        final String name;

        PatientInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // Method to clear the form after booking
    private void clearForm() {
        actvSpecialty.setText("");
        etDate.setText("");
        actvTime.setText("");
        etComments.setText("");
        selectedDate = null;
        availableTimes.clear();
        actvTime.setAdapter(null);
    }

    // Method to handle activity destruction
    @Override
    protected void onDestroy() {
        if (bookDisposable != null && !bookDisposable.isDisposed()) {
            bookDisposable.dispose();
        }

        if (loadDisposable != null && !loadDisposable.isDisposed()) {
            loadDisposable.dispose();
        }

        if (specialtiesDisposable != null && !specialtiesDisposable.isDisposed()) {
            specialtiesDisposable.dispose();
        }
        super.onDestroy();
    }
}