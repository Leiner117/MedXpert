package com.tec.medxpert.data.model.diagnostic;

public class Medicine {
    private String id;
    private String name;
    private String dosage;
    private int hours;
    private int days;

    public Medicine() {
    }

    public Medicine(String id, String name, String dosage) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
    }

    public Medicine(String id, String name, String dosage, int hours, int days) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.hours = hours;
        this.days = days;
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

    public void setName(String name) {
        this.name = name;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}
