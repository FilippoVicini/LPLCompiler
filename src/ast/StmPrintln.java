package ast;

import compile.SymbolTable;

public class StmPrintln extends Stm {

    public final Exp exp;

    public StmPrintln(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        // To Be Completed
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
