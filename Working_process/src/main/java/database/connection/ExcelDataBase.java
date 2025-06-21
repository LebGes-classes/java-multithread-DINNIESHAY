package database.connection;

import database.access.ProgressAccess;
import database.access.TasksAccess;
import database.access.WorkersAccess;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class ExcelDataBase {

    private static final String FILE_PATH = "C:/Users/din20/IdeaProjects/Working_process/Working_process.xlsx";
    public static FileInputStream file;
    public static Workbook workbook;
    private static final Object lock = new Object();

    public static void readExcelFile() {
        try {
            file = new FileInputStream(FILE_PATH);
            workbook = new XSSFWorkbook(file);

            WorkersAccess.init(workbook.getSheet("Workers"));
            ProgressAccess.init(workbook.getSheet("Progress"));
            TasksAccess.init(workbook.getSheet("Tasks"));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при открытии Excel файла: " + e.getMessage());
        }
    }

    public static synchronized void closeExcelFile() {
        synchronized (lock) {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при закрытии Excel файла", e);
            }
        }
    }

    public static synchronized void saveExcelFile() {
        synchronized (lock) {
            try {
                FileOutputStream outputFile = new FileOutputStream(FILE_PATH);
                workbook.write(outputFile);
                outputFile.close();

                if (file != null) {
                    file.close();
                }

                file = new FileInputStream(FILE_PATH);
                workbook = new XSSFWorkbook(file);
                WorkersAccess.init(workbook.getSheet("Workers"));
                ProgressAccess.init(workbook.getSheet("Progress"));
                TasksAccess.init(workbook.getSheet("Tasks"));
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при сохранении Excel файла: " + e.getMessage());
            }
        }
    }
}
