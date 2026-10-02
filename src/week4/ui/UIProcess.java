package week4.ui;

import week4.ipc.FifoConfig;
import week4.ipc.FifoUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class UIProcess {
    private static final Color DARK_BG = new Color(12, 14, 22);
    private static final Color DARK_PANEL = new Color(24, 27, 38);
    private static final Color DARK_INPUT = new Color(15, 18, 27);
    private static final Color DARK_TEXT = new Color(235, 238, 247);
    private static final Color DARK_MUTED = new Color(155, 163, 181);
    private static final Color LIGHT_BG = new Color(242, 244, 249);
    private static final Color LIGHT_PANEL = Color.WHITE;
    private static final Color LIGHT_INPUT = new Color(247, 248, 252);
    private static final Color LIGHT_TEXT = new Color(30, 34, 45);
    private static final Color LIGHT_MUTED = new Color(100, 108, 125);
    private static final Color ACCENT = new Color(115, 95, 255);
    private static final Color GREEN = new Color(70, 205, 135);

    private JFrame frame;
    private JTextArea sourceEditor;
    private JTextArea traceArea;
    private JTextArea cpuArea;
    private JLabel statusLabel;
    private JLabel instructionLabel;
    private JLabel pcLabel;
    private JLabel titleLabel;
    private boolean darkMode = true;
    private BufferedWriter coreWriter;
    private BufferedReader coreReader;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UIProcess().start());
    }

    private void start() {
        try {
            FifoUtil.ensureFifo(FifoConfig.UI_TO_CORE);
            FifoUtil.ensureFifo(FifoConfig.CORE_TO_UI);
            connectToCore();
            buildUI();
            request("SHOW");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to Core Process.\nStart CoreProcess first.\n\n" + e.getMessage(),
                    "Week 4 IPC Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void connectToCore() throws IOException {
        coreWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FifoConfig.UI_TO_CORE), StandardCharsets.UTF_8));
        coreReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(FifoConfig.CORE_TO_UI), StandardCharsets.UTF_8));
    }

    private void buildUI() {
        frame = new JFrame("STC89C52 Microcontroller Simulator • Week 4");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setMinimumSize(new Dimension(760, 600));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        frame.setContentPane(root);

        JPanel header = createHeader();
        sourceEditor = createEditor();
        traceArea = createOutputArea();
        cpuArea = createOutputArea();

        sourceEditor.setText(
                "MOV A,#10\n" +
                "MOV R0,#20\n" +
                "ADD A,R0\n" +
                "PUSH A\n" +
                "POP R1\n" +
                "ENQUEUE #10\n" +
                "ENQUEUE #20\n" +
                "DEQUEUE R2\n" +
                "END"
        );

        JPanel center = new JPanel(new GridLayout(1, 3, 12, 12));
        center.setOpaque(false);
        center.add(createPanel("ASSEMBLY PROGRAM  •  WEEK 2 + WEEK 3", new JScrollPane(sourceEditor)));
        center.add(createPanel("EXECUTION TRACE", new JScrollPane(traceArea)));
        center.add(createPanel("CPU STATE", new JScrollPane(cpuArea)));

        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(createBottomBar(), BorderLayout.SOUTH);

        applyTheme();
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                try { request("EXIT"); } catch (Exception ignored) {}
                close();
                frame.dispose();
                System.exit(0);
            }
        });
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel panel = new GradientPanel();
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBorder(new EmptyBorder(13, 15, 13, 15));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        titleLabel = new JLabel("STC89C52  MICROCONTROLLER  SIMULATOR");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel subtitle = new JLabel("8051 Architecture  •  Week 4 Multi-Process IPC");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(new Color(220, 225, 240));

        left.add(titleLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        right.setOpaque(false);

        JLabel chip = new JLabel("●  UI PROCESS");
        chip.setFont(new Font("SansSerif", Font.BOLD, 10));
        chip.setForeground(Color.WHITE);
        chip.setBorder(new EmptyBorder(6, 8, 6, 8));

        JButton mode = new JButton("☀ / ☾");
        styleButton(mode, new Color(65, 70, 88));
        mode.addActionListener(e -> { darkMode = !darkMode; applyTheme(); });

        right.add(chip);
        right.add(mode);
        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel createPanel(String title, JComponent component) {
        JPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));

        JScrollPane scroll = (JScrollPane) component;
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getHorizontalScrollBar().setUnitIncrement(14);

        panel.add(label, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JTextArea createEditor() {
        JTextArea area = new JTextArea();
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setLineWrap(false);
        area.setTabSize(4);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        return area;
    }

    private JTextArea createOutputArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        area.setLineWrap(false);
        area.setEditable(false);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        return area;
    }

    private JPanel createBottomBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 6));
        panel.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);

        JButton load = createActionButton("LOAD", ACCENT);
        JButton reset = createActionButton("RESET", new Color(70, 76, 94));
        JButton step = createActionButton("STEP", new Color(70, 76, 94));
        JButton run = createActionButton("RUN ▶", new Color(45, 155, 105));

        buttons.add(load); buttons.add(reset); buttons.add(step); buttons.add(run);
        load.addActionListener(e -> loadProgram());
        reset.addActionListener(e -> request("RESET"));
        step.addActionListener(e -> request("STEP"));
        run.addActionListener(e -> request("RUN"));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        instructionLabel = new JLabel("Instruction: -");
        statusLabel = new JLabel("● READY");
        info.add(instructionLabel); info.add(statusLabel);

        pcLabel = new JLabel("PC: 0000");
        pcLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));

        panel.add(buttons, BorderLayout.WEST);
        panel.add(info, BorderLayout.CENTER);
        panel.add(pcLabel, BorderLayout.EAST);
        return panel;
    }

    private JButton createActionButton(String text, Color base) {
        JButton button = new JButton(text);
        styleButton(button, base);
        return button;
    }

    private void loadProgram() {
        String[] lines = sourceEditor.getText().split("\\R");
        try {
            coreWriter.write("LOAD");
            coreWriter.newLine();
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    String encoded = Base64.getEncoder().encodeToString(
                            line.trim().getBytes(StandardCharsets.UTF_8));
                    coreWriter.write("PROGRAM|" + encoded);
                    coreWriter.newLine();
                }
            }
            coreWriter.write("END_LOAD");
            coreWriter.newLine();
            coreWriter.flush();
            readAndDisplayResponse();
        } catch (IOException e) {
            showError("LOAD failed: " + e.getMessage());
        }
    }

    private void request(String command) {
        if (coreWriter == null) return;
        try {
            coreWriter.write(command);
            coreWriter.newLine();
            coreWriter.flush();
            readAndDisplayResponse();
        } catch (IOException e) {
            showError(command + " failed: " + e.getMessage());
        }
    }

    private void readAndDisplayResponse() throws IOException {
        String response = coreReader.readLine();
        if (response == null) {
            throw new EOFException("Core closed the response FIFO.");
        }

        response = response.replace("\\n", "\n");
        if (response.startsWith("OK|SHOW|")) {
            String encoded = response.split("\\|", 4)[2];
            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            displayStatus(decoded);
        } else if (response.startsWith("OK|")) {
            String[] parts = response.split("\\|", 4);
            if (parts.length >= 3) {
                displayStatus(parts[2]);
            }
            if (response.contains("|TIME_NS=")) {
                statusLabel.setText("● " + response.substring(response.lastIndexOf("TIME_NS=")));
            }
        } else if (response.startsWith("ERROR|")) {
            statusLabel.setText("● ERROR");
            traceArea.setText(response.substring(6));
        }
    }

    private void displayStatus(String status) {
        String[] sections = status.split("\nTRACE\n", 2);
        String state = sections[0];
        String trace = sections.length > 1 ? sections[1] : "";

        traceArea.setText(trace);
        cpuArea.setText(state);

        for (String line : state.split("\n")) {
            if (line.startsWith("STATUS=")) statusLabel.setText("● " + line.substring(7));
            if (line.startsWith("PC : ")) pcLabel.setText("PC: " + line.substring(5));
        }

        instructionLabel.setText("Instruction: updated by Core Process");
    }

    private void showError(String message) {
        statusLabel.setText("● IPC ERROR");
        traceArea.setText(message);
    }

    private void close() {
        try { if (coreWriter != null) coreWriter.close(); } catch (IOException ignored) {}
        try { if (coreReader != null) coreReader.close(); } catch (IOException ignored) {}
    }

    private void styleButton(JButton button, Color base) {
        button.setBackground(base);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    private void applyTheme() {
        Color bg = darkMode ? DARK_BG : LIGHT_BG;
        Color panel = darkMode ? DARK_PANEL : LIGHT_PANEL;
        Color input = darkMode ? DARK_INPUT : LIGHT_INPUT;
        Color text = darkMode ? DARK_TEXT : LIGHT_TEXT;
        Color muted = darkMode ? DARK_MUTED : LIGHT_MUTED;

        if (frame == null) return;
        frame.getContentPane().setBackground(bg);
        if (sourceEditor != null) {
            sourceEditor.setBackground(input);
            sourceEditor.setForeground(text);
            sourceEditor.setCaretColor(text);
        }
        if (traceArea != null) {
            traceArea.setBackground(input);
            traceArea.setForeground(text);
        }
        if (cpuArea != null) {
            cpuArea.setBackground(input);
            cpuArea.setForeground(text);
        }
        if (statusLabel != null) statusLabel.setForeground(GREEN);
        if (instructionLabel != null) instructionLabel.setForeground(muted);
        if (pcLabel != null) pcLabel.setForeground(text);
        if (titleLabel != null) titleLabel.setForeground(Color.WHITE);

        for (Component c : frame.getContentPane().getComponents()) {
            if (c instanceof JPanel) c.setBackground(bg);
        }
        frame.repaint();
    }

    private static final class RoundedPanel extends JPanel {
        RoundedPanel() { setOpaque(true); }
    }

    private static final class GradientPanel extends JPanel {
        GradientPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, new Color(44, 35, 90),
                    getWidth(), getHeight(), new Color(78, 52, 135)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
