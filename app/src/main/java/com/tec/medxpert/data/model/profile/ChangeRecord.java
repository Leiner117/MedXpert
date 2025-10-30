package com.tec.medxpert.data.model.profile;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing a change record in the patient profile
 */
public class ChangeRecord {
    @DocumentId
    private String recordId;
    private String patientId;
    private String userId;
    private String userName;
    private String fieldName;
    private String previousValue;
    private String currentValue;
    private Timestamp timestamp;

    // Default constructor required for Firestore
    public ChangeRecord() {
    }

    public ChangeRecord(String patientId, String userId, String userName, String fieldName,
                        String previousValue, String currentValue) {
        this.patientId = patientId;
        this.userId = userId;
        this.userName = userName;
        this.fieldName = fieldName;
        this.previousValue = previousValue;
        this.currentValue = currentValue;
        this.timestamp = Timestamp.now();
    }

    // Getters and setters
    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDate() {
        return timestamp != null ? timestamp.toDate() : null;
    }

    // Convert to Firestore Map
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("patientId", patientId);
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("fieldName", fieldName);
        map.put("previousValue", previousValue);
        map.put("currentValue", currentValue);
        map.put("timestamp", timestamp);
        return map;
    }

    // Create from Firestore Map
    public static ChangeRecord fromMap(Map<String, Object> map, String recordId) {
        ChangeRecord record = new ChangeRecord();
        record.recordId = recordId;
        record.patientId = (String) map.get("patientId");
        record.userId = (String) map.get("userId");
        record.userName = (String) map.get("userName");
        record.fieldName = (String) map.get("fieldName");
        record.previousValue = (String) map.get("previousValue");
        record.currentValue = (String) map.get("currentValue");
        record.timestamp = (Timestamp) map.get("timestamp");
        return record;
    }
}
