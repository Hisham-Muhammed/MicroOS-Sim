package week4.ipc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FifoUtil {
    private FifoUtil() {}

    public static void ensureFifo(String path) throws IOException {
        Path fifo = Path.of(path);
        if (Files.exists(fifo)) {
            if (!Files.isSymbolicLink(fifo) && !Files.isRegularFile(fifo)) {
                return;
            }
            if (Files.isRegularFile(fifo)) {
                throw new IOException("Path exists but is not a FIFO: " + path);
            }
            return;
        }

        Process process = new ProcessBuilder("mkfifo", path).inheritIO().start();
        try {
            int exit = process.waitFor();
            if (exit != 0) {
                throw new IOException("mkfifo failed for " + path + " (exit " + exit + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while creating FIFO: " + path, e);
        }
    }
}
