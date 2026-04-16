package com.myorg.message.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("conversationId")
    private String conversationId;

    @JsonProperty("senderId")
    private String senderId;

    @JsonProperty("receiverId")
    private String receiverId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestamp")
    private String timestamp;

    // Constructors
    public Message() {
    }

    public Message(String messageId, String conversationId, String senderId, 
                   String receiverId, String content, String timestamp) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}

