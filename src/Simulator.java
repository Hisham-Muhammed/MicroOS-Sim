public class Simulator {

    private CPU cpu;
    private Memory memory;
    private InstructionSet instructionSet;
    private Stack stack;
    private Queue queue;

    private Instruction currentInstruction;
    private String executionStatus;
    private StringBuilder executionTrace;

    // Makes SJMP execute only once in a program run.
    private boolean sjmpUsed;

    public Simulator() {
        cpu = new CPU();
        memory = new Memory();
        instructionSet = new InstructionSet();
        stack = new Stack(memory, cpu);
        queue = new Queue(8);
        executionStatus = "Ready";
        executionTrace = new StringBuilder();
        sjmpUsed = false;
    }

    // FETCH
    public Instruction fetch() {
        int pc = cpu.getPC();
        String instructionText = memory.readInstruction(pc);

        if (instructionText == null) {
            executionStatus = "Program finished";
            currentInstruction = null;
            return null;
        }

        currentInstruction =
                instructionSet.createInstruction(instructionText);

        cpu.incrementPC();

        executionTrace.append("FETCH ✓\n");

        return currentInstruction;
    }

    // DECODE
    public Instruction decode() {
        if (currentInstruction == null) {
            return null;
        }

        executionTrace.append("DECODE ✓\n");

        return currentInstruction;
    }

    // EXECUTE
    public void execute() {
        if (currentInstruction == null) {
            return;
        }

        String name = currentInstruction.getName();
        String operand = currentInstruction.getOperand();

        executionTrace.append("EXECUTE ✓\n");

        // Clear the previous instruction's error/status before executing
        // the current instruction. Otherwise a successful instruction
        // could still display an old error message.
        executionStatus = "Running";

        try {
            switch (name) {

                case "MOV":
                    executeMOV(operand);
                    break;

                case "XCH":
                    executeXCH(operand);
                    break;

                case "ADD":
                    executeADD(operand);
                    break;

                case "SUBB":
                    executeSUBB(operand);
                    break;

                case "ANL":
                    executeANL(operand);
                    break;

                case "ORL":
                    executeORL(operand);
                    break;

                case "INC":
                    executeINC(operand);
                    break;

                case "DEC":
                    executeDEC(operand);
                    break;

                case "SJMP":
                    executeSJMP(operand);
                    break;

                case "CLR":
                    executeCLR(operand);
                    break;

                case "PUSH":
                    executePUSH(operand);
                    break;

                case "POP":
                    executePOP(operand);
                    break;

                case "ENQUEUE":
                    executeENQUEUE(operand);
                    break;

                case "DEQUEUE":
                    executeDEQUEUE(operand);
                    break;

                case "END":
                    executionStatus = "Program terminated";
                    break;

                default:
                    executionStatus =
                            "Unsupported instruction: " + name;
            }

        } catch (IllegalArgumentException e) {
            executionStatus =
                    "Execution error: " + e.getMessage();
        }
    }

    // MOV
    private void executeMOV(String operand) {

        String[] parts = operand.split(",");

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid MOV instruction");
        }

        String destination =
                parts[0].trim().toUpperCase();

        String source =
                parts[1].trim().toUpperCase();

        if (source.startsWith("#")) {

            int value = parseValue(source);

            if (destination.equals("A")) {
                cpu.setA(value);

            } else if (destination.equals("B")) {
                cpu.setB(value);

            } else if (isRegister(destination)) {
                cpu.setRegister(
                        registerNumber(destination),
                        value);

            } else {
                throw new IllegalArgumentException(
                        "Invalid MOV destination: "
                                + destination);
            }

        } else if (
                source.equals("A")
                        && isRegister(destination)) {

            cpu.setRegister(
                    registerNumber(destination),
                    cpu.getA());

        } else if (
                isRegister(source)
                        && destination.equals("A")) {

            cpu.setA(
                    cpu.getRegister(
                            registerNumber(source)));

        } else {
            throw new IllegalArgumentException(
                    "Unsupported MOV form");
        }
    }

    // XCH A,Rn
    private void executeXCH(String operand) {

        String[] parts = operand.split(",");

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid XCH instruction");
        }

        String left =
                parts[0].trim().toUpperCase();

        String right =
                parts[1].trim().toUpperCase();

        if (!left.equals("A")
                || !isRegister(right)) {

            throw new IllegalArgumentException(
                    "XCH requires A,R0-R7");
        }

        int registerNumber =
                registerNumber(right);

        int oldA = cpu.getA();

        int oldRegister =
                cpu.getRegister(registerNumber);

        cpu.setA(oldRegister);
        cpu.setRegister(registerNumber, oldA);
    }

    // ADD A,#data or ADD A,Rn
    private void executeADD(String operand) {

        String[] parts = operand.split(",");

        if (parts.length != 2
                || !parts[0].trim().equalsIgnoreCase("A")) {
            throw new IllegalArgumentException(
                    "Invalid ADD instruction");
        }

        String source = parts[1].trim().toUpperCase();
        int value;

        if (source.startsWith("#")) {
            value = parseValue(source);
        } else if (isRegister(source)) {
            value = cpu.getRegister(registerNumber(source));
        } else {
            throw new IllegalArgumentException(
                    "ADD supports A,#data or A,R0-R7");
        }

        int result = cpu.getA() + value;

        cpu.setCarryFlag(result > 255);

        cpu.setA(result & 0xFF);
    }

    // SUBB A,#data
    private void executeSUBB(String operand) {

        if (!operand.toUpperCase().startsWith("A,#")) {
            throw new IllegalArgumentException(
                    "Invalid SUBB instruction");
        }

        int value =
                parseValue(
                        operand.substring(3).trim());

        int borrow =
                cpu.isCarryFlag() ? 1 : 0;

        int result =
                cpu.getA() - value - borrow;

        cpu.setCarryFlag(result < 0);

        cpu.setA(result & 0xFF);
    }

    // ANL A,#data
    private void executeANL(String operand) {

        if (!operand.toUpperCase().startsWith("A,#")) {
            throw new IllegalArgumentException(
                    "Invalid ANL instruction");
        }

        int value =
                parseValue(
                        operand.substring(3).trim());

        cpu.setA(
                cpu.getA() & value);
    }

    // ORL A,#data
    private void executeORL(String operand) {

        if (!operand.toUpperCase().startsWith("A,#")) {
            throw new IllegalArgumentException(
                    "Invalid ORL instruction");
        }

        int value =
                parseValue(
                        operand.substring(3).trim());

        cpu.setA(
                cpu.getA() | value);
    }

    // INC A or INC Rn
    private void executeINC(String operand) {

        String target =
                operand.trim().toUpperCase();

        if (target.equals("A")) {

            cpu.setA(
                    (cpu.getA() + 1) & 0xFF);

        } else if (isRegister(target)) {

            int registerNumber =
                    registerNumber(target);

            cpu.setRegister(
                    registerNumber,
                    (cpu.getRegister(registerNumber)
                            + 1) & 0xFF);

        } else {

            throw new IllegalArgumentException(
                    "Invalid INC target");
        }
    }

    // DEC A or DEC Rn
    private void executeDEC(String operand) {

        String target =
                operand.trim().toUpperCase();

        if (target.equals("A")) {

            cpu.setA(
                    (cpu.getA() - 1) & 0xFF);

        } else if (isRegister(target)) {

            int registerNumber =
                    registerNumber(target);

            cpu.setRegister(
                    registerNumber,
                    (cpu.getRegister(registerNumber)
                            - 1) & 0xFF);

        } else {

            throw new IllegalArgumentException(
                    "Invalid DEC target");
        }
    }

    // PUSH source: PUSH A, PUSH Rn, or PUSH #data
    private void executePUSH(String operand) {
        String source = operand.trim().toUpperCase();
        int value;
        if (source.equals("A")) value = cpu.getA();
        else if (isRegister(source)) value = cpu.getRegister(registerNumber(source));
        else if (source.startsWith("#")) value = parseValue(source);
        else throw new IllegalArgumentException("PUSH supports A, R0-R7 or #data");
        stack.push(value);
        executionTrace.append("PUSH -> ").append(String.format("%02X", value)).append(" (SP=")
                .append(String.format("%02X", cpu.getSP())).append(")\n");
    }

    // POP destination: POP A or POP Rn
    private void executePOP(String operand) {
        String destination = operand.trim().toUpperCase();
        int value = stack.pop();
        if (destination.equals("A")) cpu.setA(value);
        else if (isRegister(destination)) cpu.setRegister(registerNumber(destination), value);
        else throw new IllegalArgumentException("POP supports A or R0-R7");
        executionTrace.append("POP <- ").append(String.format("%02X", value)).append(" (SP=")
                .append(String.format("%02X", cpu.getSP())).append(")\n");
    }

    // ENQUEUE source: ENQUEUE A, Rn, or #data
    private void executeENQUEUE(String operand) {
        String source = operand.trim().toUpperCase();
        int value;
        if (source.equals("A")) value = cpu.getA();
        else if (isRegister(source)) value = cpu.getRegister(registerNumber(source));
        else if (source.startsWith("#")) value = parseValue(source);
        else value = parseValue(source);
        queue.enqueue(value);
        executionTrace.append("ENQUEUE -> ").append(String.format("%02X", value))
                .append(" | Queue size=").append(queue.size()).append("\n");
    }

    // DEQUEUE destination: DEQUEUE A or DEQUEUE Rn
    private void executeDEQUEUE(String operand) {
        String destination = operand.trim().toUpperCase();
        int value = queue.dequeue();
        if (destination.equals("A")) cpu.setA(value);
        else if (isRegister(destination)) cpu.setRegister(registerNumber(destination), value);
        else throw new IllegalArgumentException("DEQUEUE supports A or R0-R7");
        executionTrace.append("DEQUEUE <- ").append(String.format("%02X", value))
                .append(" | Queue size=").append(queue.size()).append("\n");
    }

    // SJMP
    //
    // In this educational simulator, the operand is treated as
    // a relative offset from the already-incremented PC.
    // SJMP is intentionally allowed to redirect execution only once
    // during a program run to keep the demo deterministic.
    private void executeSJMP(String operand) {

        int offset = parseValue(operand);

        if (!sjmpUsed) {

            sjmpUsed = true;

            cpu.setPC(cpu.getPC() + offset);

        } else {

            // SJMP already used once.
            // PC has already moved to the next instruction
            // during FETCH, so execution continues normally.
        }
    }

    // CLR A
    private void executeCLR(String operand) {

        if (!operand.trim().equalsIgnoreCase("A")) {

            throw new IllegalArgumentException(
                    "CLR currently supports A only");
        }

        cpu.setA(0);
    }

    // STEP = FETCH -> DECODE -> EXECUTE
    public void step() {

        executionTrace.setLength(0);

        Instruction instruction =
                fetch();

        if (instruction == null) {
            return;
        }

        decode();

        execute();

        if (!executionStatus.equals("Program terminated")
                && !executionStatus.equals("Program finished")
                && !executionStatus.startsWith("Execution error")
                && !executionStatus.startsWith("Unsupported instruction")) {

            executionStatus = "Running";
        }
    }

    // RUN
    public void run() {

        executionStatus = "Running";

        int safetyCounter = 0;

        while (safetyCounter < 1000) {

            if (cpu.getPC() >= memory.getProgramSize()) {

                executionStatus = "Program finished";

                break;
            }

            step();

            safetyCounter++;

            if (
                    executionStatus.equals(
                            "Program terminated")
                    ||
                    executionStatus.equals(
                            "Program finished")
                    ||
                    executionStatus.startsWith(
                            "Execution error")
                    ||
                    executionStatus.startsWith(
                            "Unsupported instruction")) {

                break;
            }
        }

        if (safetyCounter >= 1000) {

            executionStatus =
                    "Stopped: execution limit reached";
        }
    }

    // LOAD PROGRAM
    public void loadProgram(String[] program) {

        if (program == null) {

            throw new IllegalArgumentException(
                    "Program cannot be null");
        }

        memory.reset();

        cpu.reset();

        currentInstruction = null;

        executionTrace.setLength(0);
        stack.reset();
        queue.reset();

        // Allow SJMP to be used once again
        // whenever a new program is loaded.
        sjmpUsed = false;

        for (int i = 0; i < program.length; i++) {

            if (program[i] != null
                    && !program[i].trim().isEmpty()) {

                instructionSet.createInstruction(
                        program[i]);

                memory.writeInstruction(
                        i,
                        program[i].trim());
            }
        }

        executionStatus = "Program loaded";
    }

    // RESET
    public void reset() {

        cpu.reset();

        memory.reset();

        currentInstruction = null;

        executionTrace.setLength(0);
        stack.reset();
        queue.reset();

        // Allow SJMP to be used again after reset.
        sjmpUsed = false;

        executionStatus = "Ready";
    }

    // CHECK R0-R7
    private boolean isRegister(String value) {

        return value != null
                && value.matches("R[0-7]");
    }

    // GET REGISTER NUMBER
    private int registerNumber(String register) {

        return Integer.parseInt(
                register.substring(1));
    }

    // PARSE VALUES
    private int parseValue(String value) {

        value = value.trim();

        if (value.startsWith("#")) {

            value =
                    value.substring(1).trim();
        }

        if (value.startsWith("0x")
                || value.startsWith("0X")) {

            return Integer.parseInt(
                    value.substring(2),
                    16);
        }

        if (value.endsWith("H")
                || value.endsWith("h")) {

            return Integer.parseInt(
                    value.substring(
                            0,
                            value.length() - 1),
                    16);
        }

        if (value.matches("-?[0-9A-Fa-f]+")
                && value.matches(".*[A-Fa-f].*")) {

            return Integer.parseInt(
                    value,
                    16);
        }

        return Integer.parseInt(value);
    }

    // GET CPU
    public CPU getCPU() {
        return cpu;
    }

    // GET MEMORY
    public Memory getMemory() {
        return memory;
    }

    public Stack getStack() {
        return stack;
    }

    public Queue getQueue() {
        return queue;
    }

    // GET CURRENT INSTRUCTION
    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    // GET EXECUTION STATUS
    public String getExecutionStatus() {
        return executionStatus;
    }

    // GET EXECUTION TRACE
    public String getExecutionTrace() {
        return executionTrace.toString();
    }
}
