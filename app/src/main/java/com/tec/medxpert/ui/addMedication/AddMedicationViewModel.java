package com.tec.medxpert.ui.addMedication;

import static dagger.hilt.android.internal.Contexts.getApplication;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.Medication;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddMedicationViewModel extends ViewModel {

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();
    private final FirebaseFirestore firestore;
    @Inject
    Application application;

    @Inject
    public AddMedicationViewModel(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getNavigateBack() {
        return navigateBack;
    }

    public void onAddClicked(String name, String desc, String dosage, String hours, String days) {
        if (name.isEmpty() || desc.isEmpty() || dosage.isEmpty() || hours.isEmpty() || days.isEmpty()) {
            message.setValue(application.getString(R.string.fill_all_fields_message));
        } else {
            Map<String, Object> medication = new HashMap<>();
            medication.put("name", name);
            medication.put("description", desc);
            medication.put("dosage", dosage);
            medication.put("defaultFrequency", Map.of(
                    "hours", Integer.parseInt(hours),
                    "days", Integer.parseInt(days)
            ));
            medication.put("registrationDate", com.google.firebase.Timestamp.now());

            firestore.collection("medications")
                    .add(medication)
                    .addOnSuccessListener(documentReference -> {
                        message.setValue(application.getString(R.string.medication_added_successfully));
                        navigateBack.setValue(true);
                    })
                    .addOnFailureListener(e -> message.setValue(application.getString(R.string.failed_to_add_medication) + e.getMessage()));

            message.setValue(application.getString(R.string.medication_added));
        }
    }

    public void onSaveClicked(Medication medication, String name, String desc, String dosage, String hours, String days) {
        if (name.isEmpty() || desc.isEmpty() || dosage.isEmpty() || hours.isEmpty() || days.isEmpty()) {
            message.setValue(application.getString(R.string.fill_all_fields_message));
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("description", desc);
            updates.put("dosage", dosage);
            updates.put("defaultFrequency", Map.of(
                    "hours", Integer.parseInt(hours),
                    "days", Integer.parseInt(days)
            ));

            firestore.collection("medications")
                    .document(medication.getId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        message.setValue(application.getString(R.string.medication_updated_successfully));
                        navigateBack.setValue(true);
                    })
                    .addOnFailureListener(e -> message.setValue(application.getString(R.string.failed_to_update_medication) + e.getMessage()));
        }
    }

    public void onCancelClicked() {
        navigateBack.setValue(true);
    }
}
