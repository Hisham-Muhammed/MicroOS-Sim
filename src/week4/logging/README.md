# Week 4 Logging Module

## Overview

The `week4.logging` package implements the Logging Process used by the MicroOS Simulator in Week 4. It receives log messages from the Core Process over a TCP socket, validates each message, and appends valid entries to a log file.

This module uses Java's socket and file I/O APIs. The Core Process connects to the Logging Process using the port configured by `week4.ipc.FifoConfig.CORE_LOG_PORT` (currently `5052`).

## Files in This Directory

### `LogMessage.java`

**Purpose:** Represents one validated log message.

- Stores a log `level` and a `message`.
- Its constructor is private, so messages are created through `parse(String line)`.
- `parse` expects the input format `LEVEL|message`.
- The level is trimmed and converted to uppercase. Only `INFO` and `ERROR` are accepted.
- A null line, missing separator, empty level, unsupported level, or empty message returns `null`.
- `getLevel()` and `getMessage()` provide access to the two fields.
- The class is `final`, and both fields are `final`, so a parsed message cannot be changed after creation.

**Example valid inputs:**

```text
INFO|Simulator started
ERROR|Invalid instruction
```

### `Logger.java`

**Purpose:** Writes formatted log entries to a file.

- Implements `AutoCloseable`, allowing it to be used in a try-with-resources statement.
- The constructor accepts a `Path` for the log file.
- It creates the parent directories when necessary.
- It opens the file using UTF-8, creating it if it does not exist and appending to it if it already exists.
- `log(LogMessage message)` writes an entry containing the level, current date and time, and message. It then adds a newline and flushes the writer.
- `logRaw(String line)` parses a raw `LEVEL|message` line and logs it only if it is valid.
- `close()` closes the writer.
- Its writing and closing methods are `synchronized` to prevent concurrent calls from interleaving operations on the writer.

**Log output format:**

```text
[INFO] 2026-11-02 09:30:00 - Simulator started
[ERROR] 2026-11-02 09:30:01 - Invalid instruction
```

The timestamps in this example are illustrative; actual timestamps come from the system's local date and time.

### `LoggingProcess.java`

**Purpose:** Runs the logging service as a separate process and receives messages from the Core Process.

- `main(String[] args)` is the program entry point.
- Prints a startup message and the port on which it is waiting.
- Uses `FifoConfig.CORE_LOG_PORT` for the TCP server port.
- Uses `src/week4/logs/simulator.log` as the output log file, relative to the process's working directory.
- Opens a `ServerSocket` and waits for a Core Process connection.
- Reads incoming UTF-8 text one line at a time.
- Valid lines are parsed with `LogMessage.parse`, written with `Logger`, and reported in the console.
- Invalid lines are ignored and a warning is printed to standard error.
- When it receives `EXIT` (case-insensitive, with surrounding whitespace ignored), it writes a final `INFO` message and stops reading.
- Uses try-with-resources to close the socket, reader, server socket, and logger.
- If an I/O error occurs, it prints an error message and exits with status `1`.

**Message protocol:**

```text
INFO|Logging process started
ERROR|Instruction execution failed
EXIT
```

Each message must be sent as a separate line. `EXIT` is a control command, not a normal log entry.

### `Empty file`

This file is empty and contains no source code or configuration. It has no effect on the logging module and can be ignored unless the project uses it as a placeholder.

## How the Files Work Together

1. `LoggingProcess` starts a TCP server on the configured Core-to-Logging port.
2. The process waits for the Core Process to connect.
3. Each incoming line is checked by `LogMessage.parse`.
4. If valid, the resulting `LogMessage` is passed to `Logger`.
5. `Logger` appends a timestamped entry to `src/week4/logs/simulator.log`.
6. When `EXIT` is received or the connection closes, the process shuts down and closes its resources.

## Configuration and Related Files

- `src/week4/ipc/FifoConfig.java` defines `CORE_LOG_PORT` (`5052`) and other inter-process communication settings. The logging process imports this class rather than hard-coding the port.
- `src/week4/logs/simulator.log` is the log output path used by `LoggingProcess`. The file and its parent directory are created as needed.
- The module uses TCP sockets for inter-process communication; despite the `FifoConfig` class name and legacy FIFO constants, this logging workflow uses the TCP port.

## Running and Integration Notes

Run the Logging Process from the project root so the relative log path points to the expected location. The Core Process must connect to `127.0.0.1` on port `5052` and send newline-terminated messages in the format described above.

Compile and launch commands depend on the rest of the Week 4 project setup. Refer to the main project README and `src/week4/ui/README.md` for the project's Windows IPC workflow.

## Limitations

- Only `INFO` and `ERROR` levels are accepted.
- The server accepts one Core Process connection per run; after that connection ends, the process exits.
- Messages are processed line by line, so senders must terminate each message with a newline.
- The output file path is relative to the current working directory, so launching from a different directory may place the log file somewhere unexpected.
