package bot;

/*
  Класс телеграм-бота наследуется от TelegramLongPollingBot
  TelegramLongPollingBot — это тип бота для Telegram,
  который использует механизм Long Polling
  для получения обновлений (сообщений, команд и других событий) от сервера Telegram.
*/

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {

    private String userName = "video_ed_bot";
    private String token = "7292771794:AAERcGR1HodjgV0kklZGwpwJEljpqWAsjGc";

    private Map<Long, String> usersStatus = new HashMap<>();

    //Обработка сообщений пользователя
    public void onUpdateReceived(Update update) {

        // 1 случай: пользователь отправил сообщение без текста
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            processNonTextMessage(update);
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();

        // 2 случай: пользователь запустил бот
        if (message.equals("/start")) {
            sendStartText(chatId);
            return;
        }

        // 3 случай: ожидаем от пользователя ввода времени для обрезки
        if (usersStatus.get(chatId).equals("WAITING_CUT_TIME")) {
            processCutTimeMessage(chatId, message);
            return;
        }

        // 4 случай: пользователь выбирает команду
        if (usersStatus.get(chatId).equals("WAITING_COMMAND")) {
            switch (message) {
                //Обрезка видео
                case "/cut":
                    processCutCommand(chatId);
                    break;
                //Поворот видео на 90°
                case "/rotate":
                    processRotateCommand(chatId);
                    break;
                //Отзеркаливание видео
                case "/mirror":
                    processMirrorCommand(chatId);
                    break;
                default:
                    //При некорректном вводе команды выводится сообщение об ошибке
                    sendErrorText(chatId);
            }
        }
    }

    private void processNonTextMessage(Update update) {
        
    }

    private void processCutCommand(Long chatId) {

    }

    private void processCutTimeMessage(Long chatId, String timeInput) {

    }

    private int changeTimeToSeconds(String part) {

    }

    private void processRotateCommand(Long chatId) {

    }

    private void processMirrorCommand(Long chatId) {

    }

    //Отправка текстового сообщения
    private void sendText(Long chatId, String text) {
        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(chatId.toString());
        messageToSend.setText(text);

        try {
            execute(messageToSend);
        } catch (TelegramApiException e) {
            sendText(chatId, "Произошла ошибка.");
        }
    }

    //Отправка сообщения с приветствием
    private void sendStartText(Long chatId) {
        String text = "Привет! С помощью этого бота ты можешь редактировать видео.\n +" +
                "Используй эти команды:\n" +
                "/cut - обрезать видео\n" +
                "/rotate - повернуть на 90°\n" +
                "/mirror - зеркально отразить\n\n" +
                "Отправь мне видео и затем выбери команду!";

        sendText(chatId, text);
    }

    //Отправка сообщения о некорректном формате файла
    private void sendInvalidFileText(Long chatId) {
        String text = "Некорректный формат файла. Попробуйте ещё раз.";

        sendText(chatId, text);
    }

    //Отправка сообщения о некорректном вводе команды
    private void sendErrorText(Long chatId) {
        String text = "Неизвестная команда. Попробуйте ещё раз.";

        sendText(chatId, text);
    }

    //Отправка сообщения о выборе действия
    private void sendCommandsText(Long chatId) {
        String text = "Видео получено! Выбери действие:\n" +
                "/cut - обрезать видео\n" +
                "/rotate - повернуть на 90°\n" +
                "/mirror - зеркально отразить";

        sendText(chatId, text);
    }

    //Отправка сообщения об указании времени обрезки
    private void sendCutTimeMessage(Long chatId) {
        String text = "Укажи время желаемого начала видео в формате: MM:SS-MM:SS.\n" +
                "Например: 00:05-01:15";

        sendText(chatId, text);
    }

    public String getBotUsername() {
        return userName;
    }

    public String getBotToken() {
        return token;
    }
}
