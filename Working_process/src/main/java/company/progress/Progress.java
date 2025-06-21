package company.progress;

import java.time.LocalDate;
import java.time.LocalTime;

public class Progress {

    private LocalDate date;
    private int workerId;
    private LocalTime startTime;
    private LocalTime endTime;
    private int workingHours;
    private double effectiveness;

    public Progress() {};

    public LocalDate getDate() {
        return date;
    }

    public int getWorkerId() {
        return workerId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public double getEffectiveness() {
        return effectiveness;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setWorkingHours(int workingHours) {
        this.workingHours = workingHours;
    }

    public void setEffectiveness(double effectiveness) {
        this.effectiveness = effectiveness;
    }
}
