package week4.ipc;

/** Cross-platform TCP socket IPC configuration for Week 4. */
public final class FifoConfig {
    public static final String HOST = "127.0.0.1";
    public static final int UI_CORE_PORT = 5051;
    public static final int CORE_LOG_PORT = 5052;

    // Legacy names retained only so the old FIFO smoke-test source still compiles.
    // The Windows Socket IPC workflow does not use these paths.
    public static final String UI_TO_CORE = "/tmp/microos_ui_to_core.fifo";
    public static final String CORE_TO_UI = "/tmp/microos_core_to_ui.fifo";
    public static final String CORE_TO_LOG = "/tmp/microos_core_to_log.fifo";

    private FifoConfig() {}
}
