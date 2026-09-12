public class MemoryTest {
    public static void main(String[] args) {
        Memory memory = new Memory();
        memory.writeData(10, 0xAB);
        if (memory.readData(10) != 0xAB) throw new AssertionError("Read/Write failed");
        memory.reset();
        if (memory.readData(10) != 0) throw new AssertionError("Reset failed");
        System.out.println("TC01 Memory Write/Read : PASS");
        System.out.println("TC02 Memory Reset      : PASS");
    }
}
