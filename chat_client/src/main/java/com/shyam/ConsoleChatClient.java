package com.shyam;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.Console;
import java.io.StringReader;
import java.net.URI;

public class ConsoleChatClient
{

    public static void main(String[] args)
    {
        Console console = System.console();
        final String userName = console.readLine("Please enter your user name: ");

        URI uri = URI.create("ws://localhost:8080/?username=" + userName);
        final ChatClientEndPoint clientEndPoint = new ChatClientEndPoint(uri);

        clientEndPoint.addMessageHandler(message -> {
            JsonObject messageJ = Json.createReader(new StringReader(message)).readObject();
            System.out.println(messageJ);
        });

        //noinspection InfiniteLoopStatement
        while (true) {
            String message = console.readLine();
            clientEndPoint.sendMessage(getMessage(userName, message));
        }
    }

    private static String getMessage(final String user, final String message) {
        return Json.createObjectBuilder().add("sender", user).add("message", message).build().toString();
    }
}