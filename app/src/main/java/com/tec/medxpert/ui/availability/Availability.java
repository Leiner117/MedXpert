package com.tec.medxpert.ui.availability;


// Data and logic class for Availability

import com.google.firebase.firestore.Exclude;

public class Availability {

    @Exclude
    private String id;
    private String doctorId;

    private String date;
    private String time;

    public Availability() {} // Default constructor for Firebase

    public Availability(String date, String time, String doctorId) {
        this.date = date;
        this.time = time;
        this.doctorId = doctorId;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }

    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }


}