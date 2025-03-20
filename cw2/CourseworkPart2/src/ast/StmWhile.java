package ast;

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
        emit("$_loop_s:");

        exp.compile(st);
        emit("jumpi_z $_loop_e");

        body.compile(st);
        emit("jumpi $_loop_s");

        emit("$_loop_e:");
    }


    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
