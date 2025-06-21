package database.access;

import company.progress.Progress;
import database.connection.ExcelDataBase;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import java.time.format.DateTimeFormatter;

public class ProgressAccess {

    private static Sheet progressSheet;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void init(Sheet sheet) {
        progressSheet = sheet;
    }

    public static void add(Progress progress) {
        int newRowIndex = progressSheet.getLastRowNum() + 1;
        Row newRow = progressSheet.createRow(newRowIndex);

        newRow.createCell(0).setCellValue(progress.getDate().toString());
        newRow.createCell(1).setCellValue(progress.getWorkerId());
        newRow.createCell(2).setCellValue(progress.getStartTime().toString());
        newRow.createCell(3).setCellValue(progress.getEndTime().toString());
        newRow.createCell(4).setCellValue(progress.getWorkingHours());
        newRow.createCell(5).setCellValue(progress.getEffectiveness());

        ExcelDataBase.saveExcelFile();
    }
}
