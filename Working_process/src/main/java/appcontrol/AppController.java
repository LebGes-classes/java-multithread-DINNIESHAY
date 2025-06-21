package appcontrol;

import appcontrol.visual.printer.Printer;
import appcontrol.visual.services.Services;
import company.worker.Worker;
import company.worker.WorkerThread;
import database.access.WorkersAccess;
import database.connection.ExcelDataBase;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AppController {

    Printer printer = new Printer();
    ExecutorService executor = Executors.newFixedThreadPool(WorkersAccess.getSize());

    public void openApp() {

        boolean isRunning = true;

        while (isRunning) {
            printer.printMenu();
            String choice = Services.getInput();

            switch (choice) {
                case "E", "e":
                    isRunning = false;
                    System.out.println("Выход из программы...");
                    break;
                default:
                    isRunning = false;
                    startWorkDay();
                    shutDownAll();
            }
        }
    }

    private void shutDownAll() {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
            executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            System.out.println("Ошибка при завершении потоков:" + e.getMessage());
        } finally {
            ExcelDataBase.closeExcelFile();
        }
    }

    private void startWorkDay() {
        ArrayList<Worker> workers = WorkersAccess.getAll();
        for (Worker worker : workers) {
            WorkerThread thread = new WorkerThread(worker);
            executor.execute(thread);
        }
    }
}
