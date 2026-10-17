package week4.core;

import week4.ipc.FifoConfig;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class CoreProcess {
    private final SimulatorAdapter simulator;
    private BufferedWriter uiWriter;
    private Socket uiSocket;
    private Socket logSocket;
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private volatile boolean logSenderRunning = true;
    private Thread logSenderThread;

    private CoreProcess() {
        simulator = new SimulatorAdapter();
    }

    public static void main(String[] args) {
        new CoreProcess().start();
    }

    private void start() {
        System.out.println("Week 4 Core Process (TCP Socket IPC)");
        System.out.println("UI <-> Core port: " + FifoConfig.UI_CORE_PORT);
        System.out.println("Core -> Logging port: " + FifoConfig.CORE_LOG_PORT);
        startLogSender();

        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(FifoConfig.UI_CORE_PORT));
            System.out.println("Waiting for UI Process on port " + FifoConfig.UI_CORE_PORT + "...");
            uiSocket = server.accept();
            System.out.println("UI Process connected: " + uiSocket.getRemoteSocketAddress());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        uiSocket.getInputStream(), StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        uiSocket.getOutputStream(), StandardCharsets.UTF_8))) {
                uiWriter = writer;
                log("INFO", "Core process connected to UI");
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equalsIgnoreCase("LOAD")) {
                        handleLoad(reader);
                    } else if (!handleCommand(line.trim())) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Core Process error: " + e.getMessage());
        } finally {
            logQueue.offer("INFO|Core process stopped");
            logQueue.offer("EXIT");
            logSenderRunning = false;
            if (logSenderThread != null) {
                try { logSenderThread.join(2000); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            try { if (uiSocket != null) uiSocket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleLoad(BufferedReader reader) throws IOException {
        List<String> program = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            if ("END_LOAD".equals(line)) {
                break;
            }
            if (line.startsWith("PROGRAM|")) {
                program.add(new String(
                        java.util.Base64.getDecoder().decode(line.substring(8)),
                        StandardCharsets.UTF_8));
            }
        }

        long start = System.nanoTime();
        try {
            simulator.loadProgram(program.toArray(new String[0]));
            long elapsed = System.nanoTime() - start;
            log("INFO", "Program loaded (" + program.size() + " instructions)");
            send("OK|Program loaded|" + elapsed);
        } catch (Exception e) {
            log("ERROR", "Program load error: " + e.getMessage());
            send("ERROR|Program load error: " + e.getMessage());
        }
    }

    private boolean handleCommand(String command) throws IOException {
        if (command.isEmpty()) {
            send("ERROR|Empty command");
            log("ERROR", "Empty command");
            return true;
        }

        long start = System.nanoTime();
        try {
            switch (command.toUpperCase()) {
                case "STEP":
                    simulator.step();
                    logExecution("STEP");
                    send("OK|STEP|" + buildStatus() + "|TIME_NS=" + (System.nanoTime() - start));
                    return true;

                case "RUN":
                    simulator.runWithInstructionLogging(this::logInstruction);
                    log("INFO", "RUN completed: " + simulator.status());
                    send("OK|RUN|" + buildStatus() + "|TIME_NS=" + (System.nanoTime() - start));
                    return true;

                case "RESET":
                    simulator.reset();
                    log("INFO", "Simulator reset");
                    send("OK|RESET|" + buildStatus() + "|TIME_NS=" + (System.nanoTime() - start));
                    return true;

                case "SHOW":
                    send("OK|SHOW|" + encodeStatus(buildStatus()) + "|TIME_NS=" + (System.nanoTime() - start));
                    return true;

                case "EXIT":
                    log("INFO", "Simulator stopped");
                    send("OK|EXIT|Core stopping");
                    return false;

                default:
                    log("ERROR", "Invalid command: " + command);
                    send("ERROR|Invalid command: " + command);
                    return true;
            }
        } catch (Exception e) {
            log("ERROR", "Execution error: " + e.getMessage());
            send("ERROR|" + e.getMessage());
            return true;
        }
    }

    private void logExecution(String operation) {
        logInstruction(simulator.currentInstructionText());
        String status = simulator.status();
        if (status.startsWith("Execution error")) {
            log("ERROR", status);
        } else if (status.startsWith("Unsupported")) {
            log("ERROR", status);
        } else {
            log("INFO", operation + " completed: " + status);
        }
    }

    private void logInstruction(String instruction) {
        if (instruction == null) {
            return;
        }

        log("INFO", "Instruction executed: " + instruction);
        String upper = instruction.trim().toUpperCase();

        if (upper.startsWith("PUSH")) log("INFO", "PUSH executed");
        if (upper.startsWith("POP")) log("INFO", "POP executed");
        if (upper.startsWith("ENQUEUE")) log("INFO", "ENQUEUE executed");
        if (upper.startsWith("DEQUEUE")) log("INFO", "DEQUEUE executed");
    }

    private String buildStatus() {
        return "STATUS=" + simulator.status()
                + "\n"
                + simulator.cpuState()
                + "\n"
                + simulator.stackState()
                + "\n"
                + simulator.queueState()
                + "\nTRACE\n"
                + simulator.trace();
    }

    private String encodeStatus(String status) {
        return java.util.Base64.getEncoder().encodeToString(
                status.getBytes(StandardCharsets.UTF_8));
    }

    private void send(String message) throws IOException {
        uiWriter.write(message.replace("\n", "\\n"));
        uiWriter.newLine();
        uiWriter.flush();
    }

    private void log(String level, String message) {
        logQueue.offer(level + "|" + message.replace("\n", " "));
    }

    private void startLogSender() {
        Thread thread = new Thread(() -> {
            try {
                Socket socket = null;
                for (int i = 0; i < 30 && socket == null; i++) {
                    try { socket = new Socket(FifoConfig.HOST, FifoConfig.CORE_LOG_PORT); }
                    catch (IOException e) {
                        try { Thread.sleep(500); }
                        catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
                if (socket == null) {
                    System.err.println("Logging process unavailable on port " + FifoConfig.CORE_LOG_PORT);
                    return;
                }
                logSocket = socket;
                try (Socket connected = socket;
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                             connected.getOutputStream(), StandardCharsets.UTF_8))) {
                    while (logSenderRunning || !logQueue.isEmpty()) {
                        String message = logQueue.poll();
                        if (message == null) {
                            try { Thread.sleep(10); }
                            catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            continue;
                        }
                        writer.write(message);
                        writer.newLine();
                        writer.flush();
                        if ("EXIT".equals(message)) break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Log socket unavailable: " + e.getMessage());
            }
        }, "week4-core-log-sender");
        thread.setDaemon(true);
        logSenderThread = thread;
        thread.start();
    }

    private static final class SimulatorAdapter {
        private final CoreSimulator simulator = new CoreSimulator();

        void loadProgram(String[] program) {
            simulator.loadProgram(program);
        }

        void step() { simulator.step(); }
        void run() { simulator.run(); }
        void reset() { simulator.reset(); }
        String status() { return simulator.status(); }
        String trace() { return simulator.trace(); }
        String currentInstructionText() { return simulator.currentInstructionText(); }
        String cpuState() { return simulator.cpuState(); }
        String stackState() { return simulator.stackState(); }
        String queueState() { return simulator.queueState(); }
        void runWithInstructionLogging(java.util.function.Consumer<String> logger) {
            simulator.runWithInstructionLogging(logger);
        }
    }

}
