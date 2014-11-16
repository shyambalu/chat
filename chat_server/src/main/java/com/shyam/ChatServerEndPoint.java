package com.shyam;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value="/", encoders = ChatMessageEncoder.class, decoders = ChatMessageDecoder.class)
public class ChatServerEndPoint
{

    @OnOpen
    public void onOpen(Session sess)
    {

        if (!ChatServer.getChatRoom().addUser(sess)) {
            CloseReason err = new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Username is invalid");
            try {
                sess.getBasicRemote().sendText("no-auth");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                sess.getBasicRemote().sendText("auth");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnMessage
    public void onMessage(final ChatMessage message)
    {
        ChatServer.getChatRoom().broadcast(message);
    }

    @OnClose
    public void onClose(Session sess, CloseReason reason)
    {
        ChatServer.getChatRoom().removeUser(sess);
        System.out.printf("Socket Closed: %s%n", reason);
    }

    @OnError
    public void onError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }
}
