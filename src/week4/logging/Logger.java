package week4.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger implements AutoCloseable {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BufferedWriter writer;

    public Logger(Path logFile) throws IOException {
        Path parent = logFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        writer = Files.newBufferedWriter(
                logFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    public synchronized void log(LogMessage message) throws IOException {
        if (message == null) {
            return;
        }
        writer.write("[" + message.getLevel() + "] "
                + LocalDateTime.now().format(FORMAT)
                + " - " + message.getMessage());
        writer.newLine();
        writer.flush();
    }

    public synchronized void logRaw(String line) throws IOException {
        LogMessage message = LogMessage.parse(line);
        if (message != null) {
            log(message);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        writer.close();
    }
}
