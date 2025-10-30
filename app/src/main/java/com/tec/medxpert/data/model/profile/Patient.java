package com.tec.medxpert.data.model.profile;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a patient in the system
 */
public class Patient {
    @DocumentId
    private String patientId;
    private String userId;
    private PersonalData personalData;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default constructor required for Firestore
    public Patient() {
        this.personalData = new PersonalData();
    }

    public Patient(String userId, PersonalData personalData) {
        this.userId = userId;
        this.personalData = personalData;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    // Getters and setters
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }

    public void setPersonalData(PersonalData personalData) {
        this.personalData = personalData;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Convert to Firestore Map
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("personalData", personalData.toMap());
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }

    // Create from Firestore Map
    public static Patient fromMap(Map<String, Object> map, String patientId) {
        Patient patient = new Patient();
        patient.patientId = patientId;
        patient.userId = (String) map.get("userId");

        Map<String, Object> personalDataMap = (Map<String, Object>) map.get("personalData");
        patient.personalData = PersonalData.fromMap(personalDataMap);

        // Handle different timestamp formats safely
        Object createdAtObj = map.get("createdAt");
        if (createdAtObj instanceof Timestamp) {
            patient.createdAt = (Timestamp) createdAtObj;
        } else if (createdAtObj instanceof Long) {
            // Convert Long to Timestamp
            patient.createdAt = new Timestamp(new Date((Long) createdAtObj));
        } else if (createdAtObj instanceof Map) {
            // Handle server timestamp special object
            Map<String, Object> timestampMap = (Map<String, Object>) createdAtObj;
            if (timestampMap.containsKey("seconds") && timestampMap.containsKey("nanoseconds")) {
                long seconds = (Long) timestampMap.get("seconds");
                int nanoseconds = ((Long) timestampMap.get("nanoseconds")).intValue();
                patient.createdAt = new Timestamp(seconds, nanoseconds);
            } else {
                patient.createdAt = Timestamp.now(); // Fallback
            }
        } else {
            patient.createdAt = Timestamp.now(); // Fallback
        }

        // Handle different timestamp formats safely for updatedAt
        Object updatedAtObj = map.get("updatedAt");
        if (updatedAtObj instanceof Timestamp) {
            patient.updatedAt = (Timestamp) updatedAtObj;
        } else if (updatedAtObj instanceof Long) {
            // Convert Long to Timestamp
            patient.updatedAt = new Timestamp(new Date((Long) updatedAtObj));
        } else if (updatedAtObj instanceof Map) {
            // Handle server timestamp special object
            Map<String, Object> timestampMap = (Map<String, Object>) updatedAtObj;
            if (timestampMap.containsKey("seconds") && timestampMap.containsKey("nanoseconds")) {
                long seconds = (Long) timestampMap.get("seconds");
                int nanoseconds = ((Long) timestampMap.get("nanoseconds")).intValue();
                patient.updatedAt = new Timestamp(seconds, nanoseconds);
            } else {
                patient.updatedAt = Timestamp.now(); // Fallback
            }
        } else {
            patient.updatedAt = Timestamp.now(); // Fallback
        }

        return patient;
    }
}
