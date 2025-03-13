package handbuilt;

import ast.*;
import test.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ex_d {

    public static Program buildAST() {

        TypeInt intType = new TypeInt();

        List<VarDecl> decls = new ArrayList<>();
        // define a new variable of type int with name "x"
        decls.add(new VarDecl(intType, "x"));

        // first if statement block
        List<Stm> blockStms = new ArrayList<>();
        blockStms.add(
                // operation of subtracting 7 from current value of x
                new StmAssign("x", new ExpMinus(new ExpVar("x"), new ExpInt(7)))
                );
        // else statement with inner if statement block
        List<Stm> blockStms2 = new ArrayList<>();
        blockStms2.add(
                // nested if statement inside else statement
                new StmIf(
                        // if block condition x < 30
                        new ExpLessThan(new ExpVar("x"), new ExpInt(30)),
                        // if condition is met print 77
                        new StmPrintln(new ExpInt(77)),
                        // if condition is not met (else) print 88
                        new StmPrintln(new ExpInt(88))
                )
        );

        List<Stm> stms = new ArrayList<>();
        stms.add(new StmAssign("x", new ExpInt(20)));
        // if statement block
        stms.add(new StmIf(
                // if block condition x < 20
                new ExpLessThan(new ExpVar("x"), new ExpInt(20)),
                // block statements if condition is met
                new StmBlock(blockStms),
                // block statements if condition is not met (else)
                new StmBlock(blockStms2)
        ));

        // finish the code printing a new variable x
        stms.add(new StmPrintln(new ExpVar("x")));

        Program program = new Program(decls, stms);
        return program;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(buildAST());
    }
}
