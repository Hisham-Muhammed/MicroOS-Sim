# Hamming (7,4) Error Detection

This folder contains the assembly programs for Hamming (7,4) error detection in MicroOS-Sim.

## Files

- `Hamming74Demo.asm` – Tests error detection by changing one bit.
- `Hamming74NoError.asm` – Tests the program without any error.

## About Hamming (7,4)

Hamming (7,4) uses 4 data bits and 3 parity bits.

**Bit positions:**

| Position | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Bit | P1 | P2 | D1 | P4 | D2 | D3 | D4 |

## Parity Calculation

```text
P1 = D1 XOR D2 XOR D4
P2 = D1 XOR D3 XOR D4
P4 = D2 XOR D3 XOR D4
```

## How It Works

1. Load 4 data bits into registers.
2. Calculate 3 parity bits using XOR.
3. Generate the Hamming code.
4. Introduce an error in the demo program.
5. Calculate syndrome bits (S1, S2, S4).
6. Find the error position.

## Expected Output

| Program | Syndrome | Result |
| --- | --- | --- |
| Hamming74Demo.asm | 011 | Error at position 3 |
| Hamming74NoError.asm | 000 | No error |

## Instructions Used

- `MOV` – Move data.
- `XRL` – Perform XOR operation.
- `ALERT` – Show the error result.
- `END` – End the program.

## Running

1. Start the MicroOS-Sim processes.
2. Open the UI.
3. Load either assembly file.
4. Click RUN or use STEP.
5. Check the result in the simulator.

**Note:** This program can detect and find a single-bit error. It does not correct the error automatically.

**Project:** MicroOS-Sim  
**Team:** Onji Byte
