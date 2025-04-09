package ast;

import compile.VarInfo;
import compile.SymbolTable;

import static compile.SymbolTable.*;

public class ExpVar extends Exp {

    public final String varName;

    public ExpVar(String varName) {
        this.varName = varName;
    }

    @Override
    public void compile(SymbolTable st) {
        VarInfo info = st.getVarScopeInfo(varName);

        switch (info.getVarInfo()) {
            case INFO_GLOBALS:
                emit("get_dp");
                emit("push " + info.getOffset());
                emit("add");
                emit("load");
                break;

            case INFO_LOCALS:
                emit("get_fp");
                emit("push " + (-4 * info.getOffset()));
                emit("add");
                emit("load");
                break;

            case INFO_PARAMETERS:
                emit("get_fp");
                emit("push " + (4 * info.getOffset()));
                emit("add");
                emit("load");
                break;

            default:
                throw new Error("Undefined variable or scope error during compile: " + varName);
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
