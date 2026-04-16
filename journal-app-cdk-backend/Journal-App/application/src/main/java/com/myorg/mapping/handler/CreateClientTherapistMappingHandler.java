package com.myorg.mapping.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.mapping.model.Mapping;
import com.myorg.mapping.service.MappingService;

import java.util.*;

public class CreateClientTherapistMappingHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final MappingService mappingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CreateClientTherapistMappingHandler() {
        String tableName = System.getenv("MAPPING_TABLE_NAME");
        if (tableName == null || tableName.isEmpty()) {
            tableName = "MappingTable";
        }
        this.mappingService = new MappingService(tableName);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {

            Map<String, Object> pathParameters = (Map<String, Object>) input.get("pathParameters");
            if (pathParameters == null || !pathParameters.containsKey("clientId") || !pathParameters.containsKey("therapistId")) {
                return buildErrorResponse(400, "Missing clientId or therapistId in path parameters");
            }

            String clientId = (String) pathParameters.get("clientId");
            String therapistId = (String) pathParameters.get("therapistId");


            Mapping mapping = mappingService.createMapping(clientId, therapistId);


            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 201);
            response.put("body", objectMapper.writeValueAsString(mapping));
            response.put("headers", Map.of("Content-Type", "application/json"));

            return response;

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already exists")) {
                return buildErrorResponse(409, e.getMessage());
            }
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

