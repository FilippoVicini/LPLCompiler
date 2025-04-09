package ast;

import compile.SymbolTable;
import compile.VarInfo;

import static compile.SymbolTable.*;

public class StmAssign extends Stm {

    public final String varName;
    public final Exp exp;

    public StmAssign(String varName, Exp exp) {
        this.varName = varName;
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        exp.compile(st);

        VarInfo info = st.getVarScopeInfo(varName);

        switch (info.getVarInfo()) {
            case INFO_GLOBALS:
                emit("get_dp");
                emit("push " + info.getOffset());
                emit("add");
                emit("swap");
                emit("store");
                break;

            case INFO_LOCALS:
                emit("get_fp");
                emit("push " + (-4 * info.getOffset()));
                emit("add");
                emit("swap");
                emit("store");
                break;

            case INFO_PARAMETERS:
                emit("get_fp");
                emit("push " + (4 * info.getOffset()));
                emit("add");
                emit("swap");
                emit("store");
                break;

            default:
                throw new Error("Undefined variable or scope error during assignment: " + varName);
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
