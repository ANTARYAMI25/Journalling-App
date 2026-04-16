package com.myorg.message.service;

import com.myorg.message.model.Message;
import com.myorg.message.repository.MessageRepository;
import com.myorg.message.validation.MessageValidator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(String tableName) {
        this.messageRepository = new MessageRepository(tableName);
    }

    /**
     * Send a new message
     */
    public Message sendMessage(String senderId, String receiverId, String content) 
            throws IllegalArgumentException {
        MessageValidator.validateMessageInput(senderId, receiverId, content);

        // Create conversation ID (consistent format)
        String conversationId = createConversationId(senderId, receiverId);
        String messageId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();

        Message message = new Message(
                messageId,
                conversationId,
                senderId,
                receiverId,
                content,
                timestamp
        );

        return messageRepository.sendMessage(message);
    }

    /**
     * Get message history between client and therapist
     */
    public List<Message> getMessageHistory(String clientId, String therapistId) 
            throws IllegalArgumentException {
        MessageValidator.validateSenderId(clientId);
        MessageValidator.validateReceiverId(therapistId);

        return messageRepository.getMessageHistory(clientId, therapistId);
    }

    /**
     * Get message history (most recent first)
     */
    public List<Message> getMessageHistoryDescending(String clientId, String therapistId) 
            throws IllegalArgumentException {
        MessageValidator.validateSenderId(clientId);
        MessageValidator.validateReceiverId(therapistId);

        return messageRepository.getMessageHistoryDescending(clientId, therapistId);
    }

    /**
     * Get messages for a specific conversation
     */
    public List<Message> getMessagesByConversation(String conversationId) 
            throws IllegalArgumentException {
        MessageValidator.validateConversationId(conversationId);

        return messageRepository.getMessagesByConversation(conversationId);
    }

    /**
     * Get messages sent by a specific person
     */
    public List<Message> getMessagesBySender(String senderId) 
            throws IllegalArgumentException {
        MessageValidator.validateSenderId(senderId);

        return messageRepository.getMessagesBySender(senderId);
    }

    /**
     * Get a specific message
     */
    public Message getMessage(String messageId, String conversationId) 
            throws IllegalArgumentException {
        MessageValidator.validateMessageId(messageId);
        MessageValidator.validateConversationId(conversationId);

        Message message = messageRepository.getMessageByKeys(messageId, conversationId);
        MessageValidator.validateMessageExists(message);

        return message;
    }

    /**
     * Create a consistent conversation ID
     */
    private String createConversationId(String clientId, String therapistId) {
        // Sort IDs to ensure consistency regardless of who initiated
        if (clientId.compareTo(therapistId) < 0) {
            return clientId + "#" + therapistId;
        } else {
            return therapistId + "#" + clientId;
        }
    }

    /**
     * Close resources
     */
    public void close() {
        messageRepository.close();
    }
}

