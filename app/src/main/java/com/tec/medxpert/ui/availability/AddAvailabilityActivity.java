package com.tec.medxpert.ui.availability;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.tec.medxpert.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

// Activity (user interface) to add availability

@AndroidEntryPoint
public class AddAvailabilityActivity extends AppCompatActivity {

    private AddAvailabilityViewModel viewModel;
    private AvailabilityAdapter adapter; // Adapter to manage the list of available time slots

    private Disposable addSlotDisposable; // Disposable to manage the subscription to the addAvailability method
    private Disposable fetchAvailabilityDisposable;
    private Disposable deleteSlotDisposable;
    private TextView tvAvailableTimeSlotsDate;

    private TextInputEditText etDate, etTime;

    private int lastSelectedHour = -1;
    private int lastSelectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_availability); // Load of layout

        // ViewModel to manage the data
        viewModel = new ViewModelProvider(this).get(AddAvailabilityViewModel.class);

        // Initialization of UI components
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        Button btnAddSlot = findViewById(R.id.btn_add_time_slot);
        ImageButton btnBack = findViewById(R.id.btn_back);
        tvAvailableTimeSlotsDate = findViewById(R.id.tv_available_time_slots_date);

        // RecyclerView to display available time slots
        RecyclerView recyclerTimeSlots = findViewById(R.id.recycler_time_slots);
        adapter = new AvailabilityAdapter();
        recyclerTimeSlots.setLayoutManager(new LinearLayoutManager(this));
        recyclerTimeSlots.setAdapter(adapter);

        // Event to navigate back
        btnBack.setOnClickListener(v -> finish());

        // Event to select date
        etDate.setOnClickListener(v -> showDatePicker());

        // Event to select time
        etTime.setOnClickListener(v -> showTimePicker());

        // Event to delete availability
        adapter.setOnDeleteClickListener(this::showDeleteConfirmationDialog);

        // Event to add availability
        btnAddSlot.setOnClickListener(v -> {

            // Get the selected date and time
            String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
            String time = etTime.getText() != null ? etTime.getText().toString().trim() : "";

            if (date.isEmpty() || time.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), "Please select a date and time", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Get the all available time slots for the selected date
            fetchAvailabilityDisposable = viewModel.getAvailabilityByDate(date)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMapCompletable(availabilityList -> {

                        List<String> times = new ArrayList<>(); // List to store the existing time slots
                        for (Availability a : availabilityList) {
                            times.add(a.getTime());
                        }
                        return viewModel.validateAndAddAvailability(date, time, times);
                    })
                    .subscribe(() -> {
                        Snackbar.make(findViewById(android.R.id.content), "Availability added successfully", Snackbar.LENGTH_SHORT).show();
                        etTime.setText("");
                        loadAndDisplaySlots(date); // Reload the available time slots
                    }, error -> {
                        String message = error.getMessage() != null ? error.getMessage() : "Error adding availability";
                        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
                    });
        });
    }


    // Load of available time slots for the selected date
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance(); // Get the current date

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDatePickerDialog, (view, year, month, day) -> {
                     String selectedDate = day + "/" + (month + 1) + "/" + year; // Create string with the selected date
                     etDate.setText(selectedDate);

                     tvAvailableTimeSlotsDate.setText(selectedDate); // Set the date in the TextView

                     // Load of available time slots for that date
                    loadAndDisplaySlots(selectedDate);

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Set the minimum date to today
        datePickerDialog.show();
    }

    // Method to show the time picker dialog
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance(); // Get the current time

        if (lastSelectedHour != -1 && lastSelectedMinute != -1) { // If a time was previously selected
            calendar.set(Calendar.HOUR_OF_DAY, lastSelectedHour);
            calendar.set(Calendar.MINUTE, lastSelectedMinute);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,  R.style.CustomTimePickerDialog,(view, hour, minute) -> {

            lastSelectedHour = hour;
            lastSelectedMinute = minute;

            int formatHour = (hour % 12 == 0 ? 12 : hour % 12);
            String period = hour < 12 ? "AM" : "PM";

            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", formatHour, minute, period);
            etTime.setText(selectedTime);

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }

    // Method to load and display available time slots for the selected date
    private void loadAndDisplaySlots(String date) {
        fetchAvailabilityDisposable = viewModel.getRepository().deletePastAvailabilities()
                .andThen(viewModel.getAvailabilityByDate(date))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.isEmpty()) {
                        Snackbar.make(findViewById(android.R.id.content), "No available time slots for this date", Snackbar.LENGTH_SHORT).show();
                        adapter.setData(Collections.emptyList()); // Clear the adapter data
                    } else {
                        sortAvailabilityByTime(list); // Sort the list by time
                        adapter.setData(list);
                    }
                }, error -> Snackbar.make(findViewById(android.R.id.content), "Could not load time slots", Snackbar.LENGTH_SHORT).show());
    }

    // Method to sort the availability list by time
    private void sortAvailabilityByTime(List<Availability> list) {
        list.sort((a1, a2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                Date t1 = sdf.parse(a1.getTime());
                Date t2 = sdf.parse(a2.getTime());
                return t1.compareTo(t2);
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void showDeleteConfirmationDialog(Availability availability) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button yesButton = dialogView.findViewById(R.id.yesButton);
        Button noButton = dialogView.findViewById(R.id.noButton);
        ImageView closeIcon = dialogView.findViewById(R.id.closeDeleteDialogDelete);

        yesButton.setOnClickListener(v -> {
            deleteSlotDisposable = viewModel.deleteAvailability(availability)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Snackbar.make(findViewById(android.R.id.content), "Availability deleted successfully", Snackbar.LENGTH_SHORT).show();
                    loadAndDisplaySlots(availability.getDate()); // Reload the available time slots
                    dialog.dismiss();
                }, error -> {
                    String message = error.getMessage() != null ? error.getMessage() : "Error deleting availability";
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
        });

        noButton.setOnClickListener(v -> dialog.dismiss());
        closeIcon.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (addSlotDisposable != null && !addSlotDisposable.isDisposed()) {
            addSlotDisposable.dispose();
        }
        if (fetchAvailabilityDisposable != null && !fetchAvailabilityDisposable.isDisposed()) {
            fetchAvailabilityDisposable.dispose();
        }

        if (deleteSlotDisposable != null && !deleteSlotDisposable.isDisposed()) {
            deleteSlotDisposable.dispose();
        }

        super.onDestroy();
    }
}
