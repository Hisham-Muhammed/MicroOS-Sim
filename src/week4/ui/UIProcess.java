
package week4.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class UIProcess {

    private static final Path UI_TO_CORE =
        Paths.get("/tmp/microos_ui_to_core.fifo");

    private static final Path CORE_TO_UI =
        Paths.get("/tmp/microos_core_to_ui.fifo");

    public static void main(String[] args) {

        System.out.println("MicroOS-Sim Week 4 UI");

        if (!Files.exists(UI_TO_CORE) ||
            !Files.exists(CORE_TO_UI)) {

            System.out.println("FIFO not found.");
            System.out.println("Create both FIFOs first.");
            return;
        }

        System.out.println("Waiting for Core...");

        try (
            BufferedWriter toCore =
                Files.newBufferedWriter(
                    UI_TO_CORE,
                    StandardCharsets.UTF_8
                );

            BufferedReader fromCore =
                Files.newBufferedReader(
                    CORE_TO_UI,
                    StandardCharsets.UTF_8
                );

            Scanner sc = new Scanner(System.in)
        ) {

            System.out.println("Connected to Core.");
            System.out.println(
                "Commands: LOAD STEP RUN RESET SHOW EXIT"
            );

            boolean running = true;

            while (running) {

                System.out.print("ui> ");

                if (!sc.hasNextLine()) {
                    break;
                }

                String command =
                    sc.nextLine().trim().toUpperCase();

                if (command.isEmpty()) {
                    continue;
                }

                if (!isValid(command)) {
                    System.out.println("Invalid command.");
                    continue;
                }

                // Send command to Core
                toCore.write(command);
                toCore.newLine();
                toCore.flush();

                // Receive response from Core
                String response = fromCore.readLine();

                if (response == null) {
                    System.out.println(
                        "Core disconnected."
                    );
                    break;
                }

                System.out.println("Core: " + response);

                if (command.equals("EXIT")) {
                    running = false;
                }
            }

        } catch (IOException e) {

            System.out.println(
                "FIFO communication error: "
                + e.getMessage()
            );

            System.out.println(
                "Check Core process and FIFO setup."
            );
        }

        System.out.println("UI Process stopped.");
    }

    private static boolean isValid(String command) {

        return command.equals("LOAD")
            || command.equals("STEP")
            || command.equals("RUN")
            || command.equals("RESET")
            || command.equals("SHOW")
            || command.equals("EXIT");
    }
}
