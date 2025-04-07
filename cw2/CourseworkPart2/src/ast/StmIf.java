package ast;

import compile.SymbolTable;

public class StmIf extends Stm {

    public final Exp exp;

    public final Stm trueBranch, falseBranch;

    public StmIf(Exp exp, Stm trueBranch, Stm falseBranch) {
        this.exp = exp;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public void compile(SymbolTable st) {
        String ifFalseLabel = st.freshLabel("if_false");
        String ifEndLabel = st.freshLabel("if_end");
        emit("// if-condition");
        exp.compile(st);
        emit("jumpi_z " + ifFalseLabel);
        emit("// true-branch");
        trueBranch.compile(st);
        emit("jumpi " + ifEndLabel);
        emit(ifFalseLabel + ":");
        emit("// false-branch");
        falseBranch.compile(st);
        emit(ifEndLabel + ":");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
