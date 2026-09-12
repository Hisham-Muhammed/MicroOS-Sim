## Source Files (`src/`)

| File | Use |
|---|---|
| `Main.java` | **UI integration** – creates the simulator interface and displays CPU, Memory, Stack, Queue and execution information. |
| `Simulator.java` | **Simulator integration** – connects the CPU, Memory, Stack, Queue and program execution flow. |
| `CPU.java` | **CPU execution** – stores CPU state and executes supported instructions. |
| `Instruction.java` | **Instruction representation** – stores a parsed instruction and its operands. |
| `InstructionSet.java` | **Instruction definitions** – defines the instructions supported by the simulator. |
| `Memory.java` | **Memory management** – provides memory Read/Write, reset and address handling. |
| `Stack.java` | **Stack data structure** – manages Stack Pointer (SP), PUSH and POP operations. |
| `Queue.java` | **FIFO Queue** – manages ENQUEUE and DEQUEUE operations and queue status. |
| `ExecutionTrace.java` | **Execution trace** – records instruction execution and displays the execution history. |