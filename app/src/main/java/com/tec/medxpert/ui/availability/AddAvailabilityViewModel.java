package com.tec.medxpert.ui.availability;

import androidx.lifecycle.ViewModel;

import com.tec.medxpert.data.repository.AvailabilityRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Connects the UI(activity) with the model(data)

@HiltViewModel
public class AddAvailabilityViewModel extends ViewModel {

    private final AvailabilityRepository repository;

    @Inject
    public AddAvailabilityViewModel(AvailabilityRepository repository) {
        this.repository = repository;
    }

    public Completable addAvailability(String date, String time) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return Completable.error(new IllegalStateException("User not logged in"));
        }

        String doctorId = user.getUid();
        Availability availability = new Availability(date, time, doctorId);
        return repository.addAvailability(availability);
    }

    public AvailabilityRepository getRepository() {
        return repository;
    }

    public Observable<List<Availability>> getAvailabilityByDate(String selectedDate) {
        return repository.getAvailabilityByDate(selectedDate);
    }

    public Completable deleteAvailability(Availability availability) {
        return repository.deleteAvailability(availability);
    }

    public Completable validateAndAddAvailability(String date, String time, List<String> existingTimes) {
        // Validate if the date and time are in the future
        if (!isDateTimeInFuture(date, time)) {
            return Completable.error(new IllegalArgumentException(
                    "Date and time must be in the future."
            ));
        }
        // Validate if the selected time is at least one hour apart from existing time slots
        if (!isOneHourApart(time, existingTimes)) {
            return Completable.error(new IllegalArgumentException(
                    "Time must be at least one hour apart from existing time slots."
            ));
        }
        return addAvailability(date, time);
    }

    private boolean isDateTimeInFuture(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm a", Locale.US);
            Date selected = sdf.parse(date + " " + time);
            return selected != null && selected.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // Method to check difference between the selected time and existing time slots
    public boolean isOneHourApart(String newTime, List<String> existingTimes) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date newDate = sdf.parse(normalizeTime(newTime));
            for (String time : existingTimes) {
                Date existingDate = sdf.parse(normalizeTime(time));
                long diffMillis = Math.abs(newDate.getTime() - existingDate.getTime());
                long diffMinutes = diffMillis / (60 * 1000);
                if (diffMinutes < 60) { // Less than 60 minutes difference
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Method to normalize the time format
    private String normalizeTime(String time) {
        try {
            // Parse the time string and format it to a standard format(hh:mm a)
            SimpleDateFormat parser = new SimpleDateFormat("hh:mm a", Locale.US);
            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = parser.parse(time);
            return formatter.format(date);
        } catch (Exception e) {
            return time;
        }
    }
}
