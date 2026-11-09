package week4.hamming;

/** Generic Hamming code generator using even parity. */
public final class HammingCode {
    private HammingCode() {}

    public static int parityBitCount(int dataBitCount) {
        if (dataBitCount <= 0 || dataBitCount > 26) {
            throw new IllegalArgumentException("Choose between 1 and 26 data bits.");
        }
        int r = 0;
        while ((1L << r) < dataBitCount + r + 1L) r++;
        return r;
    }

    public static Result generate(String dataBits) {
        if (dataBits == null || !dataBits.matches("[01]+")) {
            throw new IllegalArgumentException("Enter data using only 0 and 1.");
        }
        int m = dataBits.length();
        int r = parityBitCount(m);
        int n = m + r;
        int[] code = new int[n + 1];
        int dataIndex = 0;

        for (int position = 1; position <= n; position++) {
            if ((position & (position - 1)) != 0) {
                code[position] = dataBits.charAt(dataIndex++) - '0';
            }
        }

        for (int parityPosition = 1; parityPosition <= n; parityPosition <<= 1) {
            int parity = 0;
            for (int position = 1; position <= n; position++) {
                if ((position & parityPosition) != 0 && position != parityPosition) {
                    parity ^= code[position];
                }
            }
            code[parityPosition] = parity;
        }

        StringBuilder word = new StringBuilder();
        for (int position = 1; position <= n; position++) word.append(code[position]);
        StringBuilder parityPositions = new StringBuilder();
        for (int position = 1; position <= n; position <<= 1) {
            if (parityPositions.length() > 0) parityPositions.append(", ");
            parityPositions.append(position);
        }
        return new Result(dataBits, word.toString(), r, n, parityPositions.toString());
    }

    /**
     * Generates simulator assembly. errorPosition is zero for no error,
     * otherwise it is the one-based Hamming codeword bit position to flip.
     */
    public static String generateAssembly(String dataBits, int errorPosition) {
        Result result = generate(dataBits);
        int n = result.totalBits;
        if (errorPosition < 0 || errorPosition > n) {
            throw new IllegalArgumentException("Error position must be between 0 and " + n + ".");
        }

        int[] received = new int[n + 1];
        for (int i = 1; i <= n; i++) received[i] = result.codeword.charAt(i - 1) - '0';

        StringBuilder asm = new StringBuilder();
        asm.append("; HAMMING CODE GENERATION\n")
           .append("; Data bits: ").append(dataBits).append('\n')
           .append("; Generated codeword: ").append(result.codeword).append('\n')
           .append("; FINDING PARITY BITS\n");

        for (int p = 1, reg = 0; p <= n; p <<= 1, reg++) {
            asm.append("; Finding parity P").append(p).append('\n')
               .append("MOV A,#00H\n");
            for (int i = 1; i <= n; i++) {
                if ((i & p) != 0 && i != p && received[i] == 1) {
                    asm.append("XRL A,#01H\n");
                }
            }
            asm.append("MOV R").append(reg).append(",A\n");
        }

        asm.append("; ERROR INJECTION\n");
        if (errorPosition == 0) {
            asm.append("; No error injected\n");
        } else {
            received[errorPosition] ^= 1;
            asm.append("; Injecting one-bit error at position ")
               .append(errorPosition).append('\n');
        }
        asm.append("; RECEIVED CODEWORD: ");
        for (int i = 1; i <= n; i++) asm.append(received[i]);
        asm.append('\n').append("; FINDING PARITY AGAIN - SYNDROME CHECK\n");

        int syndromeBits = 0;
        for (int p = 1, reg = 0; p <= n; p <<= 1, reg++) {
            asm.append("; Rechecking parity group P").append(p).append('\n')
               .append("MOV A,#00H\n");
            for (int i = 1; i <= n; i++) {
                if ((i & p) != 0 && received[i] == 1) {
                    asm.append("XRL A,#01H\n");
                }
            }
            asm.append("MOV R").append(reg).append(",A\n");
            syndromeBits++;
        }

        asm.append("; ALERT uses syndrome bits S1,S2,S4,S8,S16\n")
           .append("ALERT ");
        for (int i = 0; i < syndromeBits; i++) {
            if (i > 0) asm.append(',');
            asm.append('R').append(i);
        }
        asm.append("\nEND\n");
        return asm.toString();
    }

    public static final class Result {
        public final String dataBits;
        public final String codeword;
        public final int parityBits;
        public final int totalBits;
        public final String parityPositions;

        private Result(String dataBits, String codeword, int parityBits,
                       int totalBits, String parityPositions) {
            this.dataBits = dataBits;
            this.codeword = codeword;
            this.parityBits = parityBits;
            this.totalBits = totalBits;
            this.parityPositions = parityPositions;
        }

        @Override public String toString() {
            return "Data bits: " + dataBits
                    + "\nParity bits: " + parityBits
                    + "\nTotal codeword bits: " + totalBits
                    + "\nParity positions: " + parityPositions
                    + "\nGenerated Hamming code: " + codeword;
        }
    }
}
