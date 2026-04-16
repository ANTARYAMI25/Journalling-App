package com.myorg.mapping.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Mapping {
    @JsonProperty("mappingId")
    private String mappingId;

    @JsonProperty("clientId")
    private String clientId;

    @JsonProperty("therapistId")
    private String therapistId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    // Constructors
    public Mapping() {
    }

    public Mapping(String mappingId, String clientId, String therapistId, String status, String createdAt, String updatedAt) {
        this.mappingId = mappingId;
        this.clientId = clientId;
        this.therapistId = therapistId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTherapistId() {
        return therapistId;
    }

    public void setTherapistId(String therapistId) {
        this.therapistId = therapistId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "Mapping{" +
                "mappingId='" + mappingId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", therapistId='" + therapistId + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}

