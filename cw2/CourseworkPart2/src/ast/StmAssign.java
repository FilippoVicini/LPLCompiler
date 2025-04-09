package ast;

import compile.SymbolTable;

import java.util.Objects;

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

        if(Objects.equals(st.getVarI(varName).getVarI(), INFO_GLOBALS)) {
            emit("get_dp");
            emit("push " + st.getVarI(varName).getOffset());
            emit("add");
            emit("swap");
            emit("store");
        }else if(Objects.equals(st.getVarI(varName).getVarI(), INFO_LOCALS)) {
            emit("get_fp");
            emit("push " + (-4 * st.getVarI(varName).getOffset()));
            emit("add");
            emit("swap");
            emit("store");
        }else {
            emit("get_fp");
            emit("push " + (4 * st.getVarI(varName).getOffset()));
            emit("add");
            emit("swap");
            emit("store");
        }

    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
