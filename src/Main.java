import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private final Simulator simulator;
    private final JTextArea sourceEditor;
    private final JTextArea traceArea;
    private final JTextArea cpuArea;
    private final JLabel statusLabel;
    private final JLabel instructionLabel;

    private int stepNumber = 1;
    private boolean programLoaded = false;

    public Main() {

        simulator = new Simulator();

        JFrame frame = new JFrame(
                "STC89C52 Microcontroller Simulator - Week 2"
        );

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1250, 720);
        frame.setMinimumSize(new Dimension(950, 600));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.setContentPane(root);

        JLabel title = new JLabel(
                "STC89C52 MICROCONTROLLER SIMULATOR",
                SwingConstants.CENTER
        );
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(title, BorderLayout.NORTH);

        sourceEditor = createTextArea();
        traceArea = createTextArea();
        cpuArea = createTextArea();

        cpuArea.setEditable(false);
        traceArea.setEditable(false);

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

        JPanel center = new JPanel(
                new GridLayout(1, 3, 8, 0)
        );

        center.add(wrap(
                "Assembly Source Editor",
                new JScrollPane(sourceEditor)
        ));

        center.add(wrap(
                "System / Execution Trace",
                new JScrollPane(traceArea)
        ));

        center.add(wrap(
                "CPU Registers & Flags",
                new JScrollPane(cpuArea)
        ));

        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(
                new BorderLayout(8, 5)
        );

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        JButton loadButton = new JButton("LOAD");
        JButton resetButton = new JButton("RESET");
        JButton stepButton = new JButton("STEP");
        JButton runButton = new JButton("RUN");

        buttons.add(loadButton);
        buttons.add(resetButton);
        buttons.add(stepButton);
        buttons.add(runButton);

        instructionLabel =
                new JLabel("Current Instruction: -");

        statusLabel =
                new JLabel("Status: Ready");

        JPanel info = new JPanel(
                new BorderLayout()
        );

        info.add(instructionLabel, BorderLayout.WEST);
        info.add(statusLabel, BorderLayout.EAST);

        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(info, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);

        loadButton.addActionListener(
                e -> loadFromEditor()
        );

        resetButton.addActionListener(
                e -> resetSimulator()
        );

        stepButton.addActionListener(
                e -> stepOnce()
        );

        runButton.addActionListener(
                e -> runProgram()
        );

        updateDisplay();

        frame.setVisible(true);
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();

        area.setFont(new Font(
                Font.MONOSPACED,
                Font.PLAIN,
                13
        ));

        area.setLineWrap(false);

        return area;
    }

    private JPanel wrap(
            String title,
            JComponent component
    ) {
        JPanel panel = new JPanel(
                new BorderLayout(3, 3)
        );

        JLabel label = new JLabel(title);

        label.setFont(new Font(
                "SansSerif",
                Font.BOLD,
                13
        ));

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);

        return panel;
    }

    private String[] getProgramFromEditor() {

        String text = sourceEditor.getText();

        String[] rawLines = text.split("\\R");

        List<String> program = new ArrayList<>();

        for (String line : rawLines) {

            line = line.trim();

            if (!line.isEmpty()) {
                program.add(line);
            }
        }

        return program.toArray(new String[0]);
    }

    private void loadFromEditor() {

        String[] program = getProgramFromEditor();

        if (program.length == 0) {

            showError(
                    "Enter at least one instruction " +
                    "in the Assembly Source Editor."
            );

            return;
        }

        try {

            simulator.loadProgram(program);

            programLoaded = true;
            stepNumber = 1;

            traceArea.setText(
                    "PROGRAM LOADED\n" +
                    "------------------------------\n"
            );

            traceArea.append(
                    formatProgram(program)
            );

            updateDisplay();

        } catch (IllegalArgumentException ex) {

            programLoaded = false;

            showError(
                    "Invalid program:\n" +
                    ex.getMessage()
            );

            updateDisplay();
        }
    }

    private String formatProgram(String[] program) {

        StringBuilder text = new StringBuilder();

        for (int i = 0; i < program.length; i++) {

            text.append(
                    String.format(
                            "%04X  %s%n",
                            i,
                            program[i]
                    )
            );
        }

        return text.toString();
    }

    private void stepOnce() {

        if (!ensureLoaded()) {
            return;
        }

        if (isFinished()) {

            showMessage(
                    "Program has already finished. " +
                    "Press RESET and STEP again."
            );

            return;
        }

        simulator.step();

        appendStepTrace();

        stepNumber++;

        updateDisplay();
    }

    private void runProgram() {

        if (!ensureLoaded()) {
            return;
        }

        if (isFinished()) {

            showMessage(
                    "Program has already finished. " +
                    "Press RESET and RUN again."
            );

            return;
        }

        int safety = 0;

        while (safety < 1000 && !isFinished()) {

            simulator.step();

            appendStepTrace();

            stepNumber++;

            safety++;

            String status =
                    simulator.getExecutionStatus();

            if (status.startsWith("Execution error")
                    || status.startsWith("Unsupported instruction")) {
                break;
            }
        }

        updateDisplay();
    }

    private void appendStepTrace() {

        Instruction instruction =
                simulator.getCurrentInstruction();

        traceArea.append(
                "\nSTEP " +
                stepNumber +
                "\n"
        );

        traceArea.append(
                "--------------------------------\n"
        );

        if (instruction != null) {

            traceArea.append(
                    "Instruction : " +
                    instruction.getFullInstruction() +
                    "\n"
            );

            traceArea.append(
                    "Category    : " +
                    instruction.getCategory() +
                    "\n\n"
            );
        }

        traceArea.append(
                simulator.getExecutionTrace()
        );

        traceArea.append(
                "\nSTATUS : " +
                simulator.getExecutionStatus() +
                "\n"
        );

        traceArea.append(
                "================================\n"
        );
    }

    private void resetSimulator() {

        simulator.reset();

        programLoaded = false;
        stepNumber = 1;

        traceArea.setText("");

        instructionLabel.setText(
                "Current Instruction: -"
        );

        updateDisplay();
    }

    private void updateDisplay() {

        cpuArea.setText(
                simulator.getCPU().getState()
        );

        statusLabel.setText(
                "Status: " +
                simulator.getExecutionStatus()
        );

        Instruction instruction =
                simulator.getCurrentInstruction();

        instructionLabel.setText(
                "Current Instruction: " +
                (
                    instruction == null
                    ? "-"
                    : instruction.getFullInstruction()
                )
        );
    }

    private boolean ensureLoaded() {

        if (!programLoaded) {

            showMessage(
                    "No program loaded. " +
                    "Edit the source and press LOAD first."
            );

            return false;
        }

        return true;
    }

    private boolean isFinished() {

        String status =
                simulator.getExecutionStatus();

        return status.equals("Program terminated")
                || status.equals("Program finished");
    }

    private void showMessage(String message) {

        JOptionPane.showMessageDialog(
                null,
                message,
                "Simulator",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                null,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                Main::new
        );
    }
}
