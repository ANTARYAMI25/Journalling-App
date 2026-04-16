package com.myorg.mapping.repository;

import com.myorg.mapping.model.Mapping;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;

public class MappingRepository {
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public MappingRepository(String tableName) {
        this.dynamoDbClient = DynamoDbClient.builder().build();
        this.tableName = tableName;
    }

    /**
     * Create a new mapping
     */
    public Mapping createMapping(Mapping mapping) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("mappingId", AttributeValue.builder().s(mapping.getMappingId()).build());
        item.put("clientId", AttributeValue.builder().s(mapping.getClientId()).build());
        item.put("therapistId", AttributeValue.builder().s(mapping.getTherapistId()).build());
        item.put("status", AttributeValue.builder().s(mapping.getStatus()).build());
        item.put("createdAt", AttributeValue.builder().s(mapping.getCreatedAt()).build());
        if (mapping.getUpdatedAt() != null) {
            item.put("updatedAt", AttributeValue.builder().s(mapping.getUpdatedAt()).build());
        }

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(putRequest);
        return mapping;
    }

    /**
     * Get mapping by clientId and therapistId
     */
    public Mapping getMappingByKeys(String clientId, String therapistId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("clientId", AttributeValue.builder().s(clientId).build());
        key.put("therapistId", AttributeValue.builder().s(therapistId).build());

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(getRequest);

        if (response.item().isEmpty()) {
            return null;
        }

        return mapItemToMapping(response.item());
    }

    /**
     * Get all therapists for a client (Query using GSI for clientId as SK)
     */
    public List<Mapping> getTherapistsForClient(String clientId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":clientId", AttributeValue.builder().s(clientId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("clientId = :clientId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        List<Mapping> mappings = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            mappings.add(mapItemToMapping(item));
        }

        return mappings;
    }

    /**
     * Get all clients for a therapist using GSI (TherapistMappingIndex)
     */
    public List<Mapping> getClientsForTherapist(String therapistId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":therapistId", AttributeValue.builder().s(therapistId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("TherapistMappingIndex")
                .keyConditionExpression("therapistId = :therapistId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        List<Mapping> mappings = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            mappings.add(mapItemToMapping(item));
        }

        return mappings;
    }

    /**
     * Update mapping status
     */
    public Mapping updateMappingStatus(String clientId, String therapistId, String status) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("clientId", AttributeValue.builder().s(clientId).build());
        key.put("therapistId", AttributeValue.builder().s(therapistId).build());

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":status", AttributeValue.builder().s(status).build());
        expressionAttributeValues.put(":updatedAt", AttributeValue.builder().s(Instant.now().toString()).build());

        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression("SET #status = :status, updatedAt = :updatedAt")
                .expressionAttributeNames(Collections.singletonMap("#status", "status"))
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        UpdateItemResponse response = dynamoDbClient.updateItem(updateRequest);
        return mapItemToMapping(response.attributes());
    }

    /**
     * Delete mapping
     */
    public void deleteMapping(String clientId, String therapistId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("clientId", AttributeValue.builder().s(clientId).build());
        key.put("therapistId", AttributeValue.builder().s(therapistId).build());

        DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(deleteRequest);
    }

    /**
     * Check if mapping exists
     */
    public boolean mappingExists(String clientId, String therapistId) {
        Mapping mapping = getMappingByKeys(clientId, therapistId);
        return mapping != null;
    }

    /**
     * Helper method to map DynamoDB item to Mapping object
     */
    private Mapping mapItemToMapping(Map<String, AttributeValue> item) {
        Mapping mapping = new Mapping();

        if (item.containsKey("mappingId")) {
            mapping.setMappingId(item.get("mappingId").s());
        }
        if (item.containsKey("clientId")) {
            mapping.setClientId(item.get("clientId").s());
        }
        if (item.containsKey("therapistId")) {
            mapping.setTherapistId(item.get("therapistId").s());
        }
        if (item.containsKey("status")) {
            mapping.setStatus(item.get("status").s());
        }
        if (item.containsKey("createdAt")) {
            mapping.setCreatedAt(item.get("createdAt").s());
        }
        if (item.containsKey("updatedAt")) {
            mapping.setUpdatedAt(item.get("updatedAt").s());
        }

        return mapping;
    }

    /**
     * Close the DynamoDB client
     */
    public void close() {
        dynamoDbClient.close();
    }
}

