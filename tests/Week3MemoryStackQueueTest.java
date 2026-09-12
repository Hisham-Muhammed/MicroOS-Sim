public class Week3MemoryStackQueueTest {
    public static void main(String[] args) {
        testMemory();
        testStack();
        testQueue();
        testSimulatorInstructions();
        System.out.println("All Week 3 tests passed.");
    }

    private static void testMemory() {
        Memory memory = new Memory();
        memory.writeData(10, 0xAB);
        assertEquals(0xAB, memory.readData(10), "Memory read/write");
        memory.reset();
        assertEquals(0, memory.readData(10), "Memory reset");
    }

    private static void testStack() {
        CPU cpu = new CPU();
        Memory memory = new Memory();
        Stack stack = new Stack(memory, cpu);
        stack.push(10);
        stack.push(20);
        assertEquals(20, stack.pop(), "Stack LIFO #1");
        assertEquals(10, stack.pop(), "Stack LIFO #2");
        assertEquals(0x07, cpu.getSP(), "Stack pointer reset after pop");
    }

    private static void testQueue() {
        Queue queue = new Queue(3);
        queue.enqueue(10);
        queue.enqueue(20);
        queue.enqueue(30);
        assertTrue(queue.isFull(), "Queue full condition");
        assertEquals(10, queue.dequeue(), "FIFO #1");
        assertEquals(20, queue.dequeue(), "FIFO #2");
        assertEquals(30, queue.dequeue(), "FIFO #3");
        assertTrue(queue.isEmpty(), "Queue empty condition");
    }

    private static void testSimulatorInstructions() {
        Simulator simulator = new Simulator();
        simulator.loadProgram(new String[] {
            "MOV A,#10", "ENQUEUE A", "MOV A,#20", "ENQUEUE A",
            "DEQUEUE R0", "DEQUEUE R1", "PUSH R0", "PUSH R1", "POP A", "END"
        });
        simulator.run();
        assertEquals(10, simulator.getCPU().getRegister(0), "Simulator FIFO first value");
        assertEquals(20, simulator.getCPU().getRegister(1), "Simulator FIFO second value");
        assertEquals(20, simulator.getCPU().getA(), "Simulator POP value");
    }

    private static void assertEquals(int expected, int actual, String test) {
        if (expected != actual) throw new AssertionError(test + ": expected " + expected + ", got " + actual);
    }

    private static void assertTrue(boolean value, String test) {
        if (!value) throw new AssertionError(test);
    }
}
