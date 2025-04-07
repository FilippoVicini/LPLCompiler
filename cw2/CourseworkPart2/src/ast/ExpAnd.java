package ast;

import compile.SymbolTable;

public class ExpAnd extends Exp {

    public final Exp left, right;

    public ExpAnd(Exp left, Exp right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void compile(SymbolTable st) {
        // this version is correct except for short-cut semantics
//        left.compile(st);
//        right.compile(st);
//        emit("mul", "test_z", "test_z");
        // this version is fully correct
        String shortCutLabel = st.freshLabel("AND_short_cut");
        String endLabel = st.freshLabel("AND_end");
        left.compile(st);
        emit("jumpi_z " + shortCutLabel);
        right.compile(st);
        emit("test_z", "test_z");
        emit("jumpi " + endLabel);
        emit(shortCutLabel + ":");
        emit("push 0");
        emit(endLabel + ":");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }

}
