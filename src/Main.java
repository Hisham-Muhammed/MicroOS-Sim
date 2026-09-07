import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String PROGRAM_PATH = "../programs/demo.txt";

    public static void main(String[] args) {

        Simulator simulator = new Simulator();
        Scanner scanner = new Scanner(System.in);
        String[] loadedProgram = null;
        int stepNumber = 1;

        printHeader();
        printCommands();

        while (true) {

            System.out.print("> ");
            String command = scanner.nextLine().trim().toUpperCase();

            if (command.isEmpty()) {
                continue;
            }

            switch (command) {

                case "LOAD":
                    String[] program = loadProgramFromFile();

                    if (program == null) {
                        break;
                    }

                    loadedProgram = program;
                    simulator.loadProgram(loadedProgram);
                    stepNumber = 1;

                    System.out.println();
                    System.out.println("PROGRAM LOADED");
                    System.out.println("--------------------------------");
                    printProgram(loadedProgram);
                    System.out.println();
                    System.out.println("Ready. Use STEP to execute one instruction.");
                    System.out.println();
                    break;

                case "STEP":
                    if (loadedProgram == null) {
                        System.out.println("No program loaded. Type LOAD first.");
                        System.out.println();
                        break;
                    }

                    if (simulator.getExecutionStatus().equals("Program terminated")
                            || simulator.getExecutionStatus().equals("Program finished")) {
                        System.out.println("Program has already finished. Type RESET to start again.");
                        System.out.println();
                        break;
                    }

                    simulator.step();

                    if (simulator.getCurrentInstruction() == null) {
                        System.out.println("Program finished.");
                        System.out.println();
                        break;
                    }

                    printStep(simulator, stepNumber);
                    stepNumber++;
                    break;

                case "RUN":
                    if (loadedProgram == null) {
                        System.out.println("No program loaded. Type LOAD first.");
                        System.out.println();
                        break;
                    }

                    if (simulator.getExecutionStatus().equals("Program terminated")
                            || simulator.getExecutionStatus().equals("Program finished")) {
                        System.out.println("Program has already finished. Type RESET to start again.");
                        System.out.println();
                        break;
                    }

                    System.out.println();
                    System.out.println("RUNNING PROGRAM...");
                    System.out.println("================================");
                    System.out.println();

                    int safetyCounter = 0;

                    while (safetyCounter < 1000) {

                        simulator.step();

                        if (simulator.getCurrentInstruction() == null) {
                            break;
                        }

                        printStep(simulator, stepNumber);
                        stepNumber++;
                        safetyCounter++;

                        if (simulator.getExecutionStatus().equals("Program terminated")
                                || simulator.getExecutionStatus().equals("Program finished")
                                || simulator.getExecutionStatus().startsWith("Execution error")
                                || simulator.getExecutionStatus().startsWith("Unsupported instruction")) {
                            break;
                        }
                    }
                    break;

                case "SHOW":
                    if (loadedProgram == null) {
                        System.out.println("No program loaded. Type LOAD first.");
                        System.out.println();
                        break;
                    }

                    System.out.println();
                    System.out.println("CURRENT CPU STATE");
                    System.out.println("--------------------------------");
                    System.out.println(simulator.getCPU().getState());
                    System.out.println("STATUS : " + simulator.getExecutionStatus());
                    System.out.println();
                    break;

                case "RESET":
                    if (loadedProgram == null) {
                        System.out.println("No program loaded. Type LOAD first.");
                        System.out.println();
                        break;
                    }

                    // Reload the currently loaded program so CPU, PC and memory
                    // return to the initial state without rereading the file.
                    simulator.loadProgram(loadedProgram);
                    stepNumber = 1;

                    System.out.println();
                    System.out.println("CPU RESET SUCCESSFULLY");
                    System.out.println("Program is ready from the first instruction.");
                    System.out.println();
                    break;

                case "HELP":
                    printCommands();
                    break;

                case "EXIT":
                case "QUIT":
                    System.out.println("Exiting ONJI BYTE SIMULATOR...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Unknown command. Type HELP to see available commands.");
                    System.out.println();
            }
        }
    }

    private static String[] loadProgramFromFile() {

        List<String> programList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(PROGRAM_PATH))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (!line.isEmpty()) {
                    programList.add(line);
                }
            }

        } catch (IOException e) {

            System.out.println();
            System.out.println("ERROR: Could not read demo.txt");
            System.out.println(e.getMessage());
            System.out.println();
            return null;
        }

        return programList.toArray(new String[0]);
    }

    private static void printHeader() {
        System.out.println();
        System.out.println("================================");
        System.out.println("       ONJI BYTE SIMULATOR");
        System.out.println("================================");
        System.out.println();
    }

    private static void printCommands() {
        System.out.println("COMMANDS");
        System.out.println("--------------------------------");
        System.out.println("LOAD   - Load program from demo.txt");
        System.out.println("STEP   - Execute one instruction");
        System.out.println("RUN    - Execute remaining instructions");
        System.out.println("SHOW   - Show current CPU state");
        System.out.println("RESET  - Reset CPU and program position");
        System.out.println("HELP   - Show commands");
        System.out.println("EXIT   - Exit simulator");
        System.out.println();
    }

    private static void printProgram(String[] program) {
        for (int i = 0; i < program.length; i++) {
            System.out.println(
                    String.format("%04X  %s", i, program[i])
            );
        }
    }

    private static void printStep(Simulator simulator, int stepNumber) {

        System.out.println("STEP " + stepNumber);
        System.out.println("--------------------------------");
        System.out.println(
                "Instruction : " + simulator.getCurrentInstruction()
        );
        System.out.println(
                "Category    : " + simulator.getCurrentInstruction().getCategory()
        );
        System.out.println();

        System.out.print(simulator.getExecutionTrace());
        System.out.println();

        System.out.println(simulator.getCPU().getState());
        System.out.println("STATUS : " + simulator.getExecutionStatus());
        System.out.println();
        System.out.println("================================");
        System.out.println();
    }
}
