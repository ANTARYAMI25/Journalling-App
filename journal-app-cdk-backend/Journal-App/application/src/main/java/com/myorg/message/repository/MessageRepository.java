package com.myorg.message.repository;

import com.myorg.message.model.Message;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

public class MessageRepository {
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public MessageRepository(String tableName) {
        this.dynamoDbClient = DynamoDbClient.builder().build();
        this.tableName = tableName;
    }

    /**
     * Send a new message
     */
    public Message sendMessage(Message message) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("messageId", AttributeValue.builder().s(message.getMessageId()).build());
        item.put("conversationId", AttributeValue.builder().s(message.getConversationId()).build());
        item.put("senderId", AttributeValue.builder().s(message.getSenderId()).build());
        item.put("receiverId", AttributeValue.builder().s(message.getReceiverId()).build());
        item.put("content", AttributeValue.builder().s(message.getContent()).build());
        item.put("timestamp", AttributeValue.builder().s(message.getTimestamp()).build());

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(putRequest);
        return message;
    }

    /**
     * Get message by messageId and conversationId
     */
    public Message getMessageByKeys(String messageId, String conversationId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("messageId", AttributeValue.builder().s(messageId).build());
        key.put("conversationId", AttributeValue.builder().s(conversationId).build());

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(getRequest);

        if (response.item().isEmpty()) {
            return null;
        }

        return mapItemToMessage(response.item());
    }

    /**
     * Get message history for a conversation between client and therapist
     * Uses GSI (messagesByConversation)
     */
    public List<Message> getMessageHistory(String clientId, String therapistId) {
        // Create conversation ID (consistent format: clientId#therapistId or therapistId#clientId)
        String conversationId = createConversationId(clientId, therapistId);

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":conversationId", AttributeValue.builder().s(conversationId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("messagesByConversation")
                .keyConditionExpression("conversationId = :conversationId")
                .expressionAttributeValues(expressionAttributeValues)
                .scanIndexForward(true)  // Sort by timestamp ascending
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        List<Message> messages = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            messages.add(mapItemToMessage(item));
        }

        return messages;
    }

    /**
     * Get message history sorted by timestamp (descending - most recent first)
     */
    public List<Message> getMessageHistoryDescending(String clientId, String therapistId) {
        String conversationId = createConversationId(clientId, therapistId);

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":conversationId", AttributeValue.builder().s(conversationId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("messagesByConversation")
                .keyConditionExpression("conversationId = :conversationId")
                .expressionAttributeValues(expressionAttributeValues)
                .scanIndexForward(false)  // Sort by timestamp descending
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        List<Message> messages = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            messages.add(mapItemToMessage(item));
        }

        return messages;
    }

    /**
     * Get messages for a specific conversation
     */
    public List<Message> getMessagesByConversation(String conversationId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":conversationId", AttributeValue.builder().s(conversationId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName("messagesByConversation")
                .keyConditionExpression("conversationId = :conversationId")
                .expressionAttributeValues(expressionAttributeValues)
                .scanIndexForward(true)
                .build();

        QueryResponse response = dynamoDbClient.query(queryRequest);
        List<Message> messages = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            messages.add(mapItemToMessage(item));
        }

        return messages;
    }

    /**
     * Get messages sent by a specific person
     */
    public List<Message> getMessagesBySender(String senderId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":senderId", AttributeValue.builder().s(senderId).build());

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("senderId = :senderId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        ScanResponse response = dynamoDbClient.scan(scanRequest);
        List<Message> messages = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            messages.add(mapItemToMessage(item));
        }

        return messages;
    }

    /**
     * Check if message exists
     */
    public boolean messageExists(String messageId, String conversationId) {
        Message message = getMessageByKeys(messageId, conversationId);
        return message != null;
    }

    /**
     * Create a consistent conversation ID
     */
    private String createConversationId(String clientId, String therapistId) {
        // Sort IDs to ensure consistency regardless of who initiated
        List<String> ids = Arrays.asList(clientId, therapistId);
        Collections.sort(ids);
        return ids.get(0) + "#" + ids.get(1);
    }


    private Message mapItemToMessage(Map<String, AttributeValue> item) {
        Message message = new Message();

        if (item.containsKey("messageId")) {
            message.setMessageId(item.get("messageId").s());
        }
        if (item.containsKey("conversationId")) {
            message.setConversationId(item.get("conversationId").s());
        }
        if (item.containsKey("senderId")) {
            message.setSenderId(item.get("senderId").s());
        }
        if (item.containsKey("receiverId")) {
            message.setReceiverId(item.get("receiverId").s());
        }
        if (item.containsKey("content")) {
            message.setContent(item.get("content").s());
        }
        if (item.containsKey("timestamp")) {
            message.setTimestamp(item.get("timestamp").s());
        }

        return message;
    }

    /**
     * Close the DynamoDB client
     */
    public void close() {
        dynamoDbClient.close();
    }
}

