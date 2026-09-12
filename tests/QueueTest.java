public class QueueTest {
    public static void main(String[] args) {
        Queue queue = new Queue(3);
        queue.enqueue(10); queue.enqueue(20); queue.enqueue(30);
        if (!queue.isFull()) throw new AssertionError("Full condition failed");
        if (queue.dequeue() != 10) throw new AssertionError("FIFO #1 failed");
        if (queue.dequeue() != 20) throw new AssertionError("FIFO #2 failed");
        if (queue.dequeue() != 30) throw new AssertionError("FIFO #3 failed");
        if (!queue.isEmpty()) throw new AssertionError("Empty condition failed");
        System.out.println("TC05 Enqueue / Full : PASS");
        System.out.println("TC06 Dequeue / FIFO : PASS");
        System.out.println("TC07 Empty condition : PASS");
    }
}
