package com.tec.medxpert.ui.addDiagnostic;

import static android.app.Activity.RESULT_OK;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tec.medxpert.R;
import com.tec.medxpert.data.model.diagnostic.Diagnostic;
import com.tec.medxpert.data.model.diagnostic.Medicine;
import com.tec.medxpert.data.model.diagnostic.VitalSigns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddDiagnosticViewModel extends ViewModel {
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();
    private final FirebaseFirestore firestore;
    private final MutableLiveData<List<Medicine>> medicineList = new MutableLiveData<>();
    private final MutableLiveData<List<Medicine>> selectedMedicines = new MutableLiveData<>();
    private final MutableLiveData<VitalSigns> vitalSigns = new MutableLiveData<>();
    private final MutableLiveData<List<Uri>> selectedImages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUploading = new MutableLiveData<>();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    @Inject
    public Application application;
    @Inject
    public AddDiagnosticViewModel(FirebaseFirestore firestore) {
        this.firestore = firestore;
        selectedMedicines.setValue(new ArrayList<>());
        selectedImages.setValue(new ArrayList<>());
        vitalSigns.setValue(new VitalSigns());
        isUploading.setValue(false);
        loadMedicinesFromFirestore();
    }

    public LiveData<List<Medicine>> getMedicineList() {
        return medicineList;
    }

    public LiveData<List<Medicine>> getSelectedMedicines() {
        return selectedMedicines;
    }


    public MutableLiveData<String> getMessage() {
        return message;
    }
    public MutableLiveData<Boolean> getNavigateBack() {
        return navigateBack;
    }

    public void onClickedBack() {
        navigateBack.setValue(true);
    }

    public LiveData<VitalSigns> getVitalSigns() {
        return vitalSigns;
    }

    public void onClickedSaveDiagnostic(String appointmentId, String patientId, String patientName,
                                        String weight, String physicalExamination,
                                        String reasonForConsultation, String subjectiveCondition,
                                        String objectiveCondition, String analysisPlan) {

        List<Uri> images = selectedImages.getValue();
        if (images != null && !images.isEmpty()) {
            isUploading.setValue(true);
            uploadImagesAndSaveDiagnostic(appointmentId, patientId, patientName, weight, physicalExamination, reasonForConsultation,
                    subjectiveCondition, objectiveCondition, analysisPlan, images);
        } else {
            saveDiagnosticWithoutImages(appointmentId, patientId, patientName, weight, physicalExamination, reasonForConsultation,
                    subjectiveCondition, objectiveCondition, analysisPlan);
        }

        uploadStatusAppointment(appointmentId);
    }

    public void uploadStatusAppointment(String appointmentId) {
        firestore.collection("appointments")
                .document(appointmentId)
                .update("status", "Completed")
                .addOnSuccessListener(aVoid -> {
                    message.setValue(application.getString(R.string.appointment_status_updated));
                })
                .addOnFailureListener(e -> {
                    message.setValue(application.getString(R.string.error_failed_update_appointment_status, e.getMessage()));
                });
    }

    private void uploadImagesAndSaveDiagnostic(String appointmentId, String patientId, String patientName,  String weight, String physicalExamination,
                                               String reasonForConsultation, String subjectiveCondition,
                                               String objectiveCondition, String analysisPlan,
                                               List<Uri> images) {

        List<String> imageUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (int i = 0; i < images.size(); i++) {
            Uri imageUri = images.get(i);
            String fileName = "diagnostic_images/" + System.currentTimeMillis() + "_" + i + ".jpg";
            StorageReference imageRef = storage.getReference().child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            imageUrls.add(downloadUri.toString());

                            if (uploadCount.incrementAndGet() == images.size()) {
                                saveDiagnosticWithImages(appointmentId, patientId, patientName, weight, physicalExamination, reasonForConsultation,
                                        subjectiveCondition, objectiveCondition, analysisPlan, imageUrls);
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        isUploading.setValue(false);
                        message.setValue(application.getString(R.string.error_uploading_image, e.getMessage()));
                    });
        }
    }

    private void saveDiagnosticWithImages(String appointmentId, String patientId, String patientName, String weight, String physicalExamination,
                                          String reasonForConsultation, String subjectiveCondition,
                                          String objectiveCondition, String analysisPlan,
                                          List<String> imageUrls) {

        Diagnostic diagnostic = createDiagnostic(appointmentId, patientId, patientName, weight, physicalExamination, reasonForConsultation,
                subjectiveCondition, objectiveCondition, analysisPlan);
        diagnostic.setImageUrls(imageUrls);

        addDiagnostic(diagnostic);
    }

    private void saveDiagnosticWithoutImages(String appointmentId, String patientId, String patientName, String weight, String physicalExamination,
                                             String reasonForConsultation, String subjectiveCondition,
                                             String objectiveCondition, String analysisPlan) {

        Diagnostic diagnostic = createDiagnostic(appointmentId, patientId, patientName, weight, physicalExamination, reasonForConsultation,
                subjectiveCondition, objectiveCondition, analysisPlan);
        diagnostic.setImageUrls(new ArrayList<>());

        addDiagnostic(diagnostic);
    }

    private Diagnostic createDiagnostic(String appointmentId, String patientId, String patientName,
                                        String weight, String physicalExamination,
                                        String reasonForConsultation, String subjectiveCondition,
                                        String objectiveCondition, String analysisPlan) {

        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setPatientName(patientName);
        diagnostic.setPatientId(patientId);
        diagnostic.setAppointmentId(appointmentId);
        diagnostic.setWeight(weight);
        diagnostic.setPhysical_examination(physicalExamination);
        diagnostic.setMedicineList(selectedMedicines.getValue());
        diagnostic.setConsultation_reason(reasonForConsultation);
        diagnostic.setSubjective_condition(subjectiveCondition);
        diagnostic.setObjective_condition(objectiveCondition);
        diagnostic.setAnalysis_and_plan(analysisPlan);

        VitalSigns currentVitalSigns = vitalSigns.getValue();
        if (currentVitalSigns != null) {
            diagnostic.setHeartbeat(currentVitalSigns.getHeartbeat());
            diagnostic.setTemperature(currentVitalSigns.getTemperature());
            diagnostic.setBloodPressure(currentVitalSigns.getBloodPressure());
            diagnostic.setOxygenSaturation(currentVitalSigns.getOxygenSaturation());
        }

        diagnostic.setUpdatedAt(new com.google.firebase.Timestamp(new java.util.Date()));
        return diagnostic;
    }

    public void updateVitalSign(VitalSignInputDialog.VitalSignType type, String value) {
        VitalSigns currentVitalSigns = vitalSigns.getValue();
        if (currentVitalSigns == null) {
            currentVitalSigns = new VitalSigns();
        }

        switch (type) {
            case HEARTBEAT:
                currentVitalSigns.setHeartbeat(value);
                break;
            case TEMPERATURE:
                currentVitalSigns.setTemperature(value);
                break;
            case BLOOD_PRESSURE:
                currentVitalSigns.setBloodPressure(value);
                break;
            case OXYGEN_SATURATION:
                currentVitalSigns.setOxygenSaturation(value);
                break;
        }

        vitalSigns.setValue(currentVitalSigns);
    }

    private void addDiagnostic(Diagnostic diagnostic) {
        firestore.collection("diagnostic")
                .add(diagnostic)
                .addOnSuccessListener(documentReference -> {
                    isUploading.setValue(false);
                    message.setValue(application.getString(R.string.diagnostic_added_successfully));
                    navigateBack.setValue(true);
                })
                .addOnFailureListener(e -> {
                    isUploading.setValue(false);
                    message.setValue(application.getString(R.string.error_failed_add_diagnostic, e.getMessage()));
                });
    }

    private void loadMedicinesFromFirestore() {
        firestore.collection("medications")
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        List<Medicine> medicines = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String name = document.getString("name");
                            String dosage = document.getString("dosage");

                            Medicine medicine = new Medicine(id, name, dosage);
                            medicines.add(medicine);
                        }
                        medicineList.setValue(medicines);
                    } else {
                        if (task.getException() != null) {
                            message.setValue(application.getString(R.string.error_generic, task.getException().getMessage()));
                        } else {
                            message.setValue(application.getString(R.string.error_unknown));
                        }
                    }
                });
    }

    public void addSelectedMedicine(Medicine medicine, int hours, int days) {
        List<Medicine> currentList = selectedMedicines.getValue();
        if (currentList != null) {
            Medicine selectedMedicine = new Medicine(
                    medicine.getId(),
                    medicine.getName(),
                    medicine.getDosage(),
                    hours,
                    days
            );

            boolean alreadyExists = false;
            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getId().equals(medicine.getId())) {
                    currentList.set(i, selectedMedicine);
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                currentList.add(selectedMedicine);
            }

            selectedMedicines.setValue(currentList);
        }
    }

    public void removeSelectedMedicine(int position) {
        List<Medicine> currentList = selectedMedicines.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            currentList.remove(position);
            selectedMedicines.setValue(currentList);
        }
    }

    public LiveData<List<Uri>> getSelectedImages() {
        return selectedImages;
    }

    public LiveData<Boolean> getIsUploading() {
        return isUploading;
    }

    public void addSelectedImage(Uri imageUri) {
        List<Uri> currentImages = selectedImages.getValue();
        if (currentImages == null) {
            currentImages = new ArrayList<>();
        }
        currentImages.add(imageUri);
        selectedImages.setValue(currentImages);
    }

    public void removeSelectedImage(int position) {
        List<Uri> currentImages = selectedImages.getValue();
        if (currentImages != null && position >= 0 && position < currentImages.size()) {
            currentImages.remove(position);
            selectedImages.setValue(currentImages);
        }
    }

}
