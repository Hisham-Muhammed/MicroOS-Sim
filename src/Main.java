import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final Simulator simulator = new Simulator();

    private JTextArea sourceEditor;
    private JTextArea traceArea;
    private JTextArea cpuArea;

    private JLabel statusLabel;
    private JLabel instructionLabel;
    private JLabel pcLabel;
    private JLabel titleLabel;

    private boolean programLoaded = false;
    private int stepNumber = 1;
    private boolean darkMode = true;

    private Timer pulseTimer;
    private float pulse = 0f;

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
    private JPanel root;
    private JPanel header;
    private JPanel center;
    private JPanel bottom;

    public Main() {
        buildUI();
        startAnimation();
    }

    private void buildUI() {

        frame = new JFrame(
            "STC89C52 Microcontroller Simulator"
        );

        frame.setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );

        frame.setSize(900, 700);

        frame.setMinimumSize(
            new Dimension(760, 600)
        );

        frame.setLocationRelativeTo(null);

        root = new JPanel(
            new BorderLayout(14, 14)
        );

        root.setBorder(
            new EmptyBorder(
                12,
                12,
                12,
                12
            )
        );

        frame.setContentPane(root);

        header = createHeader();

        sourceEditor = createEditor();
        traceArea = createOutputArea();
        cpuArea = createOutputArea();

        sourceEditor.setText(
            "MOV A,#02\n" +
            "MOV R0,#04\n" +
            "XCH A,R0\n" +
            "ADD A,#03\n" +
            "INC A\n" +
            "DEC A\n" +
            "ANL A,#0F\n" +
            "ORL A,#01\n" +
            "SJMP 1\n" +
            "INC A\n" +
            "CLR A\n" +
            "END"
        );

        center = new JPanel(
            new GridLayout(
                1,
                3,
                12,
                0
            )
        );

        center.setOpaque(false);

        center.add(
            createPanel(
                "ASSEMBLY PROGRAM",
                new JScrollPane(sourceEditor)
            )
        );

        center.add(
            createPanel(
                "EXECUTION TRACE",
                new JScrollPane(traceArea)
            )
        );

        center.add(
            createPanel(
                "CPU STATE",
                new JScrollPane(cpuArea)
            )
        );

        bottom = createBottomBar();

        root.add(
            header,
            BorderLayout.NORTH
        );

        root.add(
            center,
            BorderLayout.CENTER
        );

        root.add(
            bottom,
            BorderLayout.SOUTH
        );

        applyTheme();
        updateDisplay();

        frame.setVisible(true);
    }

    private JPanel createHeader() {

        JPanel panel = new GradientPanel();

        panel.setLayout(
            new BorderLayout(
                10,
                5
            )
        );

        panel.setBorder(
            new EmptyBorder(
                13,
                15,
                13,
                15
            )
        );

        JPanel left = new JPanel();

        left.setOpaque(false);

        left.setLayout(
            new BoxLayout(
                left,
                BoxLayout.Y_AXIS
            )
        );

        titleLabel = new JLabel(
            "STC89C52  MICROCONTROLLER  SIMULATOR"
        );

        titleLabel.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                20
            )
        );

        JLabel subtitle = new JLabel(
            "8051 Architecture  •  Instruction Execution"
        );

        subtitle.setFont(
            new Font(
                "SansSerif",
                Font.PLAIN,
                11
            )
        );

        subtitle.setForeground(
            new Color(
                220,
                225,
                240
            )
        );

        left.add(titleLabel);

        left.add(
            Box.createVerticalStrut(4)
        );

        left.add(subtitle);

        JPanel right = new JPanel(
            new FlowLayout(
                FlowLayout.RIGHT,
                5,
                0
            )
        );

        right.setOpaque(false);

        JLabel chip = badge("●  8-BIT CPU");
        JLabel team = badge("ONJI BYTE");

        JButton mode = new JButton("☀ / ☾");

        mode.setToolTipText(
            "Toggle light / dark mode"
        );

        styleButton(
            mode,
            new Color(
                65,
                70,
                88
            )
        );

        mode.addActionListener(
            e -> {
                darkMode = !darkMode;
                applyTheme();
            }
        );

        right.add(chip);
        right.add(team);
        right.add(mode);

        panel.add(
            left,
            BorderLayout.WEST
        );

        panel.add(
            right,
            BorderLayout.EAST
        );

        return panel;
    }

    private JLabel badge(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                10
            )
        );

        label.setForeground(Color.WHITE);

        label.setBorder(
            new EmptyBorder(
                6,
                8,
                6,
                8
            )
        );

        return label;
    }

    private JPanel createPanel(
        String title,
        JComponent component
    ) {

        JPanel panel = new RoundedPanel();

        panel.setLayout(
            new BorderLayout(
                0,
                8
            )
        );

        panel.setBorder(
            new EmptyBorder(
                10,
                10,
                10,
                10
            )
        );

        JLabel label = new JLabel(title);

        label.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                11
            )
        );

        JScrollPane scroll =
            (JScrollPane) component;

        scroll.setBorder(
            BorderFactory.createEmptyBorder()
        );

        scroll.getVerticalScrollBar()
            .setUnitIncrement(14);

        scroll.getHorizontalScrollBar()
            .setUnitIncrement(14);

        panel.add(
            label,
            BorderLayout.NORTH
        );

        panel.add(
            scroll,
            BorderLayout.CENTER
        );

        return panel;
    }

    private JTextArea createEditor() {

        JTextArea area = new JTextArea();

        area.setFont(
            new Font(
                Font.MONOSPACED,
                Font.PLAIN,
                13
            )
        );

        area.setLineWrap(false);

        area.setTabSize(4);

        area.setBorder(
            new EmptyBorder(
                10,
                10,
                10,
                10
            )
        );

        return area;
    }

    private JTextArea createOutputArea() {

        JTextArea area = new JTextArea();

        area.setFont(
            new Font(
                Font.MONOSPACED,
                Font.PLAIN,
                11
            )
        );

        area.setLineWrap(false);

        area.setEditable(false);

        area.setBorder(
            new EmptyBorder(
                10,
                10,
                10,
                10
            )
        );

        return area;
    }

    private JPanel createBottomBar() {

        JPanel panel = new JPanel(
            new BorderLayout(
                10,
                6
            )
        );

        panel.setOpaque(false);

        JPanel buttons = new JPanel(
            new FlowLayout(
                FlowLayout.LEFT,
                6,
                0
            )
        );

        buttons.setOpaque(false);

        JButton load = createActionButton(
            "LOAD",
            ACCENT
        );

        JButton reset = createActionButton(
            "RESET",
            new Color(
                70,
                76,
                94
            )
        );

        JButton step = createActionButton(
            "STEP",
            new Color(
                70,
                76,
                94
            )
        );

        JButton run = createActionButton(
            "RUN ▶",
            new Color(
                45,
                155,
                105
            )
        );

        buttons.add(load);
        buttons.add(reset);
        buttons.add(step);
        buttons.add(run);

        load.addActionListener(
            e -> loadFromEditor()
        );

        reset.addActionListener(
            e -> resetSimulator()
        );

        step.addActionListener(
            e -> stepOnce()
        );

        run.addActionListener(
            e -> runProgram()
        );

        JPanel info = new JPanel(
            new GridLayout(
                2,
                1
            )
        );

        info.setOpaque(false);

        instructionLabel = new JLabel(
            "Instruction: -"
        );

        statusLabel = new JLabel(
            "● READY"
        );

        info.add(instructionLabel);
        info.add(statusLabel);

        pcLabel = new JLabel(
            "PC: 0000"
        );

        pcLabel.setFont(
            new Font(
                Font.MONOSPACED,
                Font.BOLD,
                11
            )
        );

        panel.add(
            buttons,
            BorderLayout.WEST
        );

        panel.add(
            info,
            BorderLayout.CENTER
        );

        panel.add(
            pcLabel,
            BorderLayout.EAST
        );

        return panel;
    }

    private JButton createActionButton(
        String text,
        Color base
    ) {

        JButton button = new JButton(text);

        styleButton(
            button,
            base
        );

        button.addMouseListener(
            new MouseAdapter() {

                public void mouseEntered(
                    MouseEvent e
                ) {

                    button.setFont(
                        new Font(
                            "SansSerif",
                            Font.BOLD,
                            12
                        )
                    );
                }

                public void mouseExited(
                    MouseEvent e
                ) {

                    button.setFont(
                        new Font(
                            "SansSerif",
                            Font.BOLD,
                            11
                        )
                    );
                }
            }
        );

        return button;
    }

    /*
     * Fixed button styling.
     *
     * BasicButtonUI prevents the Windows/system
     * Look & Feel from hiding the button text.
     */
    private void styleButton(
        JButton button,
        Color base
    ) {

        button.setFont(
            new Font(
                "SansSerif",
                Font.BOLD,
                11
            )
        );

        button.setForeground(Color.WHITE);

        button.setBackground(base);

        button.setOpaque(true);

        button.setContentAreaFilled(true);

        button.setBorderPainted(true);

        button.setFocusPainted(false);

        button.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    base,
                    1
                ),
                new EmptyBorder(
                    7,
                    12,
                    7,
                    12
                )
            )
        );

        button.setCursor(
            new Cursor(
                Cursor.HAND_CURSOR
            )
        );

        button.setUI(
            new javax.swing.plaf.basic.BasicButtonUI()
        );
    }

    private void applyTheme() {

        Color bg =
            darkMode
                ? DARK_BG
                : LIGHT_BG;

        Color panel =
            darkMode
                ? DARK_PANEL
                : LIGHT_PANEL;

        Color input =
            darkMode
                ? DARK_INPUT
                : LIGHT_INPUT;

        Color text =
            darkMode
                ? DARK_TEXT
                : LIGHT_TEXT;

        Color muted =
            darkMode
                ? DARK_MUTED
                : LIGHT_MUTED;

        root.setBackground(bg);

        sourceEditor.setBackground(input);

        sourceEditor.setForeground(text);

        sourceEditor.setCaretColor(text);

        traceArea.setBackground(input);

        traceArea.setForeground(text);

        cpuArea.setBackground(input);

        cpuArea.setForeground(text);

        if (titleLabel != null) {
            titleLabel.setForeground(
                Color.WHITE
            );
        }

        updateComponentTree(
            root,
            panel,
            text,
            muted,
            input
        );

        root.repaint();
    }

    private void updateComponentTree(
        Component component,
        Color panel,
        Color text,
        Color muted,
        Color input
    ) {

        if (
            component instanceof RoundedPanel
        ) {

            component.setBackground(panel);
        }

        if (
            component instanceof JLabel
        ) {

            JLabel label =
                (JLabel) component;

            if (
                label == instructionLabel ||
                label == pcLabel
            ) {

                label.setForeground(muted);

            } else if (
                label == statusLabel
            ) {

                label.setForeground(GREEN);

            } else if (
                label != titleLabel
            ) {

                label.setForeground(text);
            }
        }

        if (
            component instanceof JScrollPane
        ) {

            JScrollPane scroll =
                (JScrollPane) component;

            scroll.getViewport()
                .setBackground(input);
        }

        if (
            component instanceof Container
        ) {

            for (
                Component child :
                (
                    (Container) component
                ).getComponents()
            ) {

                updateComponentTree(
                    child,
                    panel,
                    text,
                    muted,
                    input
                );
            }
        }
    }

    private void startAnimation() {

        pulseTimer = new Timer(
            45,
            e -> {

                pulse += 0.12f;

                if (
                    pulse > 6.28f
                ) {

                    pulse = 0f;
                }

                if (
                    statusLabel != null
                ) {

                    int alpha =
                        170
                        +
                        (int) (
                            60
                            *
                            (
                                0.5
                                +
                                0.5
                                *
                                Math.sin(pulse)
                            )
                        );

                    statusLabel.setForeground(
                        new Color(
                            GREEN.getRed(),
                            GREEN.getGreen(),
                            GREEN.getBlue(),
                            Math.min(
                                255,
                                alpha
                            )
                        )
                    );
                }
            }
        );

        pulseTimer.start();
    }

    private String[] getProgramFromEditor() {

        String[] raw =
            sourceEditor
                .getText()
                .split("\\R");

        List<String> program =
            new ArrayList<>();

        for (
            String line :
            raw
        ) {

            line = line.trim();

            if (
                !line.isEmpty()
            ) {

                program.add(line);
            }
        }

        return program.toArray(
            new String[0]
        );
    }

    private void loadFromEditor() {

        String[] program =
            getProgramFromEditor();

        if (
            program.length == 0
        ) {

            showError(
                "Enter at least one instruction."
            );

            return;
        }

        try {

            simulator.loadProgram(
                program
            );

            programLoaded = true;

            stepNumber = 1;

            traceArea.setText(
                "✓ PROGRAM LOADED\n"
                +
                "────────────────────────────\n"
                +
                formatProgram(program)
            );

            updateDisplay();

        } catch (
            Exception ex
        ) {

            programLoaded = false;

            showError(
                "Invalid program:\n"
                +
                ex.getMessage()
            );
        }
    }

    private String formatProgram(
        String[] program
    ) {

        StringBuilder result =
            new StringBuilder();

        for (
            int i = 0;
            i < program.length;
            i++
        ) {

            result.append(
                String.format(
                    "%04X   %s%n",
                    i,
                    program[i]
                )
            );
        }

        return result.toString();
    }

    private void stepOnce() {

        if (
            !ensureLoaded()
        ) {

            return;
        }

        if (
            isFinished()
        ) {

            showMessage(
                "Program finished. Press RESET to start again."
            );

            return;
        }

        simulator.step();

        appendStepTrace();

        stepNumber++;

        updateDisplay();
    }

    private void runProgram() {

        if (
            !ensureLoaded()
        ) {

            return;
        }

        if (
            isFinished()
        ) {

            showMessage(
                "Program finished. Press RESET to start again."
            );

            return;
        }

        int safety = 0;

        while (
            safety < 1000
            &&
            !isFinished()
        ) {

            simulator.step();

            appendStepTrace();

            stepNumber++;

            safety++;

            String status =
                simulator
                    .getExecutionStatus();

            if (
                status.startsWith(
                    "Execution error"
                )
                ||
                status.startsWith(
                    "Unsupported instruction"
                )
            ) {

                break;
            }
        }

        updateDisplay();
    }

    private void appendStepTrace() {

        Instruction instruction =
            simulator
                .getCurrentInstruction();

        traceArea.append(
            "\nSTEP "
            +
            stepNumber
            +
            "\n"
            +
            "────────────────────────────\n"
        );

        if (
            instruction != null
        ) {

            traceArea.append(
                "Instruction : "
                +
                instruction
                    .getFullInstruction()
                +
                "\n"
                +
                "Category    : "
                +
                instruction
                    .getCategory()
                +
                "\n\n"
            );
        }

        traceArea.append(
            simulator
                .getExecutionTrace()
        );

        traceArea.append(
            "STATUS : "
            +
            simulator
                .getExecutionStatus()
            +
            "\n"
        );

        traceArea.append(
            "════════════════════════════\n"
        );

        traceArea.setCaretPosition(
            traceArea
                .getDocument()
                .getLength()
        );
    }

    private void resetSimulator() {

        simulator.reset();

        programLoaded = false;

        stepNumber = 1;

        traceArea.setText(
            "READY\n\n"
            +
            "Edit the assembly program and press LOAD.\n"
            +
            "Then use STEP or RUN to execute it."
        );

        updateDisplay();
    }

    private void updateDisplay() {

        cpuArea.setText(
            simulator
                .getCPU()
                .getState()
        );

        Instruction instruction =
            simulator
                .getCurrentInstruction();

        instructionLabel.setText(
            "Instruction: "
            +
            (
                instruction == null
                    ? "-"
                    : instruction
                        .getFullInstruction()
            )
        );

        String status =
            simulator
                .getExecutionStatus();

        statusLabel.setText(
            "●  "
            +
            status.toUpperCase()
        );

        pcLabel.setText(
            String.format(
                "PC: %04X",
                simulator
                    .getCPU()
                    .getPC()
            )
        );
    }

    private boolean ensureLoaded() {

        if (
            !programLoaded
        ) {

            showMessage(
                "Load the assembly program first."
            );

            return false;
        }

        return true;
    }

    private boolean isFinished() {

        String status =
            simulator
                .getExecutionStatus();

        return
            status.equals(
                "Program terminated"
            )
            ||
            status.equals(
                "Program finished"
            );
    }

    private void showMessage(
        String message
    ) {

        JOptionPane.showMessageDialog(
            frame,
            message,
            "STC89C52 Simulator",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(
        String message
    ) {

        JOptionPane.showMessageDialog(
            frame,
            message,
            "STC89C52 Simulator - Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private class RoundedPanel
        extends JPanel {

        RoundedPanel() {
            setOpaque(false);
        }

        protected void paintComponent(
            Graphics g
        ) {

            Graphics2D g2 =
                (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color fill =
                darkMode
                    ? DARK_PANEL
                    : LIGHT_PANEL;

            Color border =
                darkMode
                    ? new Color(
                        55,
                        61,
                        78
                    )
                    : new Color(
                        218,
                        222,
                        232
                    );

            g2.setColor(fill);

            g2.fillRoundRect(
                0,
                0,
                getWidth() - 1,
                getHeight() - 1,
                16,
                16
            );

            g2.setColor(border);

            g2.drawRoundRect(
                0,
                0,
                getWidth() - 1,
                getHeight() - 1,
                16,
                16
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private class GradientPanel
        extends JPanel {

        GradientPanel() {
            setOpaque(false);
        }

        protected void paintComponent(
            Graphics g
        ) {

            Graphics2D g2 =
                (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            GradientPaint gradient =
                new GradientPaint(
                    0,
                    0,
                    new Color(
                        48,
                        39,
                        100
                    ),
                    getWidth(),
                    getHeight(),
                    new Color(
                        20,
                        90,
                        130
                    )
                );

            g2.setPaint(gradient);

            g2.fillRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                18,
                18
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    public static void main(
        String[] args
    ) {

        try {

            UIManager.setLookAndFeel(
                UIManager
                    .getSystemLookAndFeelClassName()
            );

        } catch (
            Exception ignored
        ) {
        }

        SwingUtilities.invokeLater(
            Main::new
        );
    }
}
