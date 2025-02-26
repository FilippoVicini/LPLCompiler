package handbuilt;

import ast.*;

import java.util.ArrayList;
import java.util.List;

public class Ex_e {

    public static Program buildAST() {
        TypeInt intType = new TypeInt();

        List<VarDecl> decls = new ArrayList<>();
        decls.add(new VarDecl(intType, "x"));
        decls.add(new VarDecl(intType, "zz"));

        List<StmSwitch.Case> cases = new ArrayList<>();
        cases.add(new StmSwitch.Case(7, new StmPrintln(new ExpInt(99))));
        cases.add(new StmSwitch.Case(-1, new StmPrintln(new ExpPlus(new ExpVar("x"), new ExpVar("zz")))));

        List<Stm> stms = new ArrayList<>();
        stms.add(      new StmAssign("x", new ExpMinus(new ExpVar("x"), new ExpInt(1))));
        stms.add(new StmAssign("zz", new ExpInt(55)));

        stms.add(new StmSwitch(
               new ExpVar("x"),
                new StmPrintln(new ExpVar("x")),
                cases
        ));

        Program program = new Program(decls, stms);
        return program;
    }

    public static void main(String[] args) {
        System.out.println(buildAST());
    }
}
