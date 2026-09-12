/** Simple fixed-size FIFO queue for the Week 3 simulator. */
public class Queue {
    private final int[] data;
    private int front;
    private int rear;
    private int size;

    public Queue() {
        this(8);
    }

    public Queue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Queue capacity must be positive");
        data = new int[capacity];
        reset();
    }

    public void reset() {
        front = 0;
        rear = 0;
        size = 0;
        java.util.Arrays.fill(data, 0);
    }

    public void enqueue(int value) {
        if (isFull()) throw new IllegalStateException("Queue overflow: queue is full");
        data[rear] = value & 0xFF;
        rear = (rear + 1) % data.length;
        size++;
    }

    public int dequeue() {
        if (isEmpty()) throw new IllegalStateException("Queue underflow: queue is empty");
        int value = data[front];
        data[front] = 0;
        front = (front + 1) % data.length;
        size--;
        return value;
    }

    public int peek() {
        if (isEmpty()) throw new IllegalStateException("Queue is empty");
        return data[front];
    }

    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == data.length; }
    public int size() { return size; }
    public int capacity() { return data.length; }

    public String getState() {
        StringBuilder out = new StringBuilder();
        out.append("FIFO QUEUE\n");
        out.append("Front : ").append(isEmpty() ? "-" : String.format("%02X", data[front])).append("\n");
        out.append("Rear  : ").append(isEmpty() ? "-" : String.format("%02X", data[(rear + data.length - 1) % data.length])).append("\n");
        out.append("Status: ").append(size).append(" / ").append(data.length);
        out.append(isEmpty() ? " (EMPTY)" : isFull() ? " (FULL)" : " (READY)").append("\n");
        out.append("Contents: [");
        for (int i = 0; i < size; i++) {
            if (i > 0) out.append(", ");
            out.append(String.format("%02X", data[(front + i) % data.length]));
        }
        out.append("]\n");
        return out.toString();
    }
}
