package ast;

import compile.SymbolTable;
import java.util.List;
import java.util.Collections;

public class StmMethodCall extends Stm {
    public final String id;
    public final List<Exp> actuals;

    public StmMethodCall(String id, List<Exp> actuals) {
        this.id = id;
        this.actuals = Collections.unmodifiableList(actuals);
    }

    @Override
    public void compile(SymbolTable st) {
        for (Exp arg : actuals) {
            arg.compile(st);
        }
        emit("push " + actuals.size());
        String methodLabel = st.getMethodLabel(id);
        emit("calli " + methodLabel);

        if (st.getMethodRetType(id) != null) {
            emit("pop");
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
