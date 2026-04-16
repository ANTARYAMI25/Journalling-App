package com.myorg.session.repository;

import com.myorg.session.model.Session;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;

public class SessionRepository {
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public SessionRepository(String tableName) {
        this.dynamoDbClient = DynamoDbClient.builder().build();
        this.tableName = tableName;
    }

    /**
     * Create a new session
     */
    public Session createSession(Session session) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("sessionId", AttributeValue.builder().s(session.getSessionId()).build());
        item.put("therapistId", AttributeValue.builder().s(session.getTherapistId()).build());
        item.put("title", AttributeValue.builder().s(session.getTitle()).build());
        item.put("scheduledAt", AttributeValue.builder().s(session.getScheduledAt()).build());
        item.put("durationMinutes", AttributeValue.builder().n(session.getDurationMinutes().toString()).build());
        item.put("status", AttributeValue.builder().s(session.getStatus()).build());
        item.put("createdAt", AttributeValue.builder().s(session.getCreatedAt()).build());
        if (session.getUpdatedAt() != null) {
            item.put("updatedAt", AttributeValue.builder().s(session.getUpdatedAt()).build());
        }

        if (session.getClientId() != null) {
            item.put("clientId", AttributeValue.builder().s(session.getClientId()).build());
        }
        if (session.getPrivateNotes() != null) {
            item.put("privateNotes", AttributeValue.builder().s(session.getPrivateNotes()).build());
        }
        if (session.getSharedNotes() != null) {
            item.put("sharedNotes", AttributeValue.builder().s(session.getSharedNotes()).build());
        }
        if (session.getLocation() != null) {
            item.put("location", AttributeValue.builder().s(session.getLocation()).build());
        }

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(putRequest);
        return session;
    }

    /**
     * Get session by sessionId and therapistId
     */
    public Session getSessionByKeys(String sessionId, String therapistId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("sessionId", AttributeValue.builder().s(sessionId).build());
        key.put("therapistId", AttributeValue.builder().s(therapistId).build());

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(getRequest);

        if (response.item().isEmpty()) {
            return null;
        }

        return mapItemToSession(response.item());
    }

    /**
     * Get all sessions regardless of status
     */
    public List<Session> getAllSessions() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        List<Session> sessions = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            sessions.add(mapItemToSession(item));
        }

        return sessions;
    }

    /**
     * Get all available sessions (status = available)
     */
    public List<Session> getAllAvailableSessions() {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":status", AttributeValue.builder().s("available").build());

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("#status = :status")
                .expressionAttributeNames(Collections.singletonMap("#status", "status"))
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        List<Session> sessions = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            sessions.add(mapItemToSession(item));
        }

        return sessions;
    }

    /**
     * Get available sessions with optional location filter
     */
    public List<Session> getAvailableSessionsWithFilters(String location) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        Map<String, String> expressionAttributeNames = new HashMap<>();

        // Always filter by status = available
        expressionAttributeValues.put(":status", AttributeValue.builder().s("available").build());
        expressionAttributeNames.put("#status", "status");
        StringBuilder filterExpression = new StringBuilder("#status = :status");

        // Add location filter
        expressionAttributeValues.put(":location", AttributeValue.builder().s(location).build());
        expressionAttributeNames.put("#location", "location");
        filterExpression.append(" AND contains(#location, :location)");

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression(filterExpression.toString())
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        List<Session> sessions = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            sessions.add(mapItemToSession(item));
        }

        return sessions;
    }

    /**
     * Get sessions by therapistId using GSI (TherapistTimeIndex)
     */
    public List<Session> getSessionsByTherapist(String therapistId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":therapistId", AttributeValue.builder().s(therapistId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("TherapistTimeIndex")
                .keyConditionExpression("therapistId = :therapistId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        List<Session> sessions = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            sessions.add(mapItemToSession(item));
        }

        return sessions;
    }

    /**
     * Get sessions by therapistId using a strongly consistent scan on the base table.
     * Used for overlap detection during session creation to avoid GSI eventual consistency issues.
     */
    public List<Session> getSessionsByTherapistConsistent(String therapistId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":therapistId", AttributeValue.builder().s(therapistId).build());

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("therapistId = :therapistId")
                .expressionAttributeValues(expressionAttributeValues)
                .consistentRead(true)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        List<Session> sessions = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            sessions.add(mapItemToSession(item));
        }

        return sessions;
    }

    /**
     * Get sessions by clientId using LSI (ClientIdIndex)
     */
    public List<Session> getSessionsByClient(String clientId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":clientId", AttributeValue.builder().s(clientId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("ClientIdIndex")
                .keyConditionExpression("clientId = :clientId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        List<Session> sessions = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            sessions.add(mapItemToSession(item));
        }

        return sessions;
    }

    /**
     * Update session
     */
    public Session updateSession(String sessionId, String therapistId, Session session) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("sessionId", AttributeValue.builder().s(sessionId).build());
        key.put("therapistId", AttributeValue.builder().s(therapistId).build());

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":title", AttributeValue.builder().s(session.getTitle()).build());
        expressionAttributeValues.put(":scheduledAt", AttributeValue.builder().s(session.getScheduledAt()).build());
        expressionAttributeValues.put(":durationMinutes", AttributeValue.builder().n(session.getDurationMinutes().toString()).build());
        expressionAttributeValues.put(":status", AttributeValue.builder().s(session.getStatus()).build());
        expressionAttributeValues.put(":updatedAt", AttributeValue.builder().s(Instant.now().toString()).build());

        StringBuilder updateExpression = new StringBuilder("SET title = :title, scheduledAt = :scheduledAt, durationMinutes = :durationMinutes, #status = :status, updatedAt = :updatedAt");

        if (session.getClientId() != null) {
            expressionAttributeValues.put(":clientId", AttributeValue.builder().s(session.getClientId()).build());
            updateExpression.append(", clientId = :clientId");
        }
        if (session.getPrivateNotes() != null) {
            expressionAttributeValues.put(":privateNotes", AttributeValue.builder().s(session.getPrivateNotes()).build());
            updateExpression.append(", privateNotes = :privateNotes");
        }
        if (session.getSharedNotes() != null) {
            expressionAttributeValues.put(":sharedNotes", AttributeValue.builder().s(session.getSharedNotes()).build());
            updateExpression.append(", sharedNotes = :sharedNotes");
        }
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#status", "status");

        if (session.getLocation() != null) {
            expressionAttributeValues.put(":location", AttributeValue.builder().s(session.getLocation()).build());
            expressionAttributeNames.put("#location", "location");
            updateExpression.append(", #location = :location");
        }

        UpdateItemRequest updateRequest = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression(updateExpression.toString())
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        UpdateItemResponse response = dynamoDbClient.updateItem(updateRequest);
        return mapItemToSession(response.attributes());
    }

    /**
     * Search sessions by title.
     */
    public List<Session> searchSessionsByTitle(String keyword) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":keyword", AttributeValue.builder().s(keyword).build());

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("contains(#title, :keyword)")
                .expressionAttributeNames(Collections.singletonMap("#title", "title"))
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);

        // Post-filter in Java for true case-insensitive matching
        String lowerKeyword = keyword.toLowerCase();
        List<Session> result = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            Session session = mapItemToSession(item);
            if (session.getTitle() != null && session.getTitle().toLowerCase().contains(lowerKeyword)) {
                result.add(session);
            }
        }

        return result;
    }

    /**
     * Search sessions by sharedNotes.
     */
    public List<Session> searchSessionsBySharedNotes(String keyword) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":keyword", AttributeValue.builder().s(keyword).build());

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("contains(#sharedNotes, :keyword)")
                .expressionAttributeNames(Collections.singletonMap("#sharedNotes", "sharedNotes"))
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);

        // Post-filter in Java for true case-insensitive matching
        String lowerKeyword = keyword.toLowerCase();
        List<Session> result = new ArrayList<>();
        for (Map<String, AttributeValue> item : response.items()) {
            Session session = mapItemToSession(item);
            if (session.getSharedNotes() != null && session.getSharedNotes().toLowerCase().contains(lowerKeyword)) {
                result.add(session);
            }
        }

        return result;
    }

    /**
     * Get a single session by sessionId only
     */
    public Session getSessionByIdOnly(String sessionId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":sessionId", AttributeValue.builder().s(sessionId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("sessionId = :sessionId")
                .expressionAttributeValues(expressionAttributeValues)
                .limit(1)
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);

        if (response.items().isEmpty()) {
            return null;
        }

        return mapItemToSession(response.items().get(0));
    }

    /**
     * Check if session exists
     */
    public boolean sessionExists(String sessionId, String therapistId) {
        Session session = getSessionByKeys(sessionId, therapistId);
        return session != null;
    }

    /**
     * Helper method to map DynamoDB item to Session object
     */
    private Session mapItemToSession(Map<String, AttributeValue> item) {
        Session session = new Session();

        if (item.containsKey("sessionId")) {
            session.setSessionId(item.get("sessionId").s());
        }
        if (item.containsKey("therapistId")) {
            session.setTherapistId(item.get("therapistId").s());
        }
        if (item.containsKey("clientId")) {
            session.setClientId(item.get("clientId").s());
        }
        if (item.containsKey("title")) {
            session.setTitle(item.get("title").s());
        }
        if (item.containsKey("scheduledAt")) {
            session.setScheduledAt(item.get("scheduledAt").s());
        }
        if (item.containsKey("durationMinutes")) {
            session.setDurationMinutes(Integer.parseInt(item.get("durationMinutes").n()));
        }
        if (item.containsKey("status")) {
            session.setStatus(item.get("status").s());
        }
        if (item.containsKey("privateNotes")) {
            session.setPrivateNotes(item.get("privateNotes").s());
        }
        if (item.containsKey("sharedNotes")) {
            session.setSharedNotes(item.get("sharedNotes").s());
        }
        if (item.containsKey("createdAt")) {
            session.setCreatedAt(item.get("createdAt").s());
        }
        if (item.containsKey("updatedAt")) {
            session.setUpdatedAt(item.get("updatedAt").s());
        }
        if (item.containsKey("location")) {
            session.setLocation(item.get("location").s());
        }

        return session;
    }

    /**
     * Close the DynamoDB client
     */
    public void close() {
        dynamoDbClient.close();
    }
}

