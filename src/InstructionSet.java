import java.util.LinkedHashMap;
import java.util.Map;

public class InstructionSet {

    private Map<String, String> instructionCategories;

    public InstructionSet() {
        instructionCategories = new LinkedHashMap<>();

        // Data Transfer
        instructionCategories.put("MOV", "Data Transfer");
        instructionCategories.put("XCH", "Data Transfer");

        // Arithmetic
        instructionCategories.put("ADD", "Arithmetic");
        instructionCategories.put("SUBB", "Arithmetic");
        instructionCategories.put("INC", "Increment / Decrement");
        instructionCategories.put("DEC", "Increment / Decrement");

        // Logical
        instructionCategories.put("ANL", "Logical");
        instructionCategories.put("ORL", "Logical");
        instructionCategories.put("CLR", "Logical");

        // Control Flow
        instructionCategories.put("SJMP", "Control Flow");



        // Stack
        instructionCategories.put("PUSH", "Stack");
        instructionCategories.put("POP", "Stack");

        // FIFO Queue
        instructionCategories.put("ENQUEUE", "FIFO Queue");
        instructionCategories.put("DEQUEUE", "FIFO Queue");

        // Program Termination
        instructionCategories.put("END", "Program Termination");
    }

    public boolean isSupported(String instruction) {
        return instruction != null
                && instructionCategories.containsKey(instruction.trim().toUpperCase());
    }

    public String getCategory(String instruction) {
        return instructionCategories.get(instruction.trim().toUpperCase());
    }

    public Instruction createInstruction(String line) {
        if (line == null) {
            return null;
        }

        line = line.trim();

        if (line.isEmpty()) {
            return null;
        }

        String[] parts = line.split("\\s+", 2);
        String name = parts[0].toUpperCase();
        String operand = parts.length > 1 ? parts[1].trim() : "";

        if (!isSupported(name)) {
            throw new IllegalArgumentException("Unsupported instruction: " + name);
        }

        return new Instruction(name, operand, getCategory(name));
    }

    public void displayInstructionSet() {
        System.out.println("Supported Instructions");
        System.out.println("----------------------");

        for (Map.Entry<String, String> entry : instructionCategories.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }
}