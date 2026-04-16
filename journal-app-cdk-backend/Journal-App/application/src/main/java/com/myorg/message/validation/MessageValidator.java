package com.myorg.message.validation;

public class MessageValidator {

    /**
     * Validate sender ID
     */
    public static void validateSenderId(String senderId) {
        if (senderId == null || senderId.trim().isEmpty()) {
            throw new IllegalArgumentException("senderId cannot be null or empty");
        }
    }

    /**
     * Validate receiver ID
     */
    public static void validateReceiverId(String receiverId) {
        if (receiverId == null || receiverId.trim().isEmpty()) {
            throw new IllegalArgumentException("receiverId cannot be null or empty");
        }
    }

    /**
     * Validate message content
     */
    public static void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("content cannot be null or empty");
        }
        if (content.length() > 5000) {
            throw new IllegalArgumentException("content cannot exceed 5000 characters");
        }
    }

    /**
     * Validate message input
     */
    public static void validateMessageInput(String senderId, String receiverId, String content) {
        validateSenderId(senderId);
        validateReceiverId(receiverId);
        validateContent(content);

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("senderId and receiverId cannot be the same");
        }
    }

    /**
     * Validate message exists
     */
    public static void validateMessageExists(Object message) {
        if (message == null) {
            throw new IllegalArgumentException("Message not found");
        }
    }

    /**
     * Validate conversation ID
     */
    public static void validateConversationId(String conversationId) {
        if (conversationId == null || conversationId.trim().isEmpty()) {
            throw new IllegalArgumentException("conversationId cannot be null or empty");
        }
    }

    /**
     * Validate message ID
     */
    public static void validateMessageId(String messageId) {
        if (messageId == null || messageId.trim().isEmpty()) {
            throw new IllegalArgumentException("messageId cannot be null or empty");
        }
    }
}

