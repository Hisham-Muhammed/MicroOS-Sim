# Week 4 – MicroOS-Sim

Week 4 uses separate UI, Core, and Logging processes connected through Java TCP sockets.

## Run in Windows / VS Code

Open PowerShell in the project folder (`MicroOS-Sim-week-4`).

### 1. Compile

```powershell
javac -d out src\*.java src\week4\ipc\*.java src\week4\core\*.java src\week4\logging\*.java src\week4\ui\*.java
```

### 2. Start Logging Process

Run in the first terminal:

```powershell
java -cp out week4.logging.LoggingProcess
```

### 3. Start Core Process

Run in a second terminal:

```powershell
java -cp out week4.core.CoreProcess
```

### 4. Start UI Process

Run in a third terminal:

```powershell
java -cp out week4.ui.UIProcess
```

Use the UI to run commands such as `LOAD`, `STEP`, `RUN`, `RESET`, `SHOW`, and `EXIT`. Start the processes in the order shown above.
