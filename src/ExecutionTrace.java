public class ExecutionTrace {

    private StringBuilder trace;

    public ExecutionTrace() {
        trace = new StringBuilder();
    }

    public void clear() {
        trace.setLength(0);
    }

    public void addFetch() {
        trace.append("FETCH ✓\n");
    }

    public void addDecode() {
        trace.append("DECODE ✓\n");
    }

    public void addExecute() {
        trace.append("EXECUTE ✓\n");
    }

    public void addMessage(String message) {
        trace.append(message).append("\n");
    }

    public String getTrace() {
        return trace.toString();
    }
}
