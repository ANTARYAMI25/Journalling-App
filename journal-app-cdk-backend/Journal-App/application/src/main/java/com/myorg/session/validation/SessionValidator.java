package com.myorg.session.validation;

import com.myorg.session.model.Session;

public class SessionValidator {
    private static final String[] VALID_STATUSES = {"available", "booked", "rejected", "Done", "completed", "cancelled"};

    public static void validateTherapistId(String therapistId) throws IllegalArgumentException {
        if (therapistId == null || therapistId.trim().isEmpty()) {
            throw new IllegalArgumentException("therapistId cannot be null or empty");
        }
    }

    public static void validateSessionId(String sessionId) throws IllegalArgumentException {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId cannot be null or empty");
        }
    }

    public static void validateClientId(String clientId) throws IllegalArgumentException {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("clientId cannot be null or empty");
        }
    }

    public static void validateTitle(String title) throws IllegalArgumentException {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("title cannot be null or empty");
        }
    }

    public static void validateScheduledAt(String scheduledAt) throws IllegalArgumentException {
        if (scheduledAt == null || scheduledAt.trim().isEmpty()) {
            throw new IllegalArgumentException("scheduledAt cannot be null or empty");
        }
    }

    public static void validateDurationMinutes(Integer durationMinutes) throws IllegalArgumentException {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be greater than 0");
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
            throw new IllegalArgumentException("Invalid status. Must be one of: available, booked, rejected, Done, completed, cancelled");
        }
    }

    public static void validateSessionInput(String title, String scheduledAt, Integer durationMinutes) throws IllegalArgumentException {
        validateTitle(title);
        validateScheduledAt(scheduledAt);
        validateDurationMinutes(durationMinutes);
    }

    public static void validateSessionExists(Session session) throws IllegalArgumentException {
        if (session == null) {
            throw new IllegalArgumentException("Session does not exist");
        }
    }
}

