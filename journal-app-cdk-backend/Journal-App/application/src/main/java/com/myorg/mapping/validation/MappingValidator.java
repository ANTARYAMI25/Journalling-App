package com.myorg.mapping.validation;

import com.myorg.mapping.model.Mapping;

public class MappingValidator {
    private static final String[] VALID_STATUSES = {"pending", "approved", "rejected", "active", "inactive"};

    public static void validateClientId(String clientId) throws IllegalArgumentException {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
    }

    public static void validateTherapistId(String therapistId) throws IllegalArgumentException {
        if (therapistId == null || therapistId.trim().isEmpty()) {
            throw new IllegalArgumentException("therapistId cannot be null or empty");
        }
    }

    public static void validateMappingExists(Mapping mapping) throws IllegalArgumentException {
        if (mapping == null) {
            throw new IllegalArgumentException("Mapping does not exist");
        }
    }

    public static void validateStatus(String status) throws IllegalArgumentException {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("status cannot be null or empty");
        }

        boolean isValid = false;
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equalsIgnoreCase(status)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException("Invalid status. Must be one of: pending, approved, rejected, active, inactive");
        }
    }

    public static void validateMapping(Mapping mapping) throws IllegalArgumentException {
        if (mapping == null) {
            throw new IllegalArgumentException("Mapping cannot be null");
        }
        validateClientId(mapping.getClientId());
        validateTherapistId(mapping.getTherapistId());
        validateStatus(mapping.getStatus());
    }

    public static void validateUpdateStatusRequest(String status) throws IllegalArgumentException {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("status is required");
        }

        if (!status.equalsIgnoreCase("approved") && !status.equalsIgnoreCase("rejected")) {
            throw new IllegalArgumentException("Invalid status. Must be 'approved' or 'rejected'");
        }
    }
}

