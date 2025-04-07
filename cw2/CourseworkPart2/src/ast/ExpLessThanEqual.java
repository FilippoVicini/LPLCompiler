package ast;

import compile.SymbolTable;

public class ExpLessThanEqual extends Exp {

    public final Exp left, right;

    public ExpLessThanEqual(Exp left, Exp right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void compile(SymbolTable st) {
        left.compile(st);
        right.compile(st);
        emit("push 1", "add");
        emit("sub", "test_n");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }

}
