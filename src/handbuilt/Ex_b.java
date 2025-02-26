package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_b {

    public static Program buildAST() {
        List<VarDecl> decls = new ArrayList<>();
        // define "part" and "y" variables
        decls.add(new VarDecl(new TypeInt(), "part"));
        decls.add(new VarDecl(new TypeInt(), "y"));

        List<Stm> stms = new ArrayList<>();

        // first statement to add 6 and y
        Exp a = new ExpPlus(new ExpInt(6), new ExpVar("y"));
        // assign statment to variable part

        stms.add(new StmAssign("part", a));
        // subtract varibale previously defined and 3
        Exp e = new ExpMinus( new ExpVar("part"), new ExpInt(3));
        // assign it to variable y
        stms.add(new StmAssign("y", e));
        // first part of the print statement
        Exp c = new ExpPlus(new ExpVar("part"),new ExpVar("y"));
        // compose print statement
        Exp d = new ExpTimes(c,new ExpInt(10));
        // print
        stms.add(new StmPrintChar(d));

        return new Program(decls, stms);

    }

    public static void main(String[] args) throws IOException {

        System.out.println(buildAST());
        Program p = buildAST();
        p.compile();

        AST.write(Paths.get("temp.ssma"));
    }
}
