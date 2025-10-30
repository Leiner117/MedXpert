package com.tec.medxpert.data.repository.profile;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tec.medxpert.data.model.profile.ChangeRecord;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository class for handling change history operations with Firestore
 */
@Singleton
public class ChangeHistoryRepository {

    private final FirebaseFirestore firestore;
    private final CollectionReference changeHistoryCollection;

    @Inject
    public ChangeHistoryRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
        this.changeHistoryCollection = firestore.collection("change_history");
    }

    /**
     * Add a new change record
     * @param record The change record to add
     * @return Task with the document reference
     */
    public Task<Void> addChangeRecord(ChangeRecord record) {
        return changeHistoryCollection.document().set(record.toMap());
    }

    /**
     * Get change history for a patient
     * @param patientId The patient ID
     * @return Task with the query snapshot
     */
    public Task<QuerySnapshot> getChangeHistoryForPatient(String patientId) {
        return changeHistoryCollection
                .whereEqualTo("patientId", patientId)
                //.orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get change history for a specific field of a patient
     * @param patientId The patient ID
     * @param fieldName The field name
     * @return Task with the query snapshot
     */
    public Task<QuerySnapshot> getChangeHistoryForField(String patientId, String fieldName) {
        return changeHistoryCollection
                .whereEqualTo("patientId", patientId)
                .whereEqualTo("fieldName", fieldName)
                //.orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }
}
