package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_b {

    public static Program buildAST() {
        List<VarDecl> decls = new ArrayList<>();
        // define part and y vars
        decls.add(new VarDecl(new TypeInt(), "part"));
        decls.add(new VarDecl(new TypeInt(), "y"));

        List<Stm> stms = new ArrayList<>();

        // add y and 6
        Exp a = new ExpPlus(new ExpInt(6), new ExpVar("y"));

        // assign the operation to part
        stms.add(new StmAssign("part", a));
        // subtract part and 3
        Exp e = new ExpMinus( new ExpVar("part"), new ExpInt(3));
        // assign the subtract operation to y
        stms.add(new StmAssign("y", e));
        // first part of the print statement
        Exp c = new ExpPlus(new ExpVar("part"),new ExpVar("y"));
        // compose print statement
        Exp d = new ExpTimes(c,new ExpInt(10));
        // print
        stms.add(new StmPrintChar(d));

        return new Program(decls, stms);

    }

    public static void main(String[] args)  {

        System.out.println(buildAST());

    }
}
