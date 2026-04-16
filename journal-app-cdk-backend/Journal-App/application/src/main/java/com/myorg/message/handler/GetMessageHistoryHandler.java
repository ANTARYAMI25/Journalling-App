package com.myorg.message.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.message.model.Message;
import com.myorg.message.service.MessageService;

import java.util.*;

public class GetMessageHistoryHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetMessageHistoryHandler() {
        String tableName = System.getenv("MESSAGE_TABLE_NAME");
        if (tableName == null || tableName.isEmpty()) {
            tableName = "MessagesTable";
        }
        this.messageService = new MessageService(tableName);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {

            String clientId = null;
            String therapistId = null;

            if (input.containsKey("queryStringParameters") && input.get("queryStringParameters") != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> queryParams = (Map<String, String>) input.get("queryStringParameters");
                clientId = queryParams.get("clientId");
                therapistId = queryParams.get("therapistId");
            }


            if (clientId == null || clientId.isEmpty()) {
                return buildErrorResponse(400, "clientId is required");
            }
            if (therapistId == null || therapistId.isEmpty()) {
                return buildErrorResponse(400, "therapistId is required");
            }


            List<Message> messages = messageService.getMessageHistory(clientId, therapistId);


            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            response.put("body", objectMapper.writeValueAsString(messages));
            response.put("headers", Map.of("Content-Type", "application/json"));

            return response;

        } catch (IllegalArgumentException e) {
            context.getLogger().log("Validation error: " + e.getMessage());
            return buildErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            e.printStackTrace();
            return buildErrorResponse(500, "Internal server error");
        }
    }

    private Map<String, Object> buildErrorResponse(int statusCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("error", message);
            response.put("body", objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            response.put("body", "{\"error\":\"" + message + "\"}");
        }
        response.put("headers", Map.of("Content-Type", "application/json"));
        return response;
    }
}

