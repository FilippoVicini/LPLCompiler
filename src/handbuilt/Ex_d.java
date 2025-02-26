package handbuilt;

import ast.*;

import java.util.ArrayList;
import java.util.List;

public class Ex_d {

    public static Program buildAST() {

        TypeInt intType = new TypeInt();

        List<VarDecl> decls = new ArrayList<>();
        decls.add(new VarDecl(intType, "x"));

        List<Stm> blockStms = new ArrayList<>();

        blockStms.add(
                        new StmAssign("x", new ExpMinus(new ExpVar("x"), new ExpInt(7)))
                );
        List<Stm> blockStms2 = new ArrayList<>();
        blockStms2.add(
                new StmIf(
                        new ExpLessThan(new ExpVar("x"), new ExpInt(30)),
                        new StmPrintln(new ExpInt(77)),
                        new StmPrintln(new ExpInt(88))
                )
        );



        List<Stm> stms = new ArrayList<>();
        stms.add(new StmAssign("x", new ExpInt(20)));
        stms.add(new StmIf(
                new ExpLessThan(new ExpVar("x"), new ExpInt(20)),
                new StmBlock(blockStms),
                new StmBlock(blockStms2)

        ));

        stms.add(     new StmPrintln(new ExpVar("x")));


        Program program = new Program(decls, stms);
        return program;
    }

    public static void main(String[] args) {
        System.out.println(buildAST());
    }
}
