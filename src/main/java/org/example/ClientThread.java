package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;

import static org.example.MessageType.Broadcast;
import static org.example.MessageType.Logout;
import static org.example.MessageType.Private;


public class ClientThread extends Thread {
    Socket client;
    Server server;
    PrintWriter writer;
    String clientName;

    public ClientThread(Socket client, Server server) {
        this.client = client;
        this.server = server;
    }

    public String getClientName() {
        return clientName;
    }

    public void run() {
        try {
            InputStream input = client.getInputStream();
            OutputStream output = client.getOutputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input)
            );

            writer = new PrintWriter(output, true);

            String rawMessage;

            while ((rawMessage = reader.readLine()) != null) {
                Message message = new ObjectMapper()
                        .readValue(rawMessage, Message.class);

                switch (message.type) {
                    case Broadcast -> {
                        if (message.content.equals(("/online"))) {
                            onlineUsers();
                        } else {
                            message.content = clientName + ": " + message.content;
                            server.broadcast(message);
                        }
                    }
                    case Login -> {
                        login(message.content);
                        server.broadcast(new Message(MessageType.Login, message.content + " - Joined"));
                        System.out.println(message.content + " - Joined ");
                    }
                    case Logout -> {
                        server.broadcast(new Message(Logout, message.content + " - Left"));
                        System.out.println(message.content + " - Left.");
                        server.removeClient(this);
                        client.close();
                        return;
                    }
                    case Private -> {privatemessage(message);}
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Message message) throws JsonProcessingException {
        String rawMessage = new ObjectMapper()
                .writeValueAsString(message);
        writer.println(rawMessage);
    }

    public void login(String name) throws JsonProcessingException {
        clientName = name;
        Message message = new Message(Broadcast, "Welcome, " + name);
        send(message);
    }

    private void onlineUsers() throws JsonProcessingException {
        StringBuilder usersList = new StringBuilder("Online users: ");
        for (ClientThread client : server.getClients()) {
            usersList.append(client.getClientName()).append(", ");
        }
        send(new Message(MessageType.Broadcast, usersList.toString()));
    }

    private void privatemessage(Message message) throws JsonProcessingException {
      ClientThread recipient = server.getClientName(message.recipient);
      if (recipient != null) {
          recipient.send(new Message(MessageType.Private, message.content, clientName));
      } else {
          send(new Message(MessageType.Private, "User " + message.recipient + "not found", "Serwer"));
      }
    }
    }