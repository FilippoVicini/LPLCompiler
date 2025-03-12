package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_c {

    public static Program buildAST() {

        List<VarDecl> decls = new ArrayList<>();
        // define count variable
        decls.add(new VarDecl(new TypeInt(), "count"));

        // blockStms for while loop
        List<Stm> blockStms = new ArrayList<>();
        // print 32
        blockStms.add(new StmPrintChar(new ExpInt(32)));
        // print count
        blockStms.add(new StmPrint(new ExpVar("count")));
        // reduce count by 1
        blockStms.add(new StmAssign(
                        "count",
                        new ExpMinus(new ExpVar("count"), new ExpInt(1))
                )
        );

        List<Stm> stms = new ArrayList<>();
        // create variable 3
        Exp a =new ExpInt(3);
        // assign 3 to count
        stms.add(new StmAssign("count", a));

        // inner while loop statement
        Exp c = new ExpPlus(new ExpVar("count"), new ExpInt(1));

        // while loop
        stms.add(new StmWhile(
                        // 0 < c
                        new ExpLessThan(new ExpInt(0), c),
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
