public class Week3InstructionExamples {
    private static void run(String name, String[] program) {
        Simulator s = new Simulator();
        s.loadProgram(program);
        s.run();
        CPU c = s.getCPU();
        System.out.println("=== " + name + " ===");
        for (String line : program) System.out.println(line);
        System.out.println("--- OUTPUT ---");
        System.out.println("Status: " + s.getExecutionStatus());
        System.out.printf("A=%02X B=%02X R0=%02X R1=%02X PC=%04X SP=%02X CY=%d Z=%d%n",
                c.getA(), c.getB(), c.getRegister(0), c.getRegister(1), c.getPC(), c.getSP(),
                c.isCarryFlag() ? 1 : 0, c.isZeroFlag() ? 1 : 0);
        System.out.println();
    }
    public static void main(String[] args) {
        run("MOV", new String[]{"MOV A,#10","END"});
        run("XCH", new String[]{"MOV A,#10","MOV R0,A","MOV A,#20","XCH A,R0","END"});
        run("ADD", new String[]{"MOV A,#10","ADD A,#5","END"});
        run("SUBB", new String[]{"MOV A,#10","SUBB A,#3","END"});
        run("INC", new String[]{"MOV A,#10","INC A","END"});
        run("DEC", new String[]{"MOV A,#10","DEC A","END"});
        run("ANL", new String[]{"MOV A,#240","ANL A,#15","END"});
        run("ORL", new String[]{"MOV A,#240","ORL A,#15","END"});
        run("CLR", new String[]{"MOV A,#10","CLR A","END"});
        run("SJMP", new String[]{"MOV A,#10","SJMP 1","MOV A,#20","MOV A,#30","END"});
        run("END", new String[]{"MOV A,#10","END"});
        run("PUSH / POP", new String[]{"MOV A,#10","PUSH A","MOV A,#20","PUSH A","POP R0","POP R1","END"});
        run("ENQUEUE / DEQUEUE", new String[]{"ENQUEUE #10","ENQUEUE #20","ENQUEUE #30","DEQUEUE R0","DEQUEUE R1","DEQUEUE A","END"});
    }
}
