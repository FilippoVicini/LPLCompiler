package ast;

import compile.SymbolTable;

public class StmAssign extends Stm {

    public final String varName;
    public final Exp exp;

    public StmAssign(String varName, Exp exp) {
        this.varName = varName;
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        // To Be Completed
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
