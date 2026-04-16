package com.myorg.session.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.session.model.Session;
import com.myorg.session.service.SessionService;

import java.util.*;

public class GetAllAvailableSessionsHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final SessionService sessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetAllAvailableSessionsHandler() {
        String tableName = System.getenv("SESSION_TABLE_NAME");
        if (tableName == null || tableName.isEmpty()) {
            tableName = "SessionsTable";
        }
        this.sessionService = new SessionService(tableName);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {

            String location = null;

            if (input.containsKey("queryStringParameters") && input.get("queryStringParameters") != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> queryParams = (Map<String, String>) input.get("queryStringParameters");
                location = queryParams.get("location");
            }

            // location filter for session
            List<Session> sessions;
            if (location != null && !location.isEmpty()) {
                sessions = sessionService.getAvailableSessionsWithFilters(location);
            } else {
                sessions = sessionService.getAllAvailableSessions();
            }


            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            Map<String, Object> body = new HashMap<>();
            body.put("sessions", sessions);
            response.put("body", objectMapper.writeValueAsString(body));
            response.put("headers", Map.of("Content-Type", "application/json"));

            return response;

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return buildErrorResponse(500, "Internal server error");
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

