package week4.integration;

/**
 * Optional helper showing the intended three-process startup order.
 * Start Logging first, then Core, then UI in three terminal windows.
 *
 * This class does not replace the individual processes and does not
 * instantiate CPU, Memory, Stack, Queue, Simulator or Logger directly.
 */
public final class Week4Launcher {
    private Week4Launcher() {}

    public static void main(String[] args) {
        System.out.println("Start these three processes in separate terminals:");
        System.out.println("1) java -cp out week4.logging.LoggingProcess");
        System.out.println("2) java -cp out week4.core.CoreProcess");
        System.out.println("3) java -cp out week4.ui.UIProcess");
    }
}
