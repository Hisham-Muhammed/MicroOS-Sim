public class Instruction {

    private String name;
    private String operand;
    private String category;

    public Instruction(String name, String operand, String category) {
        this.name = name;
        this.operand = operand;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getOperand() {
        return operand;
    }

    public String getCategory() {
        return category;
    }

    public String getFullInstruction() {
        if (operand == null || operand.isEmpty()) {
            return name;
        }

        return name + " " + operand;
    }

    @Override
    public String toString() {
        return getFullInstruction();
    }
}
