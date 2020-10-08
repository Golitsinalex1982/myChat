package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BotClient extends Client {

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }


    protected String getUserName(){
        int x = (int) (Math.random() * 100);
        return "date_bot_"+x;
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды:" +
                    " дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
// а)	Вывести в консоль текст полученного сообщения message.
// б)	Получить из message имя отправителя и текст сообщения. Они разделены ": ".
// в)	Отправить ответ в зависимости от текста принятого сообщения.
//
//Выполняем а), пробуем выполнить б), но если в сообщении нет ": ", то б) у нас выполнить не получается, надо завершить работу метода.
        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (!message.contains(": ")) return;
            String[] strings = message.split(": ");
            if (strings.length != 2) return;
            String userName = strings[0];
            String text = strings[1];
            SimpleDateFormat simpleDateFormat;
            switch (text) {
                case "дата":
                    simpleDateFormat = new SimpleDateFormat("d.MM.YYYY");
                    break;
                case "день":
                    simpleDateFormat = new SimpleDateFormat("d");
                    break;
                case "месяц":
                    simpleDateFormat = new SimpleDateFormat("MMMM");
                    break;
                case "год":
                    simpleDateFormat = new SimpleDateFormat("YYYY");
                    break;
                case "время":
                    simpleDateFormat = new SimpleDateFormat("H:mm:ss");
                    break;
                case "час":
                    simpleDateFormat = new SimpleDateFormat("H");
                    break;
                case "минуты":
                    simpleDateFormat = new SimpleDateFormat("m");
                    break;
                case "секунды":
                    simpleDateFormat = new SimpleDateFormat("s");
                    break;
                default:
                    simpleDateFormat = null;
                    break;
            }

            if (simpleDateFormat != null) {
                Calendar calendar = Calendar.getInstance();
                String result = "Информация для " + userName + ": ";
                result += simpleDateFormat.format(calendar.getTime());
                sendTextMessage(result);
            }


        }
    }

    public static void main(String[] args){
        BotClient botClient = new BotClient();
        botClient.run();
    }

}
