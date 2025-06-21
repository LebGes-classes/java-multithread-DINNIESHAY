package appcontrol.visual.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Services {

    private static final Scanner scanner = new Scanner(System.in);

    public static void printFile(File file) {
        try {
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine();
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Information not found.");
        }
    }

    public static String getInput() {
        return scanner.nextLine();
    }
}
