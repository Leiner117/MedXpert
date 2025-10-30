package com.tec.medxpert.data.model.diagnostic;


import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Diagnostic {
    private String patient_Name;
    private String patient_Id;
    private String appointmentId;
    private String weight;
    private String physical_examination;
    private List<Medicine> medicineList;
    private String consultation_reason;
    private String subjective_condition;
    private String objective_condition;
    private String analysis_and_plan;
    private Timestamp updatedAt;
    private VitalSigns vitalSigns;
    private List<String> imageUrls;

    private String idNumber;

    public Diagnostic() {
        this.vitalSigns = new VitalSigns();
        this.imageUrls = new ArrayList<>();
    }

    public Diagnostic(String patientName, String patientId, String weight, String physical_examination, List<Medicine> medicineList,
                      String consultation_reason, String subjective_condition,
                      String objective_condition, String analysis_and_plan, Timestamp updatedAt,
                      VitalSigns vitalSigns, List<String> imageUrls) {
        this.patient_Name = patientName;
        this.patient_Id = patientId;
        this.weight = weight;
        this.physical_examination = physical_examination;
        this.medicineList = medicineList;
        this.consultation_reason = consultation_reason;
        this.subjective_condition = subjective_condition;
        this.objective_condition = objective_condition;
        this.analysis_and_plan = analysis_and_plan;
        this.updatedAt = updatedAt;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.vitalSigns = vitalSigns;
    }

    public String getPatientName() {return patient_Name;}
    public void setPatientName(String patient_Name) {this.patient_Name = patient_Name;}

    public String getPatientId() {return patient_Id;}

    public void setPatientId(String patient_Id) {this.patient_Id = patient_Id;}

    public void setHeartbeat(String heartbeat) {
        this.vitalSigns.setHeartbeat(heartbeat);
    }

    public void setTemperature(String temperature) {
        this.vitalSigns.setTemperature(temperature);
    }

    public void setBloodPressure(String bloodPressure) {
        this.vitalSigns.setBloodPressure(bloodPressure);
    }

    public void setOxygenSaturation(String oxygenSaturation) {
        this.vitalSigns.setOxygenSaturation(oxygenSaturation);
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getWeight() {
        return weight;
    }

    public void setPhysical_examination(String physical_examination) {
        this.physical_examination = physical_examination;
    }

    public String getPhysical_examination() {
        return physical_examination;
    }

    public void setMedicineList(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    public List<Medicine> getMedicineList() {
        return medicineList;
    }

    public void setConsultation_reason(String consultation_reason) {
        this.consultation_reason = consultation_reason;
    }

    public String getConsultation_reason() {
        return consultation_reason;
    }

    public void setSubjective_condition(String subjective_condition) {
        this.subjective_condition = subjective_condition;
    }

    public String getSubjective_condition() {
        return subjective_condition;
    }

    public void setObjective_condition(String objective_condition) {
        this.objective_condition = objective_condition;
    }

    public String getObjective_condition() {
        return objective_condition;
    }

    public void setAnalysis_and_plan(String analysis_and_plan) {
        this.analysis_and_plan = analysis_and_plan;
    }

    public String getAnalysis_and_plan() {
        return analysis_and_plan;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public VitalSigns getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(VitalSigns vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getIdNumber() {
        return idNumber;
    }
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
}
