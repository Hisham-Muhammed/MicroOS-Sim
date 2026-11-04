## Week 4 – Hamming (7,4) Error Detection

As part of Week 4, Hamming (7,4) Error Detection was implemented in MicroOS-Sim. This update demonstrates how a single-bit error can be detected and its position can be identified using parity bits and syndrome calculation.

### 1. About Hamming (7,4)

Hamming (7,4) is an error detection technique which uses:

- 4 data bits
- 3 parity bits
- 7 bits in the final codeword

The parity bits are used to check whether an error has occurred during data transmission.

### 2. Bit Position Arrangement

The seven bits are arranged as follows:

| Position | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| Bit | P1 | P2 | D1 | P4 | D2 | D3 | D4 |

Here, P1, P2 and P4 are parity bits, while D1, D2, D3 and D4 are data bits.

### 3. Parity Calculation

The project uses XOR operation to calculate the three parity bits.

```text
P1 = D1 XOR D2 XOR D4
P2 = D1 XOR D3 XOR D4
P4 = D2 XOR D3 XOR D4
```

Even parity is used for the calculation.

### 4. Syndrome Calculation

After generating the Hamming code, the received bits are checked again to calculate the syndrome.

The syndrome bits are:

- S1 – Checks positions 1, 3, 5 and 7.
- S2 – Checks positions 2, 3, 6 and 7.
- S4 – Checks positions 4, 5, 6 and 7.

The syndrome is read in the order **S4 S2 S1**.

| Syndrome | Meaning |
|---|---|
| 000 | No error detected |
| Non-zero value | Indicates the error position |

The binary syndrome value gives the position of the detected single-bit error.

### 5. Implementation in MicroOS-Sim

The Hamming algorithm is implemented using assembly programs and the existing simulator.

The following instructions are used:

- `MOV` – Transfers data between registers.
- `XRL` – Performs XOR operations.
- `ALERT` – Displays the error detection result.
- `END` – Stops program execution.

Java instruction support was updated to handle the `XRL` and `ALERT` instructions. The ALERT instruction displays the result through the simulator UI.

### 6. Test Programs

Two assembly programs are included in:

```text
src/week4/hamming/
```

**Hamming74Demo.asm**

- Loads four data bits into registers.
- Calculates the parity bits.
- Introduces a single-bit error at position 3.
- Calculates the syndrome.
- Identifies and displays the error position.

**Hamming74NoError.asm**

- Performs the same Hamming calculation.
- Does not introduce any error.
- Checks whether the syndrome is zero.

### 7. Expected Test Results

| Test Program | Syndrome | Expected Result |
|---|---|---|
| Hamming74Demo.asm | 011 | Error detected at position 3 |
| Hamming74NoError.asm | 000 | No error detected |

For the error test, the syndrome `011` represents decimal position 3. The simulator displays:

```text
Error detected at Hamming bit position 3.
```

For the no-error test, the simulator displays:

```text
No error detected (syndrome 000).
```

### 8. Running the Hamming Tests

1. Start the MicroOS-Sim processes.
2. Open the simulator UI.
3. Load `Hamming74Demo.asm` or `Hamming74NoError.asm`.
4. Use RUN to execute the complete program or STEP to execute instructions one by one.
5. Check the execution trace and error alert displayed by the simulator.

### 9. Current Implementation

The current implementation can detect and identify a single-bit error using syndrome calculation. Automatic error correction is not implemented.

This update demonstrates the use of assembly instructions, register operations, parity calculation and error detection within the MicroOS-Sim project.
