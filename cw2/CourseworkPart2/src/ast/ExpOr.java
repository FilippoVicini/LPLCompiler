package ast;

import compile.SymbolTable;

public class ExpOr extends Exp {

    public final Exp left, right;

    public ExpOr(Exp left, Exp right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void compile(SymbolTable st) {
        // this version is correct except for short-cut semantics
//        left.compile(st);
//        emit("test_z");
//        right.compile(st);
//        emit("test_z");
//        emit("mul");
//        emit("test_z");
        // this version is fully correct
        String longWayLabel = st.freshLabel("OR_long_way");
        String endLabel = st.freshLabel("OR_end");
        left.compile(st);
        emit("jumpi_z " + longWayLabel);
        emit("push 1", "jumpi " + endLabel);
        emit(longWayLabel + ":");
        right.compile(st);
        emit("test_z", "test_z");
        emit(endLabel + ":");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }

}
