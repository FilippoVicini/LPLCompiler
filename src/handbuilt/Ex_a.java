package handbuilt;

import ast.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Exp e = new ExpTimes( new ExpVar("x"),new ExpInt(9));
        // print the last statement
        stms.add(new StmPrint(e));
        return new Program(decls, stms);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(buildAST());
        Program p = buildAST();
        p.compile();

        AST.write(Paths.get("temp2.ssma"));

    }
}
