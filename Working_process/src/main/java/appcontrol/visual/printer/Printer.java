package appcontrol.visual.printer;

import appcontrol.visual.services.Services;

import java.io.File;

public class Printer {

    public void printMenu() {
        Services.printFile(new File("C:/Users/din20/IdeaProjects/Working_process/src/main/java/appcontrol/visual/printer/textfiles/menu.txt"));
    }
}
