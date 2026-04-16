package com.myorg.session.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Session {
    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("therapistId")
    private String therapistId;

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("scheduledAt")
    private String scheduledAt;

    @JsonProperty("durationMinutes")
    private Integer durationMinutes;

    @JsonProperty("status")
    private String status;

    @JsonProperty("privateNotes")
    private String privateNotes;

    @JsonProperty("sharedNotes")
    private String sharedNotes;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("location")
    private String location;

    // Constructors
    public Session() {
    }

    public Session(String sessionId, String therapistId, String clientId, String title, 
                   String scheduledAt, Integer durationMinutes, String status, 
                   String privateNotes, String sharedNotes, String createdAt, String updatedAt) {
        this.sessionId = sessionId;
        this.therapistId = therapistId;
        this.clientId = clientId;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.privateNotes = privateNotes;
        this.sharedNotes = sharedNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Session(String sessionId, String therapistId, String clientId, String title, 
                   String scheduledAt, Integer durationMinutes, String status, 
                   String privateNotes, String sharedNotes, String createdAt, String updatedAt, String location) {
        this.sessionId = sessionId;
        this.therapistId = therapistId;
        this.clientId = clientId;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.privateNotes = privateNotes;
        this.sharedNotes = sharedNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.location = location;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(String therapistId) {
        this.therapistId = therapistId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(String scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrivateNotes() {
        return privateNotes;
    }

    public void setPrivateNotes(String privateNotes) {
        this.privateNotes = privateNotes;
    }

    public String getSharedNotes() {
        return sharedNotes;
    }

    public void setSharedNotes(String sharedNotes) {
        this.sharedNotes = sharedNotes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", therapistId='" + therapistId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", title='" + title + '\'' +
                ", scheduledAt='" + scheduledAt + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", status='" + status + '\'' +
                ", privateNotes='" + privateNotes + '\'' +
                ", sharedNotes='" + sharedNotes + '\'' +
                ", location='" + location + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}

