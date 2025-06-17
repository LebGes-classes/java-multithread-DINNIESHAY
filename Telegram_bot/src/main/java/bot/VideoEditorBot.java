package bot;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import videoeditor.VideoEditor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/*
  Класс телеграм-бота наследуется от TelegramLongPollingBot
  TelegramLongPollingBot — это тип бота для Telegram,
  который использует механизм Long Polling
  для получения обновлений (сообщений, команд и других событий) от сервера Telegram.
*/
public class VideoEditorBot extends TelegramLongPollingBot {

    private final String userName = "video_ed_bot";
    private final String token = "7292771794:AAERcGR1HodjgV0kklZGwpwJEljpqWAsjGc";

    private enum Status {
        WAITING_COMMAND,
        WAITING_CUT_TIME
    }

    private Map<Long, Status> usersStatus = new HashMap<>();
    private Map<Long, File> tempVideoFiles = new HashMap<>();

    /*
      Создаем расширяемый пул потоков.
      Этот метод создаёт новые потоки по запросу, но переиспользует
      уже созданные потоки, когда они становятся доступными.
      Подходит для приложений, которые запускают множество недолговечных асинхронных задач.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

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
        if (usersStatus.getOrDefault(chatId, null).equals(Status.WAITING_CUT_TIME)) {
            executor.execute(() -> processCutTimeMessage(chatId, message));
            return;
        }

        // 4 случай: пользователь выбирает команду
        if (usersStatus.getOrDefault(chatId, null).equals(Status.WAITING_COMMAND)) {
            switch (message) {
                //Обрезка видео
                case "/cut":
                    executor.execute(() -> processCutCommand(chatId));
                    break;
                //Поворот видео на 90°
                case "/rotate":
                    executor.execute(() -> processRotateCommand(chatId));
                    break;
                //Отзеркаливание видео
                case "/mirror":
                    executor.execute(() -> processMirrorCommand(chatId));
                    break;
                default:
                    //При некорректном вводе команды выводится сообщение об ошибке
                    sendErrorText(chatId);
            }
        }
    }

    private void processNonTextMessage(Update update) {
        Long chatId = update.getMessage().getChatId();

        // 1 случай: пользователь отправил видео
        if (update.getMessage().hasVideo()) {
            try {
                Thread.sleep(1000);
                File videoFile = downloadVideo(update.getMessage().getVideo().getFileId());
                tempVideoFiles.put(chatId, videoFile);
            } catch (TelegramApiException e) {
                sendText(chatId, "Не удалось загрузить видео.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            usersStatus.put(chatId, Status.WAITING_COMMAND);
            //Отправляем ему сообщение с выбором действий для видео
            sendCommandsText(chatId);
            // 2 случай: пользователь отправил файл иного формата
        } else {
            //Отправляем сообщение о некорректном типе файла
            sendInvalidFileText(chatId);
        }
    }

    //Обработка команды "/cut"
    private void processCutCommand(Long chatId) {
        usersStatus.put(chatId, Status.WAITING_CUT_TIME);
        sendCutTimeMessage(chatId);
    }

    private void processCutTimeMessage(Long chatId, String timeInput) {

        //Делим время на две части - начало промежутка и конец промежутка
        String[] parts = timeInput.split("-");
        if (parts.length != 2) {
            sendText(chatId, "Некорректный формат времени. Используйте MM:SS-MM:SS\nНапример: 00:05-01:15");
            return;
        }

        //Переводим время в секунды
        try {
            int startSec = changeTimeToSeconds(parts[0]);
            int endSec = changeTimeToSeconds(parts[1]);

            if (startSec >= endSec) {
                sendText(chatId, "Конечное время должно быть больше начального! Попробуйте ещё раз.");
                return;
            }

            //При отсутствии видео, отправляем сообщение об ошибке
            File videoFile = tempVideoFiles.remove(chatId);
            if (videoFile == null || !videoFile.exists()) {
                sendVideoNotFoundText(chatId);
                usersStatus.put(chatId, null);
                return;
            }

            //Проверка, не выходит ли введенное время за пределы допустимого
            int videoDuration = getVideoDuration(videoFile);
            if (endSec > videoDuration) {
                sendText(chatId, "Указанное время выходит за пределы видео. Длительность видео: " +
                        secondsToTimeFormat(videoDuration) + ".\nПопробуйте ещё раз.");
                // Возвращаем файл обратно в хранилище
                tempVideoFiles.put(chatId, videoFile);
                return;
            }

            //Отправляем сообщение об обработке видео
            sendProcessingText(chatId);
            try {
                //Обрезаем видео
                File editedVideo = VideoEditor.cutVideo(videoFile, startSec, endSec);
                //Отправляем готовое видео пользователю
                sendVideo(chatId, editedVideo);
                sendText(chatId, "Готово! Видео успешно обрезано.");
            } catch (Exception e) {
                sendVideoErrorText(chatId);
            }
        } catch (Exception e) {
            sendText(chatId, "Некорректный формат времени. Используйте MM:SS-MM:SS\nНапример: 00:05-01:15");
        }
    }

    //Перевод времени в секунды
    private int changeTimeToSeconds(String time) {
        String[] parts = time.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException();
        }

        int minutes = Integer.parseInt((parts[0]));
        int seconds = Integer.parseInt(parts[1]);
        int totalSeconds = minutes * 60 + seconds;

        return totalSeconds;
    }

    //Получение длительности видео в секундах
    private int getVideoDuration(File videoFile) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {
            grabber.start();
            //Длительность в микросекундах, переводим в секунды
            long duration = grabber.getLengthInTime() / 1000000;
            grabber.stop();

            return (int) duration;
        }
    }

    //Перевод секунд в формат MM:SS
    private String secondsToTimeFormat(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    //Обработка команды "/rotate"
    private void processRotateCommand(Long chatId) {
        try {
            File videoFile = tempVideoFiles.remove(chatId);
            if (videoFile == null || !videoFile.exists()) {
                sendVideoNotFoundText(chatId);
                usersStatus.put(chatId, null);
                return;
            }

            sendProcessingText(chatId);
            File editedVideo = VideoEditor.rotateVideo(videoFile);
            sendVideo(chatId, editedVideo);
            sendText(chatId, "Готово! Видео успешно перевёрнуто.");
            editedVideo.delete();
            usersStatus.put(chatId, null);
        } catch (Exception e) {
            sendVideoErrorText(chatId);
        }
    }

    //Обработка команды "/mirror"
    private void processMirrorCommand(Long chatId) {
        try {
            File videoFile = tempVideoFiles.remove(chatId);
            if (videoFile == null || !videoFile.exists()) {
                sendVideoNotFoundText(chatId);
                usersStatus.put(chatId, null);
                return;
            }

            sendProcessingText(chatId);
            File editedVideo = VideoEditor.mirrorVideo(videoFile);
            sendVideo(chatId, editedVideo);
            sendText(chatId, "Готово! Ваше видео успешно отзеркалено.");
            editedVideo.delete();
            usersStatus.put(chatId, null);
        } catch (Exception e) {
            sendVideoErrorText(chatId);
        }
    }

    private File downloadVideo(String fileId) throws TelegramApiException {
        //Получаем информацию о файле
        GetFile file = new GetFile();
        file.setFileId(fileId);
        String filePath = execute(file).getFilePath();

        //Создаем URL для скачивания
        String videoUrl = "https://api.telegram.org/file/bot" + getBotToken() + "/" + filePath;

        //Создаем файл
        File videoFile = new File("temp_video_" + System.currentTimeMillis() + ".mp4");

        try (InputStream inputStream = new URL(videoUrl).openStream()) {
            //Скачиваем и сохраняем файл
            Files.copy(inputStream, videoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return videoFile;
        } catch (IOException e) {
            throw new TelegramApiException();
        }
    }

    //Отправка видео пользователю
    private void sendVideo(Long chatId, File videoFile) {
        SendVideo video = new SendVideo();
        video.setChatId(chatId.toString());
        video.setVideo(new InputFile(videoFile));

        try {
            Thread.sleep(1000);
            execute(video);
        } catch (TelegramApiException e) {
            sendText(chatId, "Не удалось отправить видео.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
        String text = "Привет! С помощью этого бота ты можешь редактировать видео.\n" +
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

    //Отправлка сообщения об отсутствии видео
    private void sendVideoNotFoundText(Long chatId) {
        String text = "Видео не найдено. Попробуйте ещё раз";

        sendText(chatId, text);
    }

    //Отправка сообщения об ошибке при редактировании
    private void sendVideoErrorText(Long chatId) {
        String text = "Ошибка при редактировании видео.";

        sendText(chatId, text);
    }

    //Отправка сообщения об обработке видео
    private void sendProcessingText(Long chatId) {
        String text = "Обрабатываю видео...";

        sendText(chatId, text);
    }

    public String getBotUsername() {
        return userName;
    }

    public String getBotToken() {
        return token;
    }
}
