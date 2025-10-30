package com.tec.medxpert.data.model.patientChats;

import java.util.List;

public class ProfileChat {
    private String name;
    private String phone;
    private String email;
    private String bloodType;
    private Double weight;
    private Double height;
    private List<String> allergies;
    private List<String> personalMedicalHistory;
    private List<String> familyMedicalHistory;
    private String profilePicture;



    public ProfileChat(String name, String phone, String email, String bloodType, Double weight, Double height, List<String> allergies, List<String> personalMedicalHistory, List<String> familyMedicalHistory, String profilePicture) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.bloodType = bloodType;
        this.weight = weight;
        this.height = height;
        this.allergies = allergies;
        this.personalMedicalHistory = personalMedicalHistory;
        this.familyMedicalHistory = familyMedicalHistory;
        this.profilePicture = profilePicture;

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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
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
    public String getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
