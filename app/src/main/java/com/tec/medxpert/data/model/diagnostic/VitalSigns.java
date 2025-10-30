package com.tec.medxpert.data.model.diagnostic;

public class VitalSigns {
    private String heartbeat;
    private String temperature;
    private String bloodPressure;
    private String oxygenSaturation;

    public VitalSigns() {}

    public VitalSigns(String heartbeat, String temperature, String bloodPressure, String oxygenSaturation) {
        this.heartbeat = heartbeat;
        this.temperature = temperature;
        this.bloodPressure = bloodPressure;
        this.oxygenSaturation = oxygenSaturation;
    }

    public String getHeartbeat() {
        return heartbeat;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public String getOxygenSaturation() {
        return oxygenSaturation;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public void setHeartbeat(String heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setOxygenSaturation(String oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }
}
