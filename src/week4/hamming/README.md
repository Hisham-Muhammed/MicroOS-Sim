# Hamming Code – Variable Data-Bit Selection

The Hamming Code panel is available in the Week 5 UI. The user can select a data-bit size from the dropdown and generate a codeword.

## Available data-bit sizes

| Data bits | Parity bits | Total codeword bits |
|---:|---:|---:|
| 4 | 3 | 7 |
| 5 | 4 | 9 |
| 8 | 4 | 12 |
| 11 | 4 | 15 |
| 12 | 5 | 17 |

The number of parity bits is calculated automatically using:

```text
2^r >= m + r + 1
```

- `m` = number of data bits
- `r` = number of parity bits

The implementation uses even parity. Parity positions are powers of two: 1, 2, 4, 8, 16, and so on.

## How to use

1. Start Logging Process, Core Process, and UI Process.
2. In the Hamming Code panel, choose the data-bit size.
3. Enter exactly that many binary digits (only `0` and `1`).
4. Click **GENERATE CODE**.
5. The UI sends the selected size and input bits to Core over TCP.
6. Core calculates the parity bits and codeword, then returns the result to UI.
7. The request and calculation are recorded in the timestamped session log.

## Existing Hamming (7,4) assembly examples

- `Hamming74Demo.asm` – single-bit error demonstration.
- `Hamming74NoError.asm` – no-error demonstration.

These two assembly examples remain Hamming (7,4) examples. The new dropdown-based generator supports the listed variable data sizes; it does not silently rewrite the existing assembly programs.

**Note:** The current generic generator creates an even-parity Hamming codeword. Automatic correction of an altered received codeword is not part of this generator yet.

**Project:** MicroOS-Sim  
**Team:** Onji Byte
