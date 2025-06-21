package company.worker;

public class Worker {

    private final int id;
    private final String fullName;

    public Worker(int id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }
}
