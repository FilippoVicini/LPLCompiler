package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_b {

    public static Program buildAST() {
        List<VarDecl> decls = new ArrayList<>();
        // define 2 new variables with names "part" and "y" of type int
        decls.add(new VarDecl(new TypeInt(), "part"));
        decls.add(new VarDecl(new TypeInt(), "y"));

        List<Stm> stms = new ArrayList<>();

        // using plus add y to 6
        Exp a = new ExpPlus(new ExpInt(6), new ExpVar("y"));

        // assign the "a" operation to the previously defined variable "part"
        stms.add(new StmAssign("part", a));
        //subtract the value 3 from "part"
        Exp e = new ExpMinus( new ExpVar("part"), new ExpInt(3));
        // assign the "e" operation of subtraction to the variable "y"
        stms.add(new StmAssign("y", e));
        // first part to compose print statement
        Exp c = new ExpPlus(new ExpVar("part"),new ExpVar("y"));
        // compose the complete print statement multiplying "c" and a new int of value 10
        Exp d = new ExpTimes(c,new ExpInt(10));
        // print the complete print statement
        stms.add(new StmPrintChar(d));

        return new Program(decls, stms);
    }

    public static void main(String[] args)  {
        System.out.println(buildAST());
    }
}
