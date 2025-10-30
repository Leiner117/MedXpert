package com.tec.medxpert.data.model.profile;

import java.util.List;

public class Doctor {
    private String id;
    private String userId; // ID del usuario en Firebase Auth
    private String name;
    private String identification;
    private String phone;
    private String email;
    private String medicalCode;
    private String consultationFocus;
    private List<String> specialties;
    private List<Education> education;
    private List<WorkExperience> workExperience;

    public Doctor() {
        // Required empty constructor for Firestore
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
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

    public String getMedicalCode() {
        return medicalCode;
    }

    public void setMedicalCode(String medicalCode) {
        this.medicalCode = medicalCode;
    }

    public String getConsultationFocus() {
        return consultationFocus;
    }

    public void setConsultationFocus(String consultationFocus) {
        this.consultationFocus = consultationFocus;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public List<Education> getEducation() {
        return education;
    }

    public void setEducation(List<Education> education) {
        this.education = education;
    }

    public List<WorkExperience> getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(List<WorkExperience> workExperience) {
        this.workExperience = workExperience;
    }

    public static class Education {
        private String degree;
        private String institution;
        private String year;
        private String startDate;
        private String endDate;

        public Education() {
            // Required empty constructor for Firestore
        }

        public Education(String degree, String institution, String year) {
            this.degree = degree;
            this.institution = institution;
            this.year = year;
        }

        public Education(String degree, String institution, String startDate, String endDate) {
            this.degree = degree;
            this.institution = institution;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getDegree() {
            return degree;
        }

        public void setDegree(String degree) {
            this.degree = degree;
        }

        public String getInstitution() {
            return institution;
        }

        public void setInstitution(String institution) {
            this.institution = institution;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    public static class WorkExperience {
        private String position;
        private String company;
        private String period;
        private String startDate;
        private String endDate;

        public WorkExperience() {
            // Required empty constructor for Firestore
        }

        public WorkExperience(String position, String company, String period) {
            this.position = position;
            this.company = company;
            this.period = period;
        }

        public WorkExperience(String position, String company, String startDate, String endDate) {
            this.position = position;
            this.company = company;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }
}
