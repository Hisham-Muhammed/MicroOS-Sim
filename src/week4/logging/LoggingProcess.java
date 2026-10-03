package week4.logging;

import week4.ipc.FifoConfig;
import week4.ipc.FifoUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class LoggingProcess {
    private LoggingProcess() {}

    public static void main(String[] args) {
        System.out.println("Week 4 Logging Process");
        System.out.println("FIFO: " + FifoConfig.CORE_TO_LOG);

        try {
            FifoUtil.ensureFifo(FifoConfig.CORE_TO_LOG);

            Path logFile = Path.of("src", "week4", "logs", "simulator.log");
            try (Logger logger = new Logger(logFile)) {
                System.out.println("Waiting for Core Process...");

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(FifoConfig.CORE_TO_LOG),
                                StandardCharsets.UTF_8))) {

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
            }

            System.out.println("Logging Process stopped.");
        } catch (IOException e) {
            System.err.println("Logging Process I/O error: " + e.getMessage());
            System.exit(1);
        }
    }
}
