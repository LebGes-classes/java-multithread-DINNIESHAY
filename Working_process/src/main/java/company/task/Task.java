package company.task;

public class Task {

    private final int id;
    private final String name;
    private final int workerId;
    private final int totalHours;
    private int completedHours;
    private Status status;

    public Task(int id, String name, int workerId, int totalHours, int completedHours, Status status) {
        this.id = id;
        this.name = name;
        this.workerId = workerId;
        this.totalHours = totalHours;
        this.completedHours = completedHours;
        this.status = status;
    }

    public int getRemainingHours() {
        int remainingHours = totalHours - completedHours;

        return remainingHours;
    }

    public int getCompletedHours() {
        return completedHours;
    }

    public int getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void addCompletedHours(int hours) {
        completedHours += hours;
    }

    public boolean isCompleted() {
        boolean isCompleted;

        if (completedHours == totalHours) {
            isCompleted = true;
        } else {
            isCompleted = false;
        }

        return isCompleted;
    }

    public String toString() {
        return "Задача " + id +
                ": " + name +
                ", Длительность: " + totalHours + " ч." +
                ", Выполнено: " + completedHours + " ч." +
                ", Статус: " + status +
                "\n";
    }
}
