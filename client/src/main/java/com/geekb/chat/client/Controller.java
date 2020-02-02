package com.geekb.chat.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {    // интерфейс при появлении окна
    @FXML
    TextArea textArea;

    @FXML
    TextField msgField, loginField, passField;

    @FXML
    HBox loginBox, newTextBox;

    private Network network;
    private boolean authenticated;
    private String nickname;

    public void setAuthenticated(boolean authenticated){
        this.authenticated = authenticated;
        loginBox.setVisible(!authenticated);
        loginBox.setManaged(!authenticated);
        newTextBox.setVisible(authenticated);
        newTextBox.setManaged(authenticated);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setAuthenticated(false);
            network = new Network(8189);
            Thread t = new Thread(() -> {           //new Thread(() -> {
                try {
                    while (true) {
                        String msg = network.readMsg();
                        if (msg.startsWith("/authok ")){
                            nickname = msg.split(" ")[1];
                            setAuthenticated(true);
                            break;
                        }
                        textArea.appendText(msg + "\n");
                    }
                    while (true) {
                        String msg = network.readMsg();
                        if (msg.equals("/end_confirm")){        //чтобы избежать всплывающего окна
                            textArea.appendText("Завершено общение с сервером");
                            break;
                        }
                        textArea.appendText(msg + "\n");
                    }
                } catch (Exception e) {
                    Platform.runLater(()->{         // если нужно вернуться в поток окна и там выполнить часть кода
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Соединение с сервером разорвано", ButtonType.OK);
                        alert.showAndWait();
                    });
                }
                finally {
                    network.close();
                    Platform.exit();
                }
            });      //.start();
            t.setDaemon(true);          //чтобы поток закрылся с окном
            t.start();
        } catch (IOException e) {
           throw new RuntimeException("Невозможно подключиться к серверу");
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            network.sendMsg(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить сообщение, проверьте подключение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void tryToAuth(ActionEvent actionEvent){
        try {
            network.sendMsg("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить сообщение, проверьте подключение", ButtonType.OK);
            alert.showAndWait();
        }
    }


}
