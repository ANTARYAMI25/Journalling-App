package com.myorg.mapping.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.mapping.model.Mapping;
import com.myorg.mapping.service.MappingService;

import java.util.*;

public class GetTherapistClientsHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final MappingService mappingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GetTherapistClientsHandler() {
        String tableName = System.getenv("MAPPING_TABLE_NAME");
        if (tableName == null || tableName.isEmpty()) {
            tableName = "MappingTable";
        }
        this.mappingService = new MappingService(tableName);
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        try {
            Map<String, Object> response = new HashMap<>();


            Map<String, Object> pathParameters = (Map<String, Object>) input.get("pathParameters");
            if (pathParameters == null || !pathParameters.containsKey("therapistId")) {
                return buildErrorResponse(400, "Missing therapistId in path parameters");
            }

            String therapistId = (String) pathParameters.get("therapistId");


            List<Mapping> mappings = mappingService.getClientsForTherapist(therapistId);


            response.put("statusCode", 200);
            Map<String, Object> body = new HashMap<>();
            body.put("mappings", mappings);
            response.put("body", objectMapper.writeValueAsString(body));
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

