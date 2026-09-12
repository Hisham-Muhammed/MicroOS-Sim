public class StackTest {
    public static void main(String[] args) {
        CPU cpu = new CPU();
        Memory memory = new Memory();
        Stack stack = new Stack(memory, cpu);
        stack.push(0x10);
        stack.push(0x20);
        if (stack.pop() != 0x20) throw new AssertionError("LIFO #1 failed");
        if (stack.pop() != 0x10) throw new AssertionError("LIFO #2 failed");
        if (cpu.getSP() != 0x07) throw new AssertionError("SP failed");
        System.out.println("TC03 PUSH/POP LIFO : PASS");
        System.out.println("TC04 Stack Pointer : PASS");
    }
}
