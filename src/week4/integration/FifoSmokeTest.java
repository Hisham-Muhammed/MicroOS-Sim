package week4.integration;

import week4.ipc.FifoConfig;
import week4.ipc.FifoUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class FifoSmokeTest {
    private FifoSmokeTest() {}

    public static void main(String[] args) throws Exception {
        FifoUtil.ensureFifo(FifoConfig.UI_TO_CORE);
        FifoUtil.ensureFifo(FifoConfig.CORE_TO_UI);

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(FifoConfig.UI_TO_CORE), StandardCharsets.UTF_8));
             BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(FifoConfig.CORE_TO_UI), StandardCharsets.UTF_8))) {

            send(out, "LOAD");
            send(out, "PROGRAM|" + b64("MOV A,#10"));
            send(out, "PROGRAM|" + b64("END"));
            send(out, "END_LOAD");
            assertResponse(in, "OK|");

            send(out, "STEP");
            assertResponse(in, "OK|STEP|");

            send(out, "SHOW");
            assertResponse(in, "OK|SHOW|");

            send(out, "EXIT");
            assertResponse(in, "OK|EXIT|");
        }

        System.out.println("PASS: UI->Core FIFO and Core->UI FIFO smoke test.");
    }

    private static String b64(String text) {
        return Base64.getEncoder().encodeToString(
                text.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(BufferedWriter out, String line) throws IOException {
        out.write(line);
        out.newLine();
        out.flush();
    }

    private static void assertResponse(BufferedReader in, String prefix) throws IOException {
        String response = in.readLine();
        if (response == null || !response.startsWith(prefix)) {
            throw new IOException("Unexpected Core response: " + response);
        }
        System.out.println("PASS: " + response.substring(0, Math.min(response.length(), 80)));
    }
}
