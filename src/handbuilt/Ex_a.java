package handbuilt;

import ast.*;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

public class Ex_a {

    public static Program buildAST() {
        List<VarDecl> decls = new ArrayList<>();
        // declare new variable of name x
        decls.add(new VarDecl(new TypeInt(), "x"));

        List<Stm> stms = new ArrayList<>();
        // create new int var of value "3"
        Exp a = new ExpInt(3);
        // assign the int value of 3 to x
        stms.add(new StmAssign("x", a));
        // multiply x by a new int of value 9
        Exp e = new ExpTimes( new ExpVar("x"),new ExpInt(9));
        // print the last statement
        stms.add(new StmPrint(e));
        return new Program(decls, stms);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(buildAST());
    }
}
