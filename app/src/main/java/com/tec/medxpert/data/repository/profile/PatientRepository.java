package com.tec.medxpert.data.repository.profile;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tec.medxpert.data.model.profile.Patient;
import com.tec.medxpert.data.model.profile.PersonalData;
import com.tec.medxpert.data.model.profile.ChangeRecord;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.android.gms.tasks.Tasks;

import com.tec.medxpert.auth.MockAuthProvider;

import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repository class for handling patient data operations with Firestore
 */
@Singleton
public class PatientRepository {

    private final FirebaseFirestore firestore;
    private final CollectionReference patientsCollection;
    private final ChangeHistoryRepository changeHistoryRepository;
    private final MockAuthProvider mockAuthProvider;

    @Inject
    public PatientRepository(FirebaseFirestore firestore,
                             ChangeHistoryRepository changeHistoryRepository,
                             MockAuthProvider mockAuthProvider) {
        this.firestore = firestore;
        this.patientsCollection = firestore.collection("patients");
        this.changeHistoryRepository = changeHistoryRepository;
        this.mockAuthProvider = mockAuthProvider;
    }

    /**
     * Create a new patient in Firestore
     * @param patient The patient to create
     * @return Task with the document reference
     */
    public Task<DocumentReference> createPatient(Patient patient) {
        return patientsCollection.add(patient.toMap());
    }

    /**
     * Get a patient by ID
     * @param patientId The patient ID
     * @return Task with the patient document
     */
    public Task<DocumentSnapshot> getPatientById(String patientId) {
        return patientsCollection.document(patientId).get();
    }

    /**
     * Get a patient by user ID
     * @param userId The user ID
     * @return Task with the patient document
     */
    public Task<DocumentSnapshot> getPatientByUserId(String userId) {
        return patientsCollection.whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return task.getResult().getDocuments().get(0);
                    }
                    return null;
                });
    }

    /**
     * Get all patients
     * @return Task with the query snapshot
     */
    public Task<QuerySnapshot> getAllPatients() {
        return patientsCollection.get();
    }

    /**
     * Search patients by name or ID
     * @param query The search query
     * @return Task with the query snapshot
     */
    public Task<QuerySnapshot> searchPatients(String query) {
        String lowercaseQuery = query.toLowerCase();

        Task<QuerySnapshot> nameQuery = patientsCollection
                .whereGreaterThanOrEqualTo("personalData.name", lowercaseQuery)
                .whereLessThanOrEqualTo("personalData.name", lowercaseQuery + "\uf8ff")
                .get();

        Task<QuerySnapshot> idQuery = patientsCollection
                .whereGreaterThanOrEqualTo("personalData.idNumber", lowercaseQuery)
                .whereLessThanOrEqualTo("personalData.idNumber", lowercaseQuery + "\uf8ff")
                .get();

        return Tasks.whenAllSuccess(nameQuery, idQuery)
                .continueWith(task -> {
                    QuerySnapshot nameResults = (QuerySnapshot) task.getResult().get(0);
                    QuerySnapshot idResults = (QuerySnapshot) task.getResult().get(1);

                    // Combine results (this is a simple approach, might need refinement)
                    return nameResults;
                });
    }

    /**
     * Get patients sorted by name
     * @param direction The sort direction
     * @return Task with the query snapshot
     */
    public Task<QuerySnapshot> getPatientsSortedByName(Query.Direction direction) {
        return patientsCollection
                .orderBy("personalData.name", direction)
                .get();
    }

    /**
     * Get patients sorted by registration date
     * @param direction The sort direction
     * @return Task with the query snapshot
     */
    public Task<QuerySnapshot> getPatientsSortedByDate(Query.Direction direction) {
        return patientsCollection
                .orderBy("createdAt", direction)
                .get();
    }

    /**
     * Update patient personal data
     * @param patientId The patient ID
     * @param personalData The updated personal data
     * @return Task with void result
     */
    public Task<Void> updatePatientPersonalData(String patientId, PersonalData personalData) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("personalData", personalData.toMap());
        updates.put("updatedAt", Timestamp.now());

        return patientsCollection.document(patientId).update(updates);
    }

    /**
     * Update specific field in patient personal data and track the change
     * @param patientId The patient ID
     * @param field The field to update
     * @param value The new value
     * @return Task with void result
     */
    public Task<Void> updatePatientField(String patientId, String field, Object value) {
        // First get the current value
        return getPatientById(patientId)
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        return Tasks.forException(new Exception("Patient not found"));
                    }

                    Patient patient = Patient.fromMap(task.getResult().getData(), patientId);
                    PersonalData personalData = patient.getPersonalData();

                    // Get the previous value as string
                    String previousValue = getFieldValueAsString(personalData, field);

                    // Update the field
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("personalData." + field, value);
                    updates.put("updatedAt", Timestamp.now());

                    // Create a change record
                    String userId = mockAuthProvider.getUserId();
                    String userName = "Current User"; // In a real app, get the actual user name

                    String currentValue;
                    if (value == null) {
                        currentValue = "";
                    } else if (value instanceof java.util.List) {
                        // Format lists as comma-separated strings without brackets
                        currentValue = String.join(", ", (java.util.List<String>) value);
                    } else {
                        currentValue = value.toString();
                    }

                    ChangeRecord changeRecord = new ChangeRecord(
                            patientId,
                            userId,
                            userName,
                            getDisplayNameForField(field),
                            previousValue,
                            currentValue
                    );

                    // Update the field and add the change record
                    return Tasks.whenAll(
                            patientsCollection.document(patientId).update(updates),
                            changeHistoryRepository.addChangeRecord(changeRecord)
                    );
                });
    }

    /**
     * Delete a patient
     * @param patientId The patient ID
     * @return Task with void result
     */
    public Task<Void> deletePatient(String patientId) {
        return patientsCollection.document(patientId).delete();
    }

    /**
     * Get a field value as string
     * @param personalData The personal data
     * @param field The field name
     * @return The field value as string
     */
    private String getFieldValueAsString(PersonalData personalData, String field) {
        switch (field) {
            case "idType":
                return personalData.getIdType() != null ? personalData.getIdType() : "";
            case "idNumber":
                return personalData.getIdNumber() != null ? personalData.getIdNumber() : "";
            case "name":
                return personalData.getName() != null ? personalData.getName() : "";
            case "phone":
                return personalData.getPhone() != null ? personalData.getPhone() : "";
            case "bloodType":
                return personalData.getBloodType() != null ? personalData.getBloodType() : "";
            case "weight":
                return personalData.getWeight() != null ? personalData.getWeight().toString() : "";
            case "height":
                return personalData.getHeight() != null ? personalData.getHeight().toString() : "";
            case "allergies":
                return personalData.getAllergies() != null ? String.join(", ", personalData.getAllergies()) : "";
            case "personalMedicalHistory":
                return personalData.getPersonalMedicalHistory() != null ? String.join(", ", personalData.getPersonalMedicalHistory()) : "";
            case "familyMedicalHistory":
                return personalData.getFamilyMedicalHistory() != null ? String.join(", ", personalData.getFamilyMedicalHistory()) : "";
            default:
                return "";
        }
    }

    /**
     * Get a display name for a field
     * @param field The field name
     * @return The display name
     */
    private String getDisplayNameForField(String field) {
        switch (field) {
            case "idType":
                return "ID Type";
            case "idNumber":
                return "ID Number";
            case "name":
                return "Name";
            case "phone":
                return "Phone";
            case "bloodType":
                return "Blood Type";
            case "weight":
                return "Weight";
            case "height":
                return "Height";
            case "allergies":
                return "Allergies";
            case "personalMedicalHistory":
                return "Personal Medical History";
            case "familyMedicalHistory":
                return "Family Medical History";
            default:
                return field;
        }
    }
}

