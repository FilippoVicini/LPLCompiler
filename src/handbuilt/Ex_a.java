package handbuilt;

import ast.*;

import java.util.List;
import java.util.ArrayList;

public class Ex_a {

    public static Program buildAST() {
        List<VarDecl> decls = new ArrayList<>();
        decls.add(new VarDecl(new TypeInt(), "x"));

        List<Stm> stms = new ArrayList<>();
        // create int to assign to x
        Exp a = new ExpInt(3);
        // assign 3 to x
        stms.add(new StmAssign("x", a));
        // multiply x * 9
        Exp e = new ExpTimes(new ExpInt(9), new ExpVar("x"));
        // print the last statement
        stms.add(new StmPrint(e));

        // object program
        Program program = new Program(decls, stms);
        return program;
    }

    public static void main(String[] args) {
        System.out.println(buildAST());
    }
}
