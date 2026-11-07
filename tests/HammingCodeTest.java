import week4.hamming.HammingCode;

public class HammingCodeTest {
    public static void main(String[] args) {
        check("1011", 3, 7, "0110011");
        check("10101", 4, 9, "001101011");
        check("10110011", 4, 12, "101101100011");
        check("10110011001", 4, 15, "011001110011001");
        check("101100110011", 5, 17, "11100111001100111");
        System.out.println("HammingCodeTest: all tests passed.");
    }

    private static void check(String data, int parity, int total, String expected) {
        HammingCode.Result result = HammingCode.generate(data);
        if (result.parityBits != parity || result.totalBits != total
                || !result.codeword.equals(expected)) {
            throw new AssertionError("Unexpected Hamming result for " + data);
        }
    }
}
