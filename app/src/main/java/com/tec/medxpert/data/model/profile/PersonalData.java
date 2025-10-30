package com.tec.medxpert.data.model.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing personal data of a patient
 */
public class PersonalData {
    private String profilePicture;
    private String idType; // Added idType field
    private String name;
    private String phone;
    private String idNumber;
    private String bloodType;
    private Double weight;
    private Double height;
    private List<String> allergies;
    private List<String> personalMedicalHistory;
    private List<String> familyMedicalHistory;

    // Default constructor
    public PersonalData() {
        this.allergies = new ArrayList<>();
        this.personalMedicalHistory = new ArrayList<>();
        this.familyMedicalHistory = new ArrayList<>();
    }

    // Constructor with parameters
    public PersonalData(String profilePicture, String idType, String name, String phone, String idNumber,
                        String bloodType, Double weight, Double height, List<String> allergies,
                        List<String> personalMedicalHistory, List<String> familyMedicalHistory) {
        this.profilePicture = profilePicture;
        this.idType = idType;
        this.name = name;
        this.phone = phone;
        this.idNumber = idNumber;
        this.bloodType = bloodType;
        this.weight = weight;
        this.height = height;
        this.allergies = allergies != null ? allergies : new ArrayList<>();
        this.personalMedicalHistory = personalMedicalHistory != null ? personalMedicalHistory : new ArrayList<>();
        this.familyMedicalHistory = familyMedicalHistory != null ? familyMedicalHistory : new ArrayList<>();
    }

    // Getters and setters
    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getPersonalMedicalHistory() {
        return personalMedicalHistory;
    }

    public void setPersonalMedicalHistory(List<String> personalMedicalHistory) {
        this.personalMedicalHistory = personalMedicalHistory;
    }

    public List<String> getFamilyMedicalHistory() {
        return familyMedicalHistory;
    }

    public void setFamilyMedicalHistory(List<String> familyMedicalHistory) {
        this.familyMedicalHistory = familyMedicalHistory;
    }

    // Convert to Firestore Map
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("profilePicture", profilePicture);
        map.put("idType", idType); // Added idType to map
        map.put("name", name);
        map.put("phone", phone);
        map.put("idNumber", idNumber);
        map.put("bloodType", bloodType);
        map.put("weight", weight);
        map.put("height", height);
        map.put("allergies", allergies);
        map.put("personalMedicalHistory", personalMedicalHistory);
        map.put("familyMedicalHistory", familyMedicalHistory);
        return map;
    }

    // Create from Firestore Map
    public static PersonalData fromMap(Map<String, Object> map) {
        if (map == null) return new PersonalData();

        String profilePicture = (String) map.get("profilePicture");
        String idType = (String) map.get("idType"); // Get idType from map
        String name = (String) map.get("name");
        String phone = (String) map.get("phone");
        String idNumber = (String) map.get("idNumber");
        String bloodType = (String) map.get("bloodType");

        Double weight = null;
        if (map.get("weight") instanceof Number) {
            weight = ((Number) map.get("weight")).doubleValue();
        }

        Double height = null;
        if (map.get("height") instanceof Number) {
            height = ((Number) map.get("height")).doubleValue();
        }

        List<String> allergies = (List<String>) map.get("allergies");
        List<String> personalMedicalHistory = (List<String>) map.get("personalMedicalHistory");
        List<String> familyMedicalHistory = (List<String>) map.get("familyMedicalHistory");

        return new PersonalData(
                profilePicture, idType, name, phone, idNumber, bloodType, weight, height,
                allergies, personalMedicalHistory, familyMedicalHistory
        );
    }
}
