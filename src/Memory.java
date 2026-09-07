public class Memory {

    // 256 bytes of internal data memory
    private int[] dataMemory;

    // Program memory
    private String[] programMemory;

    public Memory() {
        dataMemory = new int[256];
        programMemory = new String[256];
        reset();
    }

    public void reset() {
        for (int i = 0; i < dataMemory.length; i++) {
            dataMemory[i] = 0;
        }

        for (int i = 0; i < programMemory.length; i++) {
            programMemory[i] = null;
        }
    }

    // Data memory

    public int readData(int address) {
        checkAddress(address);
        return dataMemory[address];
    }

    public void writeData(int address, int value) {
        checkAddress(address);
        dataMemory[address] = value & 0xFF;
    }

    // Program memory

    public String readInstruction(int address) {
        if (address < 0 || address >= programMemory.length) {
            throw new IllegalArgumentException("Invalid program memory address");
        }

        return programMemory[address];
    }

    public void writeInstruction(int address, String instruction) {
        if (address < 0 || address >= programMemory.length) {
            throw new IllegalArgumentException("Invalid program memory address");
        }

        programMemory[address] = instruction;
    }

    public int getProgramSize() {
        int size = 0;

        for (String instruction : programMemory) {
            if (instruction != null) {
                size++;
            }
        }

        return size;
    }

    private void checkAddress(int address) {
        if (address < 0 || address >= dataMemory.length) {
            throw new IllegalArgumentException("Invalid memory address");
        }
    }
}
