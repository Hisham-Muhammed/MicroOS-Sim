# Week 4 — Multi-Process Simulator

This folder contains only the new Week 4 process/IPC implementation. The original Week 1–3 Java files remain directly under `src/`.

The Week 4 brief requires three independent processes and IPC between them, followed by standalone vs. multi-process performance comparison.

## Architecture

```text
                         POSIX FIFO Named Pipes

  ┌───────────────┐
  │   UI Process  │
  │ week4.ui      │
  └───────┬───────┘
          │ /tmp/microos_ui_to_core.fifo
          ▼
  ┌───────────────┐
  │  Core Process │
  │ week4.core    │
  └───────┬───────┘
          │ uses existing Week 1–3 Simulator
          ▼
  CPU / Memory / Stack / Queue
          │
          │ /tmp/microos_core_to_log.fifo
          ▼
  ┌───────────────┐
  │Logging Process│
  │week4.logging  │
  └───────┬───────┘
          ▼
  src/week4/logs/simulator.log

  Core response:
  /tmp/microos_core_to_ui.fifo
          │
          ▼
       UI Process
```

## Files

- `ipc/FifoConfig.java` — exact shared FIFO names.
- `ipc/FifoUtil.java` — creates/checks POSIX FIFOs using `mkfifo`.
- `ui/UIProcess.java` — Week 3-style Swing UI. It only communicates with the Core through FIFOs; it does not import CPU, Memory, Stack, Queue, Simulator or Logger.
- `core/CoreProcess.java` — separate Core process. It receives commands through FIFO and delegates execution to the existing Week 1–3 `Simulator`.
- `logging/LogMessage.java` — validates `INFO|message` and `ERROR|message`.
- `logging/Logger.java` — adds timestamps and appends to `src/week4/logs/simulator.log`.
- `logging/LoggingProcess.java` — separate process that reads the Core→Logger FIFO.
- `integration/Week4Launcher.java` — prints the three-process startup order.
- `logs/simulator.log` — generated at runtime; not required to be committed if the team prefers a clean repository.

## Important design point

The existing Week 1–3 classes are in the Java default package. Java named packages cannot directly import classes from the default package. Therefore the Week 4 Core uses a small reflection adapter **inside `CoreProcess.java`** to instantiate and call the real existing `Simulator`.

This is not a duplicate simulator. The Week 1–3 `Simulator`, `CPU`, `Memory`, `Stack`, `Queue`, `Instruction`, and `InstructionSet` remain the actual execution implementation.

## FIFO protocol

### UI → Core

Single command:

```text
STEP
RUN
RESET
SHOW
EXIT
```

For loading:

```text
LOAD
PROGRAM|<Base64 encoded instruction>
PROGRAM|<Base64 encoded instruction>
END_LOAD
```

Base64 is used only to keep each FIFO message on one line.

### Core → UI

Examples:

```text
OK|Program loaded|<time_ns>
OK|STEP|STATUS=Running\nCPU STATE...\nTRACE...|TIME_NS=<n>
OK|RUN|STATUS=Program terminated\n...|TIME_NS=<n>
ERROR|Invalid command: XYZ
```

### Core → Logging Process

Examples:

```text
INFO|Program loaded (9 instructions)
INFO|Instruction executed: MOV A,#10
INFO|PUSH executed
INFO|POP executed
INFO|ENQUEUE executed
INFO|DEQUEUE executed
ERROR|Invalid command: XYZ
ERROR|Execution error: Stack underflow
```

The logger converts these into:

```text
[INFO] 2026-10-02 12:00:00 - PUSH executed
[ERROR] 2026-10-02 12:00:01 - Stack underflow
```

## Compile

Run from the project root:

```bash
rm -rf out
mkdir out
javac -d out $(find src -name "*.java")
```

This compiles the unchanged Week 1–3 classes and the new Week 4 packages together.

## Run

Use three terminals on Linux, WSL or Termux.

### Terminal 1 — Logging

```bash
java -cp out week4.logging.LoggingProcess
```

### Terminal 2 — Core

```bash
java -cp out week4.core.CoreProcess
```

### Terminal 3 — UI

```bash
java -cp out week4.ui.UIProcess
```

Start Logging first, then Core, then UI.

## UI behaviour

The UI keeps the Week 3 style:

- assembly editor
- execution trace panel
- CPU state panel
- LOAD
- RESET
- STEP
- RUN
- light/dark mode

The important Week 4 difference is that the UI no longer owns or directly executes the simulator. Button actions send FIFO commands to the Core process.

## IPC test cases

### Test 1 — UI → Core

1. Start Logging.
2. Start Core.
3. Start UI.
4. Enter/load:

```text
MOV A,#10
PUSH A
POP R1
ENQUEUE #20
DEQUEUE R2
END
```

5. Click `LOAD`.
6. Click `STEP`.
7. Click `RUN`.

Expected: the UI receives `OK|...` responses from `/tmp/microos_core_to_ui.fifo` and displays updated CPU/Stack/Queue state.

### Test 2 — Core → UI

Click `SHOW`.

Expected: Core reads its actual Week 1–3 simulator state and sends it through:

```text
/tmp/microos_core_to_ui.fifo
```

The UI displays the returned state.

### Test 3 — Core → Logger

After `STEP`/`RUN`, inspect:

```bash
cat src/week4/logs/simulator.log
```

Expected entries include instruction/execution messages and timestamps.

### Test 4 — Stack operation

Use:

```text
MOV A,#10
PUSH A
POP R1
END
```

Expected log entries include:

```text
[INFO] ... - PUSH executed
[INFO] ... - POP executed
```

### Test 5 — Queue operation

Use:

```text
ENQUEUE #10
ENQUEUE #20
DEQUEUE R0
DEQUEUE R1
END
```

Expected FIFO order:

```text
R0 = 0A
R1 = 14
```

and the log contains ENQUEUE/DEQUEUE entries.

### Test 6 — Invalid instruction

Try loading:

```text
XYZ A,#10
```

Expected: Core rejects the program load and sends an error; the Logging Process records an `ERROR` entry.

### Test 7 — Stack underflow

Try:

```text
POP A
END
```

Expected: Core returns an execution error and Logging Process records the error.

### Test 8 — Invalid command

Send `XYZ` to the Core FIFO.

Expected:

```text
ERROR|Invalid command: XYZ
```

and an `ERROR` log entry.

## Independent Logging test

Create the FIFO if required:

```bash
rm -f /tmp/microos_core_to_log.fifo
mkfifo /tmp/microos_core_to_log.fifo
```

Start:

```bash
java -cp out week4.logging.LoggingProcess
```

In another terminal:

```bash
printf 'INFO|Independent logging test\nERROR|Independent error test\nEXIT\n' > /tmp/microos_core_to_log.fifo
```

Then:

```bash
cat src/week4/logs/simulator.log
```

This tests the Logging Process independently before Team Leader integration.

## Performance measurement

The Core response contains `TIME_NS=<value>` for `LOAD`, `STEP`, `RUN`, `RESET` and `SHOW`.

For IPC overhead, measure:

1. timestamp immediately before the UI writes a command;
2. timestamp immediately after the UI receives the Core response;
3. compare this round-trip time with the Core's `TIME_NS`.

Approximation:

```text
IPC round-trip overhead
≈ UI round-trip time - Core execution time
```

For better results, repeat each operation many times and report average, minimum and maximum.

### CPU usage

On Linux/Termux, use tools such as:

```bash
top
```

or:

```bash
/usr/bin/time -v java -cp out week4.core.CoreProcess
```

Record CPU percentage separately for:

- standalone simulator
- UI process
- Core process
- Logging process

### Memory usage

Use:

```bash
/usr/bin/time -v ...
```

and record maximum resident set size where available.

### Execution-time comparison

Run the same instruction program:

```text
MOV A,#10
MOV R0,#20
ADD A,R0
PUSH A
POP R1
ENQUEUE #10
ENQUEUE #20
DEQUEUE R2
END
```

Compare:

| Metric | Standalone | Multi-process |
|---|---:|---:|
| Total execution time | measure | measure |
| Core execution time | — | measure |
| IPC round-trip time | — | measure |
| CPU usage | measure | measure |
| Memory usage | measure | measure |

Do not invent benchmark numbers; fill this table with measurements from the actual machine.

## Why POSIX FIFO Named Pipes?

The team selected POSIX FIFO Named Pipes because the Week 4 architecture needs simple one-way message channels between separate processes. A FIFO has a filesystem path, so independently started Java processes can open the same named pipe without sharing Java objects or memory.

This design gives three clear channels:

```text
UI → Core       /tmp/microos_ui_to_core.fifo
Core → UI       /tmp/microos_core_to_ui.fifo
Core → Logger   /tmp/microos_core_to_log.fifo
```

It also keeps CPU, Memory, Stack and Queue inside the Core process as required by the architecture.

## Integration rule

Do not move the old files.

Do not create `src/week3/`.

Do not create duplicate CPU, Memory, Stack, Queue or Simulator classes.

The Team Leader only needs to integrate the three process entry points:

```text
week4.logging.LoggingProcess
week4.core.CoreProcess
week4.ui.UIProcess
```

The Week 4 brief explicitly assigns UI, Core, Logging and integration as separate responsibilities.

## Week 4 Core File Separation

Week 4 Core has its own package and source files under `src/week4/core/`:

- `CoreProcess.java` — Core process and FIFO communication.
- `CoreSimulator.java` — Week 4 Core-side simulator logic.
- `CoreCPU.java` — Core-side CPU/register implementation.
- `CoreMemory.java` — Core-side memory implementation.
- `CoreStack.java` — Core-side stack implementation.
- `CoreQueue.java` — Core-side FIFO queue implementation.
- `CoreInstruction.java` — Core-side instruction model.
- `CoreInstructionSet.java` — Core-side instruction definitions.

These files are intentionally separate from the original Week 3 files in `src/`. The Week 3 files are not moved or renamed. The Week 4 Core files preserve the Week 3 instruction behavior while giving the Week 4 Core member an independently owned implementation.
