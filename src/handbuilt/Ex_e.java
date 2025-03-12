package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_e {

    public static Program buildAST() {
        TypeInt intType = new TypeInt();

        // define 2 vars x and zz
        List<VarDecl> decls = new ArrayList<>();
        decls.add(new VarDecl(intType, "x"));
        decls.add(new VarDecl(intType, "zz"));

        // switch statement block
        List<StmSwitch.Case> cases = new ArrayList<>();
        cases.add(new StmSwitch.Case(7, new StmPrintln(new ExpInt(99))));
        cases.add(new StmSwitch.Case(-1, new StmPrintln(new ExpPlus(new ExpVar("x"), new ExpVar("zz")))));

        List<Stm> stms = new ArrayList<>();
        stms.add(new StmAssign("x", new ExpMinus(new ExpVar("x"), new ExpInt(1))));
        stms.add(new StmAssign("zz", new ExpInt(55)));

        // actions inside if statement
        stms.add(new StmSwitch(
                new ExpVar("x"),
                new StmPrintln(new ExpVar("x")),
                cases
        ));

        Program program = new Program(decls, stms);
        return program;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(buildAST());
        Program p = buildAST();
        p.compile();
        AST.write(Paths.get("tst.ssma"));
        System.out.println(buildAST());
    }
}
