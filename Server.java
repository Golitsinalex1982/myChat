package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.javarush.task.task30.task3008.MessageType.*;

public class Server {
    private static Map<String, Connection> connectionMap =
            new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Введите порт сервера");
        ServerSocket server = new ServerSocket(ConsoleHelper.readInt());
        ConsoleHelper.writeMessage("Сервер запущен");
        try {
            while (true) {
                Socket socket = server.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            System.out.println(" произошла ошибка.");
            server.close();
        }
    }


    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            System.out.println("Было установлено соединение с "+
                    socket.getRemoteSocketAddress());    //вывел мне в консоль инфу о соединении
            Connection newConnection = null;
            String userName = "";
            try {
                 newConnection = new Connection(socket);  //создал новое соединение
                 userName = serverHandshake(newConnection); //запрос имени, добавление имени
                                                       // в мэп и оповещение об этом
                 sendBroadcastMessage(new Message(USER_ADDED, userName)); //оповещение других участников о
                                                                    // о новом участнике
                 notifyUsers(newConnection, userName); //оповещение клиента о других уч.
                 serverMainLoop(newConnection, userName);} //процесс
            catch (Exception e) {
                 e.printStackTrace();
                 ConsoleHelper.writeMessage("ошибка при обмене данными с удаленным адресом"); }

            finally {
                if (userName != null){
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED,userName));
                }
            }
            ConsoleHelper.writeMessage("соединение с удаленным адресом закрыто");

        }

        
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name = null;
            Message zaprosOfName = new Message(NAME_REQUEST, "Введите свое имя");
            boolean povtor = true;
            while (povtor) {
                connection.send(zaprosOfName);
                Message messagenameClient = connection.receive();
                name = messagenameClient.getData();
                   if ((messagenameClient.getType() == USER_NAME) && (!name.isEmpty()) &&
                           (!connectionMap.containsKey(name))) {
                      connectionMap.put(name, connection);
                      connection.send(new Message(MessageType.NAME_ACCEPTED, "Ваше имя принято!"));
                      povtor = false;
                       }
                }
            return name;
        }


        private void notifyUsers(Connection connection, String userName) throws IOException{
          for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                String name = entry.getKey();
                if (!name.equals(userName)) {
                    Message uved = new Message(USER_ADDED, name);
                    connection.send(uved);}
            }
        }


        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String messageText = userName + ":" + " " + message.getData();
                    Message messageNew = new Message(MessageType.TEXT, messageText);
                    sendBroadcastMessage(messageNew);
                } else {
                    ConsoleHelper.writeMessage("Нe правильный формат сообщения!");
                }
            }
        }
    }


        public static void sendBroadcastMessage(Message message) {
            connectionMap.forEach((name, connection) -> {
                try {
                    connection.send(message);
                } catch (IOException e) {
                    System.out.println("Произошла ошибка при отправке сообщения");
                }
            });
        }

    }

