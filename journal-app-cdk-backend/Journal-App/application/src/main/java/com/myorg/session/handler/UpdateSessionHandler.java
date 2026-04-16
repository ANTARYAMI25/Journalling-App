package com.myorg.session.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.session.model.Session;
import com.myorg.session.service.SessionService;

import java.util.*;

public class UpdateSessionHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final SessionService sessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UpdateSessionHandler() {
        String tableName = System.getenv("SESSION_TABLE_NAME");
        if (tableName == null || tableName.isEmpty()) {
            tableName = "SessionsTable";
        }
        this.sessionService = new SessionService(tableName);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {

            Map<String, Object> pathParameters = (Map<String, Object>) input.get("pathParameters");
            if (pathParameters == null || !pathParameters.containsKey("sessionId") || !pathParameters.containsKey("therapistId")) {
                return buildErrorResponse(400, "Missing sessionId or therapistId in path parameters");
            }

            String sessionId = (String) pathParameters.get("sessionId");
            String therapistId = (String) pathParameters.get("therapistId");


            String body = (String) input.get("body");
            if (body == null || body.isEmpty()) {
                return buildErrorResponse(400, "Request body cannot be empty");
            }

            Map<String, Object> requestBody = objectMapper.readValue(body, Map.class);

            String title = (String) requestBody.get("title");
            String scheduledAt = (String) requestBody.get("scheduledAt");
            Object rawDuration = requestBody.get("durationMinutes");
            if (rawDuration == null) {
                return buildErrorResponse(400, "durationMinutes is required");
            }
            Integer durationMinutes = ((Number) rawDuration).intValue();
            String status = (String) requestBody.get("status");
            String privateNotes = (String) requestBody.get("privateNotes");
            String sharedNotes = (String) requestBody.get("sharedNotes");
            String location = (String) requestBody.get("location");


            Session session = sessionService.updateSession(sessionId, therapistId, title, scheduledAt,
                    durationMinutes, status, privateNotes, sharedNotes, location);


            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            response.put("body", objectMapper.writeValueAsString(session));
            response.put("headers", Map.of("Content-Type", "application/json"));

            return response;

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("does not exist")) {
                return buildErrorResponse(404, e.getMessage());
            }
            return buildErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            context.getLogger().log("Error [" + e.getClass().getName() + "]: " + e.getMessage());
            return buildErrorResponse(500, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private Map<String, Object> buildErrorResponse(int statusCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("error", message);
            response.put("body", new ObjectMapper().writeValueAsString(body));
        } catch (Exception e) {
            response.put("body", "{\"error\":\"" + message + "\"}");
        }
        response.put("headers", Map.of("Content-Type", "application/json"));
        return response;
    }
}

