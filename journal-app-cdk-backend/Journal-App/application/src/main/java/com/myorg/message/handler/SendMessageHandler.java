package com.myorg.message.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.message.model.Message;
import com.myorg.message.service.MessageService;

import java.util.*;

public class SendMessageHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SendMessageHandler() {
        String tableName = System.getenv("MESSAGE_TABLE_NAME");
        if (tableName == null || tableName.isEmpty()) {
            tableName = "MessagesTable";
        }
        this.messageService = new MessageService(tableName);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {

            String body = (String) input.get("body");
            if (body == null || body.isEmpty()) {
                return buildErrorResponse(400, "Request body is required");
            }

            @SuppressWarnings("unchecked")
            Map<String, String> messageInput = objectMapper.readValue(body, Map.class);

            String senderId = messageInput.get("senderId");
            String receiverId = messageInput.get("receiverId");
            String content = messageInput.get("content");


            if (senderId == null || senderId.isEmpty()) {
                return buildErrorResponse(400, "senderId is required");
            }
            if (receiverId == null || receiverId.isEmpty()) {
                return buildErrorResponse(400, "receiverId is required");
            }
            if (content == null || content.isEmpty()) {
                return buildErrorResponse(400, "content is required");
            }


            Message message = messageService.sendMessage(senderId, receiverId, content);


            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 201);
            response.put("body", objectMapper.writeValueAsString(message));
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

