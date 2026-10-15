# Week 4 UI Process — Java TCP Socket IPC

The UI runs as a separate Java process and communicates with `week4.core.CoreProcess` using Java TCP sockets. It does not directly call CPU, Memory, Stack, Queue, Simulator, or Logger classes.

## Windows / VS Code

Open PowerShell in the repository root (`MicroOS-Sim-week-4`) and compile:

```powershell
javac -d out src\*.java src\week4\ipc\*.java src\week4\core\*.java src\week4\logging\*.java src\week4\ui\*.java
```

Run each process in a separate terminal, in this order:

```powershell
java -cp out week4.logging.LoggingProcess
```

```powershell
java -cp out week4.core.CoreProcess
```

```powershell
java -cp out week4.ui.UIProcess
```

Core listens on `127.0.0.1:5051` for the UI. Logging listens on `127.0.0.1:5052` for messages from Core. Allow Java through Windows Firewall for local loopback if prompted.

Commands supported by the UI include LOAD, STEP, RUN, RESET, SHOW, and EXIT. Existing Week 1–3 source files remain in `src/`; Week 4 code remains under `src/week4/`.
