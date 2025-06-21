package company.worker;

import company.progress.Progress;
import company.task.Status;
import company.task.Task;
import database.access.ProgressAccess;
import database.access.TasksAccess;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class WorkerThread implements Runnable {

    private Worker worker;

    public WorkerThread(Worker worker) {
        this.worker = worker;
    }

    public void run() {
        try {
            startTasks();
        } catch (InterruptedException e) {
            System.out.println("Ошибка в рабочем дне у " + worker.getFullName());
            e.printStackTrace();
        }
    }

    private void startTasks() throws InterruptedException {
        Progress progress = new Progress();
        progress.setWorkerId(worker.getId());
        progress.setDate(LocalDate.now());
        progress.setStartTime(LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
        System.out.println(worker.getFullName() + " начал/а рабочий день");

        ArrayList<Task> workerTasks = TasksAccess.getAvailableByWorker(worker.getId());

        int hours = 0;
        int workingHours = 0;

        Iterator<Task> taskIterator = workerTasks.iterator();
        while (taskIterator.hasNext() && hours < 8) {
            Task task = taskIterator.next();

            int taskTime = Math.min(task.getRemainingHours(), 8 - hours);
            hours += taskTime;
            workingHours += taskTime;

            System.out.println(worker.getFullName() + " начал/а работу над задачей № " + task.getId() + " " + task.getName());

            TimeUnit.SECONDS.sleep(taskTime);

            synchronized (Task.class) {
                task.addCompletedHours(taskTime);

                if (task.isCompleted()) {
                    task.setStatus(Status.COMPLETED);
                    System.out.println(worker.getFullName() + " полностью завершил/а задачу №" + task.getId() + " " + task.getName());
                } else {
                    task.setStatus(Status.IN_PROGRESS);
                    System.out.println(worker.getFullName() + " частично завершил/а задачу №" + task.getId() + " " + task.getName());
                }
            }

            TasksAccess.update(task);

            int randomBreak = ThreadLocalRandom.current().nextInt(1, 3);
            hours += randomBreak;
        }

        System.out.println(worker.getFullName() + " завершил/а рабочий день. Отработано " + hours + "ч.");
        progress.setEndTime(LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
        progress.setWorkingHours(hours);
        progress.setEffectiveness(((double) workingHours / 8.0) * 100.0);
        ProgressAccess.add(progress);
    }
}
