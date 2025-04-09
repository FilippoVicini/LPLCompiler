package ast;

import compile.SymbolTable;

public class StmReturn extends Stm {
    public final Exp exp;

    public StmReturn(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        if (st.isInMethod()) {
            if (exp != null) {
                exp.compile(st);
            } else {
                emit("push 0");
            }

            int numParams = st.getPCountCurrMethod();
            int numLocals = st.getLCountCurrMethod();
            emit("push " + (numParams + numLocals));

            emit("ret");
        } else {
            emit("halt");
        }
    }


    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
