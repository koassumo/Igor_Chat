package com.geekb.chat.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {    // интерфейс при появлении окна
    @FXML
    TextArea mainArea;

    @FXML
    TextField msgField;

    Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            network = new Network(8189);
            new Thread(() -> {
                try {
                    while (true) {
                        String msg = network.readMsg();
                        mainArea.appendText(msg + "\n");
                    }
                } catch (Exception e) {
                    Platform.runLater(()->{
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Соединение с сервером разорвано", ButtonType.OK);
                        alert.showAndWait();
                    });
                }
                finally {
                    network.close();
                }
            }).start();

        } catch (IOException e) {
           throw new RuntimeException("Невозможно подключиться к серверу");
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            network.sendMsg(msgField.getText());
            mainArea.appendText(msgField.getText() + "\n");
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить сообщение, проверьте подключение", ButtonType.OK);
            alert.showAndWait();
        }
    }


}
