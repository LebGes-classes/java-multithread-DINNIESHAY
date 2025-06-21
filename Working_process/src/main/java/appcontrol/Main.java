package appcontrol;

import database.connection.ExcelDataBase;

public class Main {

    public static void main(String[] args) {
        ExcelDataBase.readExcelFile();
        AppController controller = new AppController();
        controller.openApp();
    }
}
