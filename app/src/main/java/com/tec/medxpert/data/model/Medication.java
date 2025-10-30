package com.tec.medxpert.data.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.Timestamp;


public class Medication implements Serializable {
    private String id;
    private String name;
    private String description;
    private String dosage;
    private Map<String, Integer> defaultFrequency;
    private transient Timestamp registrationDate;
    private transient Timestamp diagnosticUpdatedAt;

    public Medication() {
        // Default constructor required for calls to DataSnapshot.getValue(Medication.class)
    }

    public Medication(String id, String name, String description, String dosage,
                      Map<String, Integer> defaultFrequency, Timestamp registrationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dosage = dosage;
        this.defaultFrequency = defaultFrequency;
        this.registrationDate = registrationDate;
    }

    public Medication(String id, String name, String description, String dosage, Map<String, Integer> defaultFrequency) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dosage = dosage;
        this.defaultFrequency = defaultFrequency;
    }

    public Medication(Timestamp diagnosticUpdatedAt, String id, String name, String description, String dosage,
                      Map<String, Integer> defaultFrequency) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dosage = dosage;
        this.defaultFrequency = defaultFrequency;
        this.diagnosticUpdatedAt = diagnosticUpdatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDosage() {
        return dosage;
    }

    public Map<String, Integer> getDefaultFrequency() {
        return defaultFrequency;
    }

    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public void setDefaultFrequency(Map<String, Integer> defaultFrequency) {
        this.defaultFrequency = defaultFrequency;
    }

    public Timestamp getDiagnosticUpdatedAt() {
        return diagnosticUpdatedAt;
    }

    public String getFrecuencyUsageForPatientView() {
        if (defaultFrequency == null || !defaultFrequency.containsKey("everyHours") || !defaultFrequency.containsKey("days")) {
            return "Frequency data not available";
        }

        Integer hours = defaultFrequency.get("everyHours");
        Integer days = defaultFrequency.get("days");

        return String.format(Locale.getDefault(), "Every %d hours for %d days", hours, days);
    }

    public static Medication createSerializableCopy(Medication medication) {
        return new Medication(
                medication.getId(),
                medication.getName(),
                medication.getDescription(),
                medication.getDosage(),
                medication.getDefaultFrequency()
        );
    }
    public boolean isTerminated() {
        if (diagnosticUpdatedAt == null || defaultFrequency == null || !defaultFrequency.containsKey("days")) {
            return false;
        }

        Integer days = defaultFrequency.get("days");
        if (days == null || days <= 0) {
            return false;
        }

        long startTimeMillis = diagnosticUpdatedAt.toDate().getTime();
        long daysInMillis = days * 24 * 60 * 60 * 1000L;
        long endTimeMillis = startTimeMillis + daysInMillis;

        long currentTimeMillis = System.currentTimeMillis();

        return currentTimeMillis >= endTimeMillis;
    }

    public boolean isInUse() {
        return !isTerminated();
    }

    public Date getEndDate() {
        if (diagnosticUpdatedAt == null || defaultFrequency == null || !defaultFrequency.containsKey("days")) {
            return null;
        }

        Integer days = defaultFrequency.get("days");
        if (days == null || days <= 0) {
            return null;
        }

        long startTimeMillis = diagnosticUpdatedAt.toDate().getTime();
        long daysInMillis = days * 24 * 60 * 60 * 1000L;
        long endTimeMillis = startTimeMillis + daysInMillis;

        return new Date(endTimeMillis);
    }
}
