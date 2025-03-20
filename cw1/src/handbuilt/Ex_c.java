package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_c {

    public static Program buildAST() {

        List<VarDecl> decls = new ArrayList<>();
        // define a new variable named "count" of type int
        decls.add(new VarDecl(new TypeInt(), "count"));

        // create a list of blockStms for the while loop
        List<Stm> blockStms = new ArrayList<>();
        // first line in while loop: print 32
        blockStms.add(new StmPrintChar(new ExpInt(32)));
        // second line inside while loop: print a newly defined variable named "count"
        blockStms.add(new StmPrint(new ExpVar("count")));
        // third line inside while loop: decrease count by 1
        blockStms.add(new StmAssign(
                        "count",
                        new ExpMinus(new ExpVar("count"), new ExpInt(1))
                )
        );

        List<Stm> stms = new ArrayList<>();
        // create new int var of value "3"
        Exp a =new ExpInt(3);
        // assign the value of 3 to the variable named "count"
        stms.add(new StmAssign("count", a));

        // define while loop condition
        Exp c = new ExpPlus(new ExpVar("count"), new ExpInt(1));

        // while loop block
        stms.add(new StmWhile(
                        // while loop condition comparing 0 to previously defined operation named "c"
                        new ExpLessThan(new ExpInt(0), c),
                        // block of statements inside while loop
                        new StmBlock(blockStms)
                )
        );

        Program program = new Program(decls, stms);
        return program;
    }

    public static void main(String[] args)  {
        System.out.println(buildAST());
    }
}
