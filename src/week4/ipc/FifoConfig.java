package week4.ipc;

public final class FifoConfig {
    public static final String UI_TO_CORE = "/tmp/microos_ui_to_core.fifo";
    public static final String CORE_TO_UI = "/tmp/microos_core_to_ui.fifo";
    public static final String CORE_TO_LOG = "/tmp/microos_core_to_log.fifo";

    private FifoConfig() {}
}
