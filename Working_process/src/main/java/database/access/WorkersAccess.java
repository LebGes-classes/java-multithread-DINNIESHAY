package database.access;

import company.worker.Worker;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Iterator;

public class WorkersAccess {

    private static Sheet workersSheet;

    public static void init(Sheet sheet) {
        workersSheet = sheet;
    }

    public static ArrayList<Worker> getAll() {
        ArrayList<Worker> workers = new ArrayList<>();
        Iterator<Row> iterator = workersSheet.iterator();

        if (iterator.hasNext()) {
            iterator.next();
        }

        while (iterator.hasNext()) {
            Row currRow = iterator.next();

            Cell idCell = currRow.getCell(0);
            Cell nameCell = currRow.getCell(1);

            if (idCell != null && nameCell != null) {
                try {
                    int workerId = (int) currRow.getCell(0).getNumericCellValue();
                    String name = currRow.getCell(1).getStringCellValue();

                    Worker worker = new Worker(workerId, name);
                    workers.add(worker);
                } catch (Exception e) {
                    System.out.println("Ошибка при чтении данных из страницы Excel");
                }
            }
        }

        return workers;
    }

    public static int getSize() {
        ArrayList<Worker> workers = getAll();
        int size = workers.size();

        return size;
    }
}
