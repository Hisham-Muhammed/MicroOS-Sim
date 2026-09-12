# MicroOS-Sim Documentation Guide

This folder contains the project documentation, meeting records, weekly progress, decisions, design notes, and Week 3 validation material.

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

## Programs (`programs/`)

| File | Use |
|---|---|
| `demo.txt` | General simulator demonstration program. |
| `week3_queue_validation.txt` | Week 3 Assembly validation program for multiple ENQUEUE/DEQUEUE operations and FIFO ordering. |

## Tests (`tests/`)

| File | Use |
|---|---|
| `InstructionTest.java` | Tests the existing instruction behaviour. |
| `DemoProgramTest.java` | Tests the demo program execution. |
| `Week3InstructionExamples.java` | Provides examples for the Week 3 instructions. |
| `Week3MemoryStackQueueTest.java` | Tests Memory, Stack and FIFO Queue functionality added in Week 3. |

## Documentation Folders

### `Meeting/`
Contains meeting minutes, discussions, decisions and action items.

### `weekly-status/`
Contains weekly progress reports including planned work, completed work, pending work, issues and individual responsibilities.

### `decisions/`
Contains important technical and project decisions made during development.

### `Documents/`
Contains supporting study and design documents related to the STC89C52, memory, instruction handling and operating-system concepts.

### `Week-3/`
Contains Week 3 flowchart, status and validation documentation.

## Images (`images/`)

`images/week3-instructions/` contains the output screenshots used in the Week 3 README for instruction demonstrations.

## Important Note

`.class` files are compiled Java output files. The editable project source is contained in the `.java` files under `src/`.
