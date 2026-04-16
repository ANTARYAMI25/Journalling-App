package com.myorg.session.service;

import com.myorg.session.model.Session;
import com.myorg.session.repository.SessionRepository;
import com.myorg.session.validation.SessionValidator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(String tableName) {
        this.sessionRepository = new SessionRepository(tableName);
    }

    /**
     * Create a new session
     */
    public Session createSession(String therapistId, String title, String scheduledAt,
                                 Integer durationMinutes, String privateNotes, String sharedNotes, String location)
            throws IllegalArgumentException {
        SessionValidator.validateTherapistId(therapistId);
        SessionValidator.validateSessionInput(title, scheduledAt, durationMinutes);

        // Reject if the therapist already has an overlapping session
        Instant newStart;
        try {
            newStart = Instant.parse(scheduledAt);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid scheduledAt format. Use ISO 8601 with timezone (e.g. 2024-01-15T10:00:00Z)");
        }
        Instant newEnd = newStart.plus(durationMinutes, ChronoUnit.MINUTES);
        List<Session> existingSessions = sessionRepository.getSessionsByTherapistConsistent(therapistId);
        for (Session existing : existingSessions) {
            if (existing.getScheduledAt() == null || existing.getDurationMinutes() == null) continue;
            Instant existStart;
            try {
                existStart = Instant.parse(existing.getScheduledAt());
            } catch (Exception e) {
                continue; // skip existing sessions with unparseable dates
            }
            Instant existEnd = existStart.plus(existing.getDurationMinutes(), ChronoUnit.MINUTES);
            if (newStart.isBefore(existEnd) && newEnd.isAfter(existStart)) {
                throw new IllegalArgumentException(
                        "Therapist already has a session scheduled during this time");
            }
        }

        Session session = new Session(
                UUID.randomUUID().toString(),
                therapistId,
                null,  // clientId is null initially
                title,
                scheduledAt,
                durationMinutes,
                "available",
                privateNotes,
                sharedNotes,
                Instant.now().toString(),
                null,  // updatedAt is null on first insertion
                location
        );

        return sessionRepository.createSession(session);
    }

    /**
     * Update session
     */
    public Session updateSession(String sessionId, String therapistId, String title, String scheduledAt,
                                 Integer durationMinutes, String status, String privateNotes, String sharedNotes,
                                 String location)
            throws IllegalArgumentException {
        SessionValidator.validateSessionId(sessionId);
        SessionValidator.validateTherapistId(therapistId);
        SessionValidator.validateSessionInput(title, scheduledAt, durationMinutes);
        SessionValidator.validateStatus(status);

        Session existingSession = sessionRepository.getSessionByKeys(sessionId, therapistId);
        SessionValidator.validateSessionExists(existingSession);

        String resolvedLocation = (location != null) ? location : existingSession.getLocation();

        Session updatedSession = new Session(
                sessionId,
                therapistId,
                existingSession.getClientId(),
                title,
                scheduledAt,
                durationMinutes,
                status,
                privateNotes,
                sharedNotes,
                existingSession.getCreatedAt(),
                Instant.now().toString(),
                resolvedLocation
        );

        return sessionRepository.updateSession(sessionId, therapistId, updatedSession);
    }

    /**
     * Get all sessions regardless of status
     */
    public List<Session> getAllSessions() {
        return sessionRepository.getAllSessions();
    }

    /**
     * Get all available sessions
     */
    public List<Session> getAllAvailableSessions() {
        return sessionRepository.getAllAvailableSessions();
    }

    /**
     * Get available sessions with optional location filter
     */
    public List<Session> getAvailableSessionsWithFilters(String location) {
        return sessionRepository.getAvailableSessionsWithFilters(location);
    }

    /**
     * Request session appointment (client booking a session)
     */
    public Session requestSessionAppointment(String sessionId, String therapistId, String clientId)
            throws IllegalArgumentException {
        SessionValidator.validateSessionId(sessionId);
        SessionValidator.validateTherapistId(therapistId);
        SessionValidator.validateClientId(clientId);

        Session existingSession = sessionRepository.getSessionByKeys(sessionId, therapistId);
        SessionValidator.validateSessionExists(existingSession);

        if (!"available".equals(existingSession.getStatus())) {
            throw new IllegalArgumentException("Session is not available for booking");
        }

        Session bookedSession = new Session(
                sessionId,
                therapistId,
                clientId,
                existingSession.getTitle(),
                existingSession.getScheduledAt(),
                existingSession.getDurationMinutes(),
                "booked",
                existingSession.getPrivateNotes(),
                existingSession.getSharedNotes(),
                existingSession.getCreatedAt(),
                Instant.now().toString(),
                existingSession.getLocation()
        );

        return sessionRepository.updateSession(sessionId, therapistId, bookedSession);
    }

    /**
     * Request session appointment using only sessionId and clientId.
     * Resolves therapistId internally by looking up the session first.
     */
    public Session requestSessionAppointmentByClient(String sessionId, String clientId)
            throws IllegalArgumentException {
        SessionValidator.validateSessionId(sessionId);
        SessionValidator.validateClientId(clientId);

        Session existingSession = sessionRepository.getSessionByIdOnly(sessionId);
        SessionValidator.validateSessionExists(existingSession);

        return requestSessionAppointment(sessionId, existingSession.getTherapistId(), clientId);
    }

    /**
     * Search sessions by title
     */
    public List<Session> searchSessionsByTitle(String keyword) throws IllegalArgumentException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("keyword cannot be null or empty");
        }
        return sessionRepository.searchSessionsByTitle(keyword);
    }

    /**
     * Search sessions by sharedNotes
     */
    public List<Session> searchSessionsBySharedNotes(String keyword) throws IllegalArgumentException {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("keyword cannot be null or empty");
        }
        return sessionRepository.searchSessionsBySharedNotes(keyword);
    }

    /**
     * Get sessions by therapist
     */
    public List<Session> getSessionsByTherapist(String therapistId) throws IllegalArgumentException {
        SessionValidator.validateTherapistId(therapistId);
        return sessionRepository.getSessionsByTherapist(therapistId);
    }

    /**
     * Get session by ID
     */
    public Session getSessionById(String sessionId, String therapistId) throws IllegalArgumentException {
        SessionValidator.validateSessionId(sessionId);
        SessionValidator.validateTherapistId(therapistId);

        Session session = sessionRepository.getSessionByKeys(sessionId, therapistId);
        SessionValidator.validateSessionExists(session);

        return session;
    }

    /**
     * Close resources
     */
    public void close() {
        sessionRepository.close();
    }
}

