package bot;

/*
  Класс телеграм-бота наследуется от TelegramLongPollingBot
  TelegramLongPollingBot — это тип бота для Telegram,
  который использует механизм Long Polling
  для получения обновлений (сообщений, команд и других событий) от сервера Telegram.
*/

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {

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

        // 2 случай: ожидаем от пользователя ввода времени для обрезки
        if (usersStatus.get(chatId).equals("WAITING_CUT_TIME")) {
            processCutTimeMessage(chatId, message);
            return;
        }

        // 3 случай: пользователь выбирает команду
        switch (message) {
            //Запуск бота
            case "/start":
                sendStartText(chatId);
                break;
            //Отмена действия
            case "/cancel":
                processCancelCommand(chatId);
                break;
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

    private void sendStartText(Long chatId) {

    }

    private void processCancelCommand(Long chatId) {

    }

    private void processCutCommand(Long chatId) {

    }

    private void processRotateCommand(Long chatId) {

    }

    private void processMirrorCommand(Long chatId) {

    }

    private void sendErrorText(Long chatId) {

    }

    private void processCutTimeMessage(Long chatId, String message) {

    }

    private void processNonTextMessage(Update update) {

    }

    private void processVideoMessage(Update update) {

    }
    
    public String getBotUsername() {
        return "video_ed_bot";
    }

    public String getBotToken() {
        return "7292771794:AAERcGR1HodjgV0kklZGwpwJEljpqWAsjGc";
    }
}
