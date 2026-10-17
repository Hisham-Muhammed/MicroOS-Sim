package week4.logging;

public final class LogMessage {
    private final String level;
    private final String message;

    private LogMessage(String level, String message) {
        this.level = level;
        this.message = message;
    }

    public static LogMessage parse(String line) {
        if (line == null) {
            return null;
        }
        int separator = line.indexOf('|');
        if (separator <= 0 || separator == line.length() - 1) {
            return null;
        }

        String level = line.substring(0, separator).trim().toUpperCase();
        String message = line.substring(separator + 1).trim();

        if (!level.equals("INFO") && !level.equals("ERROR")) {
            return null;
        }
        if (message.isEmpty()) {
            return null;
        }
        return new LogMessage(level, message);
    }

    public String getLevel() { return level; }
    public String getMessage() { return message; }
}
