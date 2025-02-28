package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_c {

    public static Program buildAST() {

        List<VarDecl> decls = new ArrayList<>();
        // define "part" and "y" variables
        decls.add(new VarDecl(new TypeInt(), "count"));


        List<Stm> blockStms = new ArrayList<>();
        blockStms.add(new StmPrintChar(new ExpInt(32)));
        blockStms.add(new StmPrint(new ExpVar("count")));
        blockStms.add(new StmAssign(
                        "count",
                        new ExpMinus(new ExpVar("count"), new ExpInt(1))

                )
        );

        List<Stm> stms = new ArrayList<>();
        // first statement to add 6 and y
        Exp a =new ExpInt(3);
        // assign statment to variable part
        stms.add(new StmAssign("count", a));

        Exp c = new ExpPlus(new ExpVar("count"), new ExpInt(1));

        stms.add(new StmWhile(
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
