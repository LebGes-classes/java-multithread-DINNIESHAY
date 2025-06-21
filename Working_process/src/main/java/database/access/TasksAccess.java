package database.access;

import company.task.Status;
import company.task.Task;
import database.connection.ExcelDataBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Iterator;

public class TasksAccess {

    private static Sheet tasksSheet;

    public static void init(Sheet sheet) {
        tasksSheet = sheet;
    }

    public static ArrayList<Task> getAvailableByWorker(int workerId) {
        ArrayList<Task> tasks = new ArrayList<>();
        Iterator<Row> iterator = tasksSheet.iterator();

        if (iterator.hasNext()) {
            iterator.next();
        }

        while (iterator.hasNext()) {
            Row currRow = iterator.next();

            Cell idCell = currRow.getCell(0);
            Cell workerIdCell = currRow.getCell(2);
            Cell statusCell = currRow.getCell(5);

            if (idCell != null && (int) workerIdCell.getNumericCellValue() == workerId && (
                statusCell.getStringCellValue().equals("NOT_STARTED") || statusCell.getStringCellValue().equals("IN_PROGRESS"))) {
                try {
                    int taskId = (int) currRow.getCell(0).getNumericCellValue();
                    String name = currRow.getCell(1).getStringCellValue();
                    int totalHours = (int) currRow.getCell(3).getNumericCellValue();
                    int completedHours = (int) currRow.getCell(4).getNumericCellValue();
                    Status status = Status.valueOf(currRow.getCell(5).getStringCellValue());

                    Task task = new Task(taskId, name, workerId, totalHours, completedHours, status);
                    tasks.add(task);
                } catch (Exception e) {
                    System.out.println("Ошибка при чтении данных из страницы Excel");
                }
            }
        }

        return tasks;
    }

    public static void update(Task task) {
        int rowIndex = getRowIndex(task);
        if (rowIndex != -1) {
            Row row = tasksSheet.getRow(rowIndex);
            row.getCell(4).setCellValue(task.getCompletedHours());
            row.getCell(5).setCellValue(task.getStatus().toString());
        }

        ExcelDataBase.saveExcelFile();
    }

    private static int getRowIndex(Task task) {
        int rowIndex = -1;

        for (int i = 1; i <= tasksSheet.getLastRowNum(); i++) {
            Row row = tasksSheet.getRow(i);
            if (row != null) {
                Cell idCell = row.getCell(0);
                if (idCell != null && (int) idCell.getNumericCellValue() == task.getId()) {
                    rowIndex = i;
                }
            }
        }

        return rowIndex;
    }
}
