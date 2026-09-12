public class InstructionTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("TC01 MOV A,#05", InstructionTest::testMovA);
        run("TC02 MOV R0,#03", InstructionTest::testMovR0);
        run("TC03 XCH A,R0", InstructionTest::testXch);
        run("TC04 ADD A,#03", InstructionTest::testAdd);
        run("TC05 SUBB A,#02", InstructionTest::testSubb);
        run("TC06 ANL A,#03", InstructionTest::testAnl);
        run("TC07 ORL A,#01", InstructionTest::testOrl);
        run("TC08 INC A", InstructionTest::testInc);
        run("TC09 DEC A", InstructionTest::testDec);
        run("TC10 CLR A", InstructionTest::testClr);
        run("TC11 SJMP 1", InstructionTest::testSjmp);
        

        System.out.println();
        System.out.println("======================");
        System.out.println("Tests Passed : " + passed);
        System.out.println("Tests Failed : " + failed);
        System.out.println("======================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void run(String name, TestCase test) {
        if (test.run()) {
            passed++;
            System.out.println(name + " : PASS");
        } else {
            failed++;
            System.out.println(name + " : FAIL");
        }
    }

    private interface TestCase {
        boolean run();
    }

    private static boolean testMovA() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#05", "END"});
        s.step();
        return s.getCPU().getA() == 5;
    }

    private static boolean testMovR0() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV R0,#03"});
        s.step();
        return s.getCPU().getRegister(0) == 3;
    }

    private static boolean testXch() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#05", "MOV R0,#03", "XCH A,R0"});
        s.step();
        s.step();
        s.step();
        return s.getCPU().getA() == 3 && s.getCPU().getRegister(0) == 5;
    }

    private static boolean testAdd() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#05", "ADD A,#03"});
        s.step();
        s.step();
        return s.getCPU().getA() == 8;
    }

    private static boolean testSubb() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#08", "SUBB A,#02"});
        s.step();
        s.step();
        return s.getCPU().getA() == 6;
    }

    private static boolean testAnl() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#0F", "ANL A,#03"});
        s.step();
        s.step();
        return s.getCPU().getA() == 3;
    }

    private static boolean testOrl() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#06", "ORL A,#01"});
        s.step();
        s.step();
        return s.getCPU().getA() == 7;
    }

    private static boolean testInc() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#05", "INC A"});
        s.step();
        s.step();
        return s.getCPU().getA() == 6;
    }

    private static boolean testDec() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#05", "DEC A"});
        s.step();
        s.step();
        return s.getCPU().getA() == 4;
    }

    private static boolean testClr() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{"MOV A,#05", "CLR A"});
        s.step();
        s.step();
        return s.getCPU().getA() == 0 && s.getCPU().isZeroFlag();
    }

    private static boolean testSjmp() {
        Simulator s = new Simulator();
        s.loadProgram(new String[]{
                "MOV A,#05",
                "SJMP 1",
                "CLR A",
                "END"
        });
        s.step();
        s.step();
        return s.getCPU().getPC() == 3;
    }
}
