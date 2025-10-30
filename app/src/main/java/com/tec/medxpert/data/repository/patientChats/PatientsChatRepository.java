package com.tec.medxpert.data.repository.patientChats;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tec.medxpert.data.model.patientChats.PatientChat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PatientsChatRepository {
    private final FirebaseFirestore firestore;

    @Inject
    public PatientsChatRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void getPatients(OnPatientsLoadedCallback callback) {
        firestore.collection("patients")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<PatientChat> patients = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.get("personalData.name", String.class);
                            String userId = document.getString("userId");
                            String idNumber = document.getString("personalData.idNumber");
                            patients.add(new PatientChat(name, userId, idNumber));
                        }
                        callback.onSuccess(patients);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public interface OnPatientsLoadedCallback {
        void onSuccess(List<PatientChat> patients);
        void onFailure(Exception e);
    }
}