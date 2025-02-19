package ast;

import compile.SymbolTable;

public class ExpMinus extends Exp {

    public final Exp left, right;

    public ExpMinus(Exp left, Exp right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void compile(SymbolTable st) {
        // To Be Completed
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }

}
