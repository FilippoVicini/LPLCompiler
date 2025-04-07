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
        String loopStartLabel = st.freshLabel("while_start");
        String loopEndLabel = st.freshLabel("while_end");
        emit(loopStartLabel + ":");
        emit("// while-condition");
        exp.compile(st);
        emit("jumpi_z " + loopEndLabel);
        emit("// while-body");
        body.compile(st);
        emit("jumpi " + loopStartLabel);
        emit(loopEndLabel + ":");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
