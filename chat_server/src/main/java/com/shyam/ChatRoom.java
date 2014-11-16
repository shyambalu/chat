package com.shyam;

import javax.websocket.Session;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ChatRoom {

    private final Logger log = Logger.getLogger(getClass().getName());

    private static final Set<String> users = new HashSet<>();
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    public void broadcast(final ChatMessage message) {

        log.info("msg received " + message);

        for (Session s : sessions) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendObject(message);
                } catch (Exception ex) {
                    System.out.printf("Websocket Error %s", ex.getMessage());
                }
            }
            else {
                sessions.remove(s);
            }
        }

    }

    public boolean addUser(Session sess) {
        String userName = sess.getRequestParameterMap().get("username").get(0);

        log.info(users.toString());
        log.info("user attempting to connect: " + userName);
        log.info("user exists " + users.contains(userName));

        return !users.contains(userName) && users.add(userName) && sessions.add(sess);
    }

    public void removeUser(Session sess) {
        users.remove(sess.getRequestParameterMap().get("username").get(0));
        sessions.remove(sess);
    }
}
