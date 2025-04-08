package ast;

import compile.SymbolTable;
import java.util.List;

/**
 * Method/procedure call statement.
 */
public class StmMethodCall extends Stm {
    public final String methodName;
    public final List<Exp> arguments;

    public StmMethodCall(String methodName, List<Exp> arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }

    @Override
    public void compile(SymbolTable st) {

        for (int i = arguments.size() - 1; i >= 0; i--) {
            arguments.get(i).compile(st);
        }


        emit("call " + methodName);

        if (!arguments.isEmpty()) {
            emit("pop " + arguments.size());
        }
    }


    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}