Week 4 - UI Process

Overview
The UI Process is a separate process in the MicroOS-Sim project. It communicates with the Core Process using POSIX FIFO Named Pipes.

FIFO Communication
 UI to Core: `/tmp/microos_ui_to_core.fifo`
 Core to UI: `/tmp/microos_core_to_ui.fifo`

Commands
The UI supports the following commands:
  LOAD: Load a program.
  STEP: Execute one instruction.
  RUN: Execute the program.
  RESET: Reset the simulator.
  SHOW: Display the current status.
  EXIT: Exit the UI process.

 Working
1. The user enters a command in the UI.
2. The UI sends the command to the Core through the first FIFO.
3. The Core processes the command.
4. The Core sends a response through the second FIFO.
5. The UI displays the response.

 Technologies
 Java
 POSIX FIFO Named Pipes
 BufferedReader
 BufferedWriter

Testing
The UI can be tested independently using a temporary FIFO test peer before integration with the actual Core Process.

 Architecture
UI Process-> Core Process -> CPU / Memory / Stack / Queue -> Logging Process

The UI and Core communicate through two FIFO named pipes.
