package com.shyam.gui;

import com.shyam.ChatClientEndPoint;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class ChatController implements Initializable {

    private final ChatModel model = new ChatModel();

    @FXML
    private Button login_btn;

    @FXML
    private Button send_btn;

    @FXML
    private TextField username_txt;

    @FXML
    private TextField message_txt;

    @FXML
    private ListView<String> chat_view;

    private ChatClientEndPoint clientEndPoint;

    private static String getJsonMessage(final String user, final String message) {
        return Json.createObjectBuilder().add("sender", user).add("message", message).build().toString();
    }

    private static String getStringMessage(final String response) {
        JsonObject root = Json.createReader(new StringReader(response)).readObject();
        String message = root.getString("message");
        String sender = root.getString("sender");
        String received = root.getString("received");
        return String.format("%s: %s [%s]", sender, message, received);
    }

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {


        model.userName.bindBidirectional(username_txt .textProperty());
        model.readyToChat.bind(model.userName.isNotEmpty());

        send_btn.disableProperty().bind(model.connected.not());
        message_txt.disableProperty().bind(model.connected.not());
        message_txt.textProperty().bindBidirectional(model.currentMessage);

        login_btn.disableProperty().bind(model.readyToChat.not().or(model.connected));
        chat_view.setItems(model.chatHistory);

        message_txt.setOnAction(event -> handleSendMessage());

        send_btn.setOnAction(evt -> handleSendMessage());


        login_btn.setOnAction(evt -> {
            try {

                clientEndPoint = new ChatClientEndPoint(new URI("ws://localhost:8080/?username=" + model.userName.get()));
                clientEndPoint.addMessageHandler(responseString -> {
                    Platform.runLater(() -> {
                        model.chatHistory.add(getStringMessage(responseString));
                    });
                });

                clientEndPoint.awaitAuth(5, TimeUnit.SECONDS);

                if(clientEndPoint.authenticated) {
                    model.connected.set(true);
                }
                else {
                    showDialog("Username invalid");
                }

            } catch (Exception e) {
                showDialog("Error: " + e.getMessage());
            }

        });
    }

    private void handleSendMessage() {
        clientEndPoint.sendMessage(getJsonMessage(model.userName.get(), model.currentMessage.get()));
        model.currentMessage.set("");
        message_txt.requestFocus();
    }

    private void showDialog(final String message) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        VBox box = new VBox();
        box.getChildren().addAll(new Label(message));
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5));
        dialogStage.setScene(new Scene(box));
        dialogStage.show();
    }

}
