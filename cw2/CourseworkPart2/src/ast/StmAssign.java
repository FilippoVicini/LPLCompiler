package ast;

import compile.SymbolTable;
import compile.VarInfo;

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

        VarInfo i = st.getVarInfo(varName);

        if(Objects.equals(st.getVarInfo(varName).getVarInfo(), INFO_GLOBALS)) {
            emit("get_dp");
            emit("push " + i.getOffset());
            emit("add");
            emit("swap");
            emit("store");
        }else if(Objects.equals(st.getVarInfo(varName).getVarInfo(), INFO_LOCALS)) {
            emit("get_fp");
            emit("push " + (-4 * i.getOffset()));
            emit("add");
            emit("swap");
            emit("store");
        }else {
            emit("get_fp");
            emit("push " + (4 * i.getOffset()));
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
