package com.tec.medxpert.ui.appointment;

public class Appointment {

    private String id;
    private String date;
    private String time;
    private String specialty;
    private String comments;
    private String patientId;
    private String doctorId;
    private String status;

    public Appointment() {} /// Default constructor for Firebase

    public Appointment(String date, String time, String specialty, String comments, String patientId, String doctorId) {
        this.date = date;
        this.time = time;
        this.specialty = specialty;
        this.comments = comments;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

}
