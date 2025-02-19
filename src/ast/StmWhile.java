package ast;

import compile.StaticAnalysisException;
import compile.SymbolTable;

public class StmWhile extends Stm {

    public final Exp exp;

    public final Stm body;

    public StmWhile(Exp exp, Stm body) {
        this.exp = exp;
        this.body = body;
    }

    @Override
    public void compile(SymbolTable st) {
        // To Be Completed
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
