package ast;

import compile.SymbolTable;

public class StmIf extends Stm {

    private static int labelCounter = 0;
    public final Exp exp;
    public final Stm trueBranch, falseBranch;

    public StmIf(Exp exp, Stm trueBranch, Stm falseBranch) {
        this.exp = exp;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    @Override
    public void compile(SymbolTable st) {
        int currentLabel = labelCounter++;
        String elseLabel = "$_if_e_" + currentLabel;
        String endLabel = "$_if_end_" + currentLabel;

        exp.compile(st);
        emit("jumpi_z " + elseLabel);

        trueBranch.compile(st);
        if (falseBranch != null) {
            emit("jumpi " + endLabel);
            emit(elseLabel + ":");
            falseBranch.compile(st);
            emit(endLabel + ":");
        } else {
            emit(elseLabel + ":");
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}