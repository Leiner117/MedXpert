package com.tec.medxpert.data.repository.profile;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tec.medxpert.data.model.profile.Doctor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DoctorRepository {
    private final CollectionReference doctorsCollection;
    private final CollectionReference usersCollection;

    @Inject
    public DoctorRepository(FirebaseFirestore firestore) {
        this.doctorsCollection = firestore.collection("doctors");
        this.usersCollection = firestore.collection("users");
    }

    public Task<DocumentReference> createDoctor(Doctor doctor) {
        return doctorsCollection.add(doctor);
    }

    public Task<Void> updateDoctor(Doctor doctor) {
        return doctorsCollection.document(doctor.getId()).set(doctor);
    }

    public Task<DocumentSnapshot> getDoctor(String doctorId) {
        return doctorsCollection.document(doctorId).get();
    }

    public Task<QuerySnapshot> getAllDoctors() {
        return doctorsCollection.get();
    }

    public Task<QuerySnapshot> getDoctorByUserId(String userId) {
        return doctorsCollection.whereEqualTo("userId", userId).get();
    }

    public Task<Void> deleteDoctor(String doctorId) {
        return doctorsCollection.document(doctorId).delete();
    }

    public Task<Void> updateUserRole(String userId, String role) {
        return usersCollection.document(userId).update("role", role);
    }

    public Task<DocumentSnapshot> getUserRole(String userId) {
        return usersCollection.document(userId).get();
    }
}
