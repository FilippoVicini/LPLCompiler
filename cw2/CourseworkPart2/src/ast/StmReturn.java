package ast;

import compile.SymbolTable;

public class StmReturn extends Stm {

    public final Exp exp;

    public StmReturn(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {

        if (exp != null) {
            exp.compile(st);
        }

        emit("ret");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}