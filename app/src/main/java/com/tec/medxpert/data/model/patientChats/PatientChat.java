package com.tec.medxpert.data.model.patientChats;

public class PatientChat {
    private String name;
    private String userID;

    private String idNumber;

    public PatientChat(String name, String userID, String idNumber) {
        this.name = name;
        this.userID = userID;
        this.idNumber = idNumber;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getIdNumber() {
        return idNumber;
    }
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
}


