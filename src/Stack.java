/**
 * 8051-style stack helper.
 * The stack grows upward: PUSH increments SP before writing,
 * POP reads the current top and then decrements SP.
 */
public class Stack {
    private final Memory memory;
    private final CPU cpu;
    private final int startAddress;
    private final int endAddress;

    public Stack(Memory memory, CPU cpu) {
        this(memory, cpu, 0x07, 0xFF);
    }

    public Stack(Memory memory, CPU cpu, int startAddress, int endAddress) {
        if (startAddress < 0 || endAddress > 0xFF || startAddress >= endAddress) {
            throw new IllegalArgumentException("Invalid stack range");
        }
        this.memory = memory;
        this.cpu = cpu;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    public void reset() {
        cpu.setSP(startAddress);
    }

    public void push(int value) {
        if (cpu.getSP() >= endAddress) {
            throw new IllegalStateException("Stack overflow");
        }
        int newSP = cpu.getSP() + 1;
        cpu.setSP(newSP);
        memory.writeData(newSP, value);
    }

    public int pop() {
        if (cpu.getSP() <= startAddress) {
            throw new IllegalStateException("Stack underflow");
        }
        int value = memory.readData(cpu.getSP());
        cpu.setSP(cpu.getSP() - 1);
        return value;
    }

    public boolean isEmpty() {
        return cpu.getSP() == startAddress;
    }

    public boolean isFull() {
        return cpu.getSP() >= endAddress;
    }

    public int size() {
        return cpu.getSP() - startAddress;
    }

    public String getState() {
        StringBuilder out = new StringBuilder();
        out.append("STACK\n");
        out.append("SP : ").append(String.format("%02X", cpu.getSP())).append("\n");
        out.append("Size : ").append(size()).append("\n");
        out.append("Status : ").append(isEmpty() ? "EMPTY" : isFull() ? "FULL" : "READY").append("\n");

        if (isEmpty()) {
            out.append("Contents : [empty]\n");
        } else {
            out.append("Top -> ");
            for (int address = cpu.getSP(); address > startAddress; address--) {
                out.append(String.format("%02X", memory.readData(address)));
                if (address > startAddress + 1) out.append(" | ");
            }
            out.append(" <- Bottom\n");
        }
        return out.toString();
    }
}
