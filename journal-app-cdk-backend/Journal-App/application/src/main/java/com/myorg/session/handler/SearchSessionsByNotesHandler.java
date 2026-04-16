package com.myorg.session.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.session.model.Session;
import com.myorg.session.service.SessionService;

import java.util.*;

public class SearchSessionsByNotesHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final SessionService sessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchSessionsByNotesHandler() {
        String tableName = System.getenv("SESSION_TABLE_NAME");
        if (tableName == null || tableName.isEmpty()) {
            tableName = "SessionsTable";
        }
        this.sessionService = new SessionService(tableName);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {

            Map<String, String> queryParameters = (Map<String, String>) input.get("queryStringParameters");
            String keyword = (queryParameters != null) ? queryParameters.get("keyword") : null;
            String title = (queryParameters != null) ? queryParameters.get("title") : null;


            List<Session> sessions;
            if (keyword != null && !keyword.isEmpty()) {
                sessions = sessionService.searchSessionsBySharedNotes(keyword);
            } else if (title != null && !title.isEmpty()) {
                sessions = sessionService.searchSessionsByTitle(title);
            } else {
                sessions = sessionService.getAllSessions();
            }


            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            response.put("body", objectMapper.writeValueAsString(sessions));
            response.put("headers", Map.of("Content-Type", "application/json"));

            return response;

        } catch (IllegalArgumentException e) {
            return buildErrorResponse(400, e.getMessage());
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

