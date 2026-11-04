; Hamming (7,4) parity generation + error detection
; Data bits: D1=1, D2=0, D3=1, D4=1
; R0=D1, R1=D2, R2=D3, R3=D4
; P1=R4, P2=R5, P4=R6
; This demo intentionally flips D1 (Hamming position 3).
; Syndrome output: S1=R7, S2=R4, S4=R5
; ALERT R7,R4,R5 displays the detected error position.

MOV R0,#01H
MOV R1,#00H
MOV R2,#01H
MOV R3,#01H

; P1 = D1 XOR D2 XOR D4
MOV A,R0
XRL A,R1
XRL A,R3
MOV R4,A

; P2 = D1 XOR D3 XOR D4
MOV A,R0
XRL A,R2
XRL A,R3
MOV R5,A

; P4 = D2 XOR D3 XOR D4
MOV A,R1
XRL A,R2
XRL A,R3
MOV R6,A

; Inject one-bit error at Hamming position 3 (D1)
MOV A,R0
XRL A,#01H
MOV R0,A

; S1 = P1 XOR D1 XOR D2 XOR D4
MOV A,R4
XRL A,R0
XRL A,R1
XRL A,R3
MOV R7,A

; S2 = P2 XOR D1 XOR D3 XOR D4
MOV A,R5
XRL A,R0
XRL A,R2
XRL A,R3
MOV R4,A

; S4 = P4 XOR D2 XOR D3 XOR D4
MOV A,R6
XRL A,R1
XRL A,R2
XRL A,R3
MOV R5,A

; Report syndrome S4 S2 S1
ALERT R7,R4,R5
END
