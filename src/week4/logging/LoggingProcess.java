package week4.logging;

import week4.ipc.FifoConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class LoggingProcess {
    private LoggingProcess() {}

    public static void main(String[] args) {
        System.out.println("Week 4 Logging Process (TCP Socket IPC)");
        System.out.println("Waiting for Core Process on port " + FifoConfig.CORE_LOG_PORT);

        Path logFile = Path.of("src", "week4", "logs", "simulator.log");
        try (ServerSocket server = new ServerSocket(FifoConfig.CORE_LOG_PORT);
             Logger logger = new Logger(logFile)) {
            try (Socket socket = server.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(
                         socket.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if ("EXIT".equalsIgnoreCase(line.trim())) {
                        logger.logRaw("INFO|Logging process stopped");
                        break;
                    }
                    LogMessage message = LogMessage.parse(line);
                    if (message == null) {
                        System.err.println("Ignored invalid log message: " + line);
                        continue;
                    }
                    logger.log(message);
                    System.out.println("Logged: " + line);
                }
            }
            System.out.println("Logging Process stopped.");
        } catch (IOException e) {
            System.err.println("Logging Process I/O error: " + e.getMessage());
            System.exit(1);
        }
    }
}
