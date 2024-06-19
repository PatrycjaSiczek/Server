package org.example;

import java.awt.*;

public class Message {
    public MessageType type;
    public String content;
    public String recipient;

    public Message() {}

    public Message(MessageType type, String content) {
        this.type = type;
        this.content = content;
    }


    public Message(MessageType type, String content,  String recipient) {
        this.type = type;
        this.content = content;
        this.recipient = recipient;
    }
}
