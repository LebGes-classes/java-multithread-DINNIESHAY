package videoeditor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoEditor {

    //Обрезка видео
    public static File cutVideo(File input, int startSec, int endSec) throws Exception {
        String outputPath = "cut_video_" + System.currentTimeMillis() + ".mp4";
        //FFmpegFrameGrabber используется для чтения и декодирования видеофайлов, позволяет извлекать отдельные кадры
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
            grabber.start();

            //FFmpegFrameRecorder позволяет кодировать и записывать кадры в новые видеофайлы
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, grabber.getImageWidth(), grabber.getImageHeight())) {
                /*
                  Установка видеокодека H.264.
                  Видеокодек — это программа или алгоритм, который сжимает видеоданные (видеофайл, видеопоток) и восстанавливает сжатые данные.
                 */
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                //Установка формата mp4
                recorder.setFormat("mp4");
                //Увеличиваем битрейт
                recorder.setVideoBitrate(grabber.getVideoBitrate() * 2);
                //Максимальное качество (0 - наилучшее, 51 - наихудшее)
                recorder.setVideoQuality(0);
                //Сохраняем исходный FPS
                recorder.setFrameRate(grabber.getFrameRate());

                //Настройка аудиопараметров, если в исходном видео есть аудио
                if (grabber.getAudioChannels() > 0) {
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                    recorder.setAudioChannels(grabber.getAudioChannels());
                    recorder.setAudioBitrate(grabber.getAudioBitrate());
                    recorder.setSampleRate(grabber.getSampleRate());
                }

                recorder.start();

                /*
                  grabber.getFrameRate() получает количество кадров в секунду (FPS) текущего видео (тип double).
                  start * (int) grabber.getFrameRate() умножаем время в секундах (startSec) на FPS, чтобы получить номер кадра.
                  grabber.setVideoFrameNumber(...) – перематывает видео к указанному кадру.
                  После этого следующий вызов grabber.grab() начнёт чтение с этого кадра.
                */
                grabber.setVideoFrameNumber(startSec * (int) grabber.getFrameRate());

                while (true) {
                    //Захватываем очередной кадр
                    var frame = grabber.grab();
                    /*
                      Выходим из цикла, если кадр пуст (видео закончилось) или время > endTime
                      grabber.getTimestamp() возвращает текущую позицию в микросекундах, поэтому endSec преобразуем в микросекунды
                     */
                    if (frame == null || grabber.getTimestamp() > endSec * 1_000_000L) {
                        break;
                    }

                    //Записываем кадр в выходной файл
                    recorder.record(frame);
                }
            }
        }

        return new File(outputPath);
    }

    //Поворот видео на 90°
    public static File rotateVideo(File input) throws Exception {
        String outputPath = "rotated_video_" + System.currentTimeMillis() + ".mp4";
        //FFmpegFrameGrabber используется для чтения и декодирования видеофайлов, позволяет извлекать отдельные кадры
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
            grabber.start();

            //FFmpegFrameRecorder позволяет кодировать и записывать кадры в новые видеофайлы
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, grabber.getImageHeight(), grabber.getImageWidth())) {
                //Установка видеокодека H.264
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                //Установка формата mp4
                recorder.setFormat("mp4");
                //Увеличиваем битрейт
                recorder.setVideoBitrate(grabber.getVideoBitrate() * 2);
                //Максимальное качество (0 - наилучшее, 51 - наихудшее)
                recorder.setVideoQuality(0);
                //Сохраняем исходный FPS
                recorder.setFrameRate(grabber.getFrameRate());

                //Настройка аудиопараметров, если в исходном видео есть аудио
                if (grabber.getAudioChannels() > 0) {
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                    recorder.setAudioChannels(grabber.getAudioChannels());
                    recorder.setAudioBitrate(grabber.getAudioBitrate());
                    recorder.setSampleRate(grabber.getSampleRate());
                }

                recorder.start();

                /*
                  Java2DFrameConverter преобразует кадры (Frame из OpenCV/FFmpeg) в объекты Java2D (BufferedImage) и обратно.
                  Это позволяет накладывать графику, текст, фильтры на видео и т.п.
                 */
                Java2DFrameConverter converter = new Java2DFrameConverter();

                while (true) {
                    //Захватываем очередной кадр
                    var frame = grabber.grab();
                    //Выходим из цикла, если кадр пуст (видео закончилось)
                    if (frame == null) {
                        break;
                    }

                    //Поворачиваем кадр на 90°
                    BufferedImage image = converter.convert(frame);
                    if (image != null) {
                        BufferedImage rotated = rotateImage(image);
                        recorder.record(converter.convert(rotated));
                    }
                    //Обрабатываем аудиофрейм
                    if (frame.samples != null) {
                        recorder.record(frame);
                    }
                }
            }
        }

        return new File(outputPath);
    }

    public static File mirrorVideo(File input) throws Exception {
        String outputPath = "mirrored_video_" + System.currentTimeMillis() + ".mp4";
        //FFmpegFrameGrabber используется для чтения и декодирования видеофайлов, позволяет извлекать отдельные кадры
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input)) {
            grabber.start();

            //FFmpegFrameRecorder позволяет кодировать и записывать кадры в новые видеофайлы
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, grabber.getImageWidth(), grabber.getImageHeight())) {
                //Установка видеокодека H.264
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                //Установка формата mp4
                recorder.setFormat("mp4");
                //Увеличиваем битрейт
                recorder.setVideoBitrate(grabber.getVideoBitrate() * 2);
                //Максимальное качество (0 - наилучшее, 51 - наихудшее)
                recorder.setVideoQuality(0);
                //Сохраняем исходный FPS
                recorder.setFrameRate(grabber.getFrameRate());

                //Настройка аудиопараметров, если в исходном видео есть аудио
                if (grabber.getAudioChannels() > 0) {
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                    recorder.setAudioChannels(grabber.getAudioChannels());
                    recorder.setAudioBitrate(grabber.getAudioBitrate());
                    recorder.setSampleRate(grabber.getSampleRate());
                }

                recorder.start();

                /*
                  Java2DFrameConverter преобразует кадры (Frame из OpenCV/FFmpeg) в объекты Java2D (BufferedImage) и обратно.
                  Это позволяет накладывать графику, текст, фильтры на видео и т.п.
                */
                Java2DFrameConverter converter = new Java2DFrameConverter();
                while (true) {
                    //Захватываем очередной кадр
                    var frame = grabber.grab();
                    //Выходим из цикла, если кадр пуст (видео закончилось)
                    if (frame == null) {
                        break;
                    }

                    //Зеркально отражаем кадр
                    BufferedImage image = converter.convert(frame);
                    if (image != null) {
                        BufferedImage mirrored = mirrorImage(image);
                        recorder.record(converter.convert(mirrored));
                    }
                    //Обрабатываем аудиофрейм
                    if (frame.samples != null) {
                        recorder.record(frame);
                    }
                }
            }
        }

        return new File(outputPath);
    }

    private static BufferedImage rotateImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        //Создаем пустое изображение с новыми размерами: ширина и высота меняются местами
        BufferedImage rotated = new BufferedImage(height, width, image.getType());

        //Рисуем исходное изображение
        Graphics2D graphics = rotated.createGraphics();
        //Переносим начало координат в правый верхний угол
        graphics.translate(height, 0);
        //Поворачиваем на 90° по часовой стрелке
        graphics.rotate(Math.toRadians(90));
        //Рисуем исходное изображение
        graphics.drawImage(image, 0, 0, null);
        //Освобождаем ресурсы
        graphics.dispose();

        return rotated;
    }

    private static BufferedImage mirrorImage(BufferedImage image) {
        //Создаем пустое изображение с такими же размерами и свойствами
        BufferedImage mirrored = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        //Применяем зеркальное отражение по горизонтали
        Graphics2D graphics = mirrored.createGraphics();
        graphics.drawImage(
                image,
                image.getWidth(), 0,
                -image.getWidth(), image.getHeight(),
                null
        );
        //Освобождаем ресурсы
        graphics.dispose();

        return mirrored;
    }
}