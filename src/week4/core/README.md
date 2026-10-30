# Week 4 – Core Process (`src/week4/core/`)

This package is the **Core Process** of the Week 4 MicroOS simulator. It holds the whole simulated 8051-style machine (CPU, memory, stack, queue, instruction decoding and execution) and runs it as its **own Java process**, separate from the UI and the Logging processes.

The Core never draws a screen and never writes log files. It only:

1. receives commands from the UI over TCP,
2. runs the simulator,
3. sends the result back to the UI,
4. sends log messages to the Logging process.

---

## Where Core fits in the Week 4 architecture

```
 ┌──────────────┐   TCP 127.0.0.1:5051    ┌──────────────┐   TCP 127.0.0.1:5052   ┌──────────────────┐
 │  UIProcess   │ ──────────────────────► │ CoreProcess  │ ─────────────────────► │  LoggingProcess  │
 │ (week4.ui)   │ ◄────────────────────── │ (week4.core) │                        │ (week4.logging)  │
 └──────────────┘   commands / responses  └──────────────┘   log messages         └──────────────────┘
```

- **UI → Core** (port `5051`): the UI sends `LOAD`, `STEP`, `RUN`, `RESET`, `SHOW`, `EXIT`. Core is the server and waits for the UI to connect.
- **Core → Logging** (port `5052`): Core is the client and sends `LEVEL|message` lines (for example `INFO|PUSH executed`).
- Ports and host come from `week4.ipc.FifoConfig`.

IPC was originally planned with POSIX FIFO named pipes, but that did not work on Windows. The team switched to **Java TCP sockets** (see `Docs/decisions/Week 4 Decisions.txt`). The `Fifo*` names in the `ipc` package are left over from that first approach.

---

## Why these files exist

| File | What it is | Why it is needed |
|---|---|---|
| `CoreProcess.java` | Entry point (`main`) of the Core process. Opens a `ServerSocket` on port 5051, reads commands from the UI, calls the simulator, replies, and forwards logs to the Logging process. | Makes Core a **separate process** that talks to the UI and Logging only through sockets, as required by the Week 4 IPC task. It is the only file in this package that does networking. |
| `CoreSimulator.java` | The simulator engine. Contains the **FETCH → DECODE → EXECUTE** cycle, `step()`, `run()`, `loadProgram()`, `reset()` and the implementation of every instruction (MOV, XCH, ADD, SUBB, ANL, ORL, INC, DEC, SJMP, CLR, PUSH, POP, ENQUEUE, DEQUEUE, END). Also builds the execution trace shown in the UI. | This is where the work actually happens. It ties together CPU, memory, stack, queue and instruction set so `CoreProcess` only needs to call a few simple methods. |
| `CoreCPU.java` | CPU state: accumulator `A`, register `B`, general registers `R0–R7`, `PC`, `SP`, and the Carry and Zero flags. Keeps values in range (8-bit for registers, 16-bit for PC). | Models the processor registers in one place. `getState()` produces the register dump the UI displays. |
| `CoreMemory.java` | 256 bytes of **data memory** and 256 slots of **program memory** (the instruction text). Checks addresses and masks values to 8 bits. | Gives the program a place to be stored and the stack a place to keep its data, like the 8051's internal RAM and program memory. |
| `CoreStack.java` | 8051-style stack on top of `CoreMemory` and `CoreCPU`. PUSH increments `SP` and then writes; POP reads and then decrements `SP`. Detects overflow and underflow. | Implements `PUSH` / `POP`. It uses the CPU's `SP` and data memory so the stack behaves like the real hardware stack instead of being a separate list. |
| `CoreQueue.java` | Fixed-size (8 entries) circular **FIFO queue** with enqueue, dequeue, peek, full/empty checks and a printable state. | Implements the project's `ENQUEUE` / `DEQUEUE` instructions, which are the FIFO part of the MicroOS idea. |
| `CoreInstruction.java` | A small data object holding one parsed instruction: name, operand and category. | Lets the simulator pass around a decoded instruction instead of raw strings. |
| `CoreInstructionSet.java` | The list of supported instructions and their categories (Data Transfer, Arithmetic, Logical, Control Flow, Stack, FIFO Queue, Termination). Validates a line and turns it into a `CoreInstruction`. | Decides what counts as a valid instruction. Unsupported instructions are rejected at **load time** with a clear error. |
| `Empty` | Placeholder file. | Only keeps the folder in Git before the real files were added. It can be removed. |

---

## Why the `Core` prefix?

Week 1–3 classes (`CPU`, `Memory`, `Stack`, `Queue`, `Instruction`, `InstructionSet`, `Simulator`) live in `src/` and are still used by the Week 1–3 console simulator. The Week 4 copies were renamed `Core*` and moved into the `week4.core` package so that:

- they do **not clash** with, or break, the existing Week 1–3 code (the Week 4 decision was to keep Week 1–3 functionality intact);
- it is obvious in the code which classes belong to the Week 4 Core process;
- Core can be started and tested on its own without importing the UI or Logging classes.

---

## How the pieces work together

```
CoreProcess  ──uses──►  CoreSimulator
                          ├── CoreCPU            (registers, PC, SP, flags)
                          ├── CoreMemory         (data + program memory)
                          ├── CoreStack          (uses CoreMemory + CoreCPU)
                          ├── CoreQueue          (FIFO, 8 entries)
                          └── CoreInstructionSet (validates → CoreInstruction)
```

**What happens on a `STEP` command**

1. `CoreProcess` receives `STEP` from the UI socket.
2. `CoreSimulator.step()` runs **fetch** (read the instruction at `PC`, then `PC++`), **decode**, then **execute**.
3. `CoreProcess` writes log messages (for example `Instruction executed: PUSH A`) into a queue; a background thread sends them to the Logging process.
4. `CoreProcess` replies to the UI with `OK|STEP|STATUS=...` containing the status, CPU registers, stack, queue and trace.

---

## Commands handled by `CoreProcess`

| Command | Effect |
|---|---|
| `LOAD` … `END_LOAD` | Followed by `PROGRAM|<base64 line>` entries. Validates and loads the program into memory, and resets CPU, stack and queue. |
| `STEP` | Executes one instruction. |
| `RUN` | Executes until `END`, end of program, an error, or the 1000-instruction safety limit. |
| `RESET` | Clears CPU, memory, stack, queue and trace. |
| `SHOW` | Returns the current machine state without executing anything. |
| `EXIT` | Stops the Core process. |

Replies start with `OK|` or `ERROR|`. Newlines inside a reply are escaped as `\n` so each reply stays on one line.

---

## Behaviour worth knowing

- **Register sizes:** `A`, `B`, `R0–R7`, `SP` are masked to 8 bits (`& 0xFF`); `PC` is 16 bits.
- **Zero flag** is updated whenever `A` is set; **Carry flag** is set by `ADD` and `SUBB`.
- **Stack** starts at `SP = 0x07` and grows upward to `0xFF`; overflow and underflow raise an error that is reported to the UI.
- **SJMP** is intentionally allowed to jump **only once per program run**, so demo programs cannot loop forever. It is re-armed on `LOAD` and `RESET`.
- **RUN safety limit:** execution stops after 1000 instructions.
- **Values** can be written as decimal, `0x1F`, `1Fh`, or hex with letters (for example `#FF`).

---

## How to run

From the repository root:

```powershell
javac -d out src\*.java src\week4\ipc\*.java src\week4\core\*.java src\week4\logging\*.java src\week4\ui\*.java
```

Start the processes in separate terminals, **in this order**:

```powershell
java -cp out week4.logging.LoggingProcess
java -cp out week4.core.CoreProcess
java -cp out week4.ui.UIProcess
```

Core waits for the UI on port `5051` and retries connecting to Logging on port `5052` for about 15 seconds, so starting Logging first avoids lost messages.

---

## Related documents

- `src/week4/ui/README.md` – UI process
- `src/README.md` – Week 1–3 source files
- `Docs/decisions/Week 4 Decisions.txt` – why TCP sockets replaced FIFO pipes
