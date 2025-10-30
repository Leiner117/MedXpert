package com.tec.medxpert.data.repository.profileChatPatient;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tec.medxpert.data.model.patientChats.ProfileChat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ProfileChatRepository {
    private final FirebaseFirestore firestore;

    @Inject
    public ProfileChatRepository(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void getProfileChatByUserId(String userId, OnProfileChatLoadedCallback callback) {
        firestore.collection("users").document(userId).get()
        .addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful() && userTask.getResult() != null) {
                String email = userTask.getResult().getString("email");
                String profilePicture = userTask.getResult().getString("profilePicture");

                firestore.collection("patients")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(patientTask -> {
                    if (patientTask.isSuccessful()) {
                        List<ProfileChat> profileChats = new ArrayList<>();
                        for (QueryDocumentSnapshot document : patientTask.getResult()) {
                            String name = document.getString("personalData.name");
                            String phone = document.getString("personalData.phone");
                            String bloodType = document.getString("personalData.bloodType");
                            Double weight = document.getDouble("personalData.weight");
                            Double height = document.getDouble("personalData.height");
                            List<String> allergies = (List<String>) document.get("personalData.allergies");
                            List<String> personalMedicalHistory = (List<String>) document.get("personalData.personalMedicalHistory");
                            List<String> familyMedicalHistory = (List<String>) document.get("personalData.familyMedicalHistory");

                            ProfileChat profileChat = new ProfileChat(
                                    name, phone, email, bloodType, weight, height, allergies, personalMedicalHistory, familyMedicalHistory, profilePicture
                            );
                            profileChats.add(profileChat);
                        }
                        callback.onSuccess(profileChats);
                    } else {
                        callback.onFailure(patientTask.getException());
                    }
                });
            } else {
                callback.onFailure(userTask.getException());
            }
        });
    }


    public interface OnProfileChatLoadedCallback {
        void onSuccess(List<ProfileChat> profileChats);
        void onFailure(Exception e);
    }
}