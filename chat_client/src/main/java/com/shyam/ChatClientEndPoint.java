package com.shyam;

import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@ClientEndpoint
public class ChatClientEndPoint {
    public Session userSession = null;
    private MessageHandler messageHandler;

    private final CountDownLatch authLatch;
    public boolean authenticated;

    public ChatClientEndPoint(URI endpointURI) {
        this.authLatch = new CountDownLatch(1);

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println(message);

        switch (message) {
            case "auth":
                this.authenticated = true;
                this.authLatch.countDown();
                break;
            case "no-auth":
                this.authenticated = false;
                this.authLatch.countDown();
                break;
            default:
                if (this.messageHandler != null)
                    this.messageHandler.handleMessage(message);
                break;
        }
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    public static interface MessageHandler {
        public void handleMessage(String message);
    }

    public boolean awaitAuth(int duration, TimeUnit unit) throws InterruptedException {
        return this.authLatch.await(duration, unit);
    }
}