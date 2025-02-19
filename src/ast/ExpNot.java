package ast;

import compile.SymbolTable;

public class ExpNot extends Exp {

    public final Exp e;

    public ExpNot(Exp e) {
        this.e = e;
    }

    @Override
    public void compile(SymbolTable st) {
        // To Be Completed
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
