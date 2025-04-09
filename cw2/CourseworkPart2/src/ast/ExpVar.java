package ast;

import compile.VarInfo;
import compile.SymbolTable;

import java.util.Objects;

import static compile.SymbolTable.*;

public class ExpVar extends Exp {

    public final String varName;

    public ExpVar(String name) {
        this.varName = name;
    }

    @Override
    public void compile(SymbolTable st) {
        VarInfo i = st.getVarI(varName);

        if(Objects.equals(i.getVarI(), INFO_GLOBALS))
        {
            emit("get_dp");
            emit("push " + i.getOffset());
            emit("add");
            emit("load");

        } else if (Objects.equals(i.getVarI(), INFO_LOCALS)) {
            emit("get_fp");
            emit("push " + (-4 * i.getOffset()));
            emit("add");
            emit("load");

        } else {
            emit("get_fp");
            emit("push " + (4 * i.getOffset()));
            emit("add");
            emit("load");
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
