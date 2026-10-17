package week4.core;

public class CoreMemory {

    // 256 bytes of internal data memory
    private int[] dataCoreMemory;

    // Program memory
    private String[] programCoreMemory;

    public CoreMemory() {
        dataCoreMemory = new int[256];
        programCoreMemory = new String[256];
        reset();
    }

    public void reset() {
        for (int i = 0; i < dataCoreMemory.length; i++) {
            dataCoreMemory[i] = 0;
        }

        for (int i = 0; i < programCoreMemory.length; i++) {
            programCoreMemory[i] = null;
        }
    }

    // Data memory

    public int readData(int address) {
        checkAddress(address);
        return dataCoreMemory[address];
    }

    public void writeData(int address, int value) {
        checkAddress(address);
        dataCoreMemory[address] = value & 0xFF;
    }

    // Program memory

    public String readCoreInstruction(int address) {
        if (address < 0 || address >= programCoreMemory.length) {
            throw new IllegalArgumentException("Invalid program memory address");
        }

        return programCoreMemory[address];
    }

    public void writeCoreInstruction(int address, String instruction) {
        if (address < 0 || address >= programCoreMemory.length) {
            throw new IllegalArgumentException("Invalid program memory address");
        }

        programCoreMemory[address] = instruction;
    }

    public int getProgramSize() {
        int size = 0;

        for (String instruction : programCoreMemory) {
            if (instruction != null) {
                size++;
            }
        }

        return size;
    }

    private void checkAddress(int address) {
        if (address < 0 || address >= dataCoreMemory.length) {
            throw new IllegalArgumentException("Invalid memory address");
        }
    }
}
