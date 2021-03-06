package com.geekb.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server; //каждый должен понимать на каком сервере он крутится
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        new Thread(()->{
            try {
                while (true){ // цикл аутентификации
                    String msg = in.readUTF();
                    System.out.println("Сообщение от клиента:" + msg + "\n");
                    if (msg.startsWith("/auth ")) {
                        String[] tokens = msg.split(" ", 3);      // "\\s" -можно просто пробел " "
                        String nickFromAuthManager = server.getAuthManager().getNicknameByLoginAndPassword(tokens[1],tokens[2]);
                        if (nickFromAuthManager != null) {
                            if (server.isNickBusy(nickFromAuthManager)) {
                                sendMsg("Данный пользователь уже в чате");
                                continue;
                            }
                            nickname = nickFromAuthManager;
                            server.subscribe(this);
                            sendMsg("/authok " + nickname);
                            break;
                        } else {
                            sendMsg("Указан неверный логин/пароль");
                        }
                    }
                }
                while (true){  // цикл общения с сервером
                    String msg = in.readUTF();
                    System.out.print("Сообщение от клиента:" + msg + "\n");
                    if (msg.startsWith("/")) {
                        if (msg.equals("/end")){
                            sendMsg("/end_confirm");
                            break;
                        }
                    } else {
                        server.broadcastMsg(nickname + ": " + msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }).start();
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        server.unsubscribe(this);
        nickname = null;
        try {
            if (in!= null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
