package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_e {

    public static Program buildAST() {
        TypeInt intType = new TypeInt();

        // define 2 new variables of type int with names "x" and "zz"
        List<VarDecl> decls = new ArrayList<>();
        decls.add(new VarDecl(intType, "x"));
        decls.add(new VarDecl(intType, "zz"));

        // switch statement block
        List<StmSwitch.Case> cases = new ArrayList<>();
        // first switch stm case where x == 7
        cases.add(new StmSwitch.Case(7, new StmPrintln(new ExpInt(99))));
        // second switch stm case where x ==-1
        cases.add(new StmSwitch.Case(-1, new StmPrintln(new ExpPlus(new ExpVar("x"), new ExpVar("zz")))));

        List<Stm> stms = new ArrayList<>();
        // assign "x" to the value of x -1
        stms.add(new StmAssign("x", new ExpMinus(new ExpVar("x"), new ExpInt(1))));
        // assign the value of 55 to the variable "zz"
        stms.add(new StmAssign("zz", new ExpInt(55)));

        // switch stm block
        stms.add(new StmSwitch(
                // variable for switch stm block
                new ExpVar("x"),
                // default case simply print x
                new StmPrintln(new ExpVar("x")),
                // include cases block
                cases
        ));
        Program program = new Program(decls, stms);
        return program;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(buildAST());
    }
}
