package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;
    public static void main(String[] args){
        new Client().run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {
                this.wait();
            } } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("При работе клиента возникла ошибка");
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду ‘exit’.");}
        else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while(clientConnected) {
            String line = ConsoleHelper.readString();
            if (line.equals("exit")) {break;};
            if (!shouldSendTextFromConsole()) {} else{sendTextMessage(line);};
        }
    }



    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера");
        String adress = ConsoleHelper.readString();
        return adress;
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера.");
        int port = ConsoleHelper.readInt();
        return port;
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите имя пользователя");
        String userName = ConsoleHelper.readString();
        return userName;
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {

        try {
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);}
        catch (IOException e) {
            ConsoleHelper.writeMessage("Во время отправки собщения произошла ошибка");
            clientConnected = false;
        }
    }


    public class SocketThread extends Thread {
        @Override
        public void run() {
            Socket socket;
            
            try {
                socket = new Socket(getServerAddress(), getServerPort());
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            }
            catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false); }

        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник " + userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник " + userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            synchronized (Client.this)
            {
                Client.this.clientConnected=clientConnected;
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    String userName = getUserName();
                    Message messageUserName = new Message(MessageType.USER_NAME, userName);
                    connection.send(messageUserName);
                }
                else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                }
                else throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
         while (true) {
             Message message = connection.receive();
             if (message.getType() == MessageType.TEXT) {
                 processIncomingMessage(message.getData());}
             else if (message.getType() == MessageType.USER_ADDED) {
                 informAboutAddingNewUser(message.getData()); }
             else if (message.getType() == MessageType.USER_REMOVED) {
                 informAboutDeletingNewUser(message.getData());
             }
             else throw new IOException("Unexpected MessageType");
         }
        }

    }

}
