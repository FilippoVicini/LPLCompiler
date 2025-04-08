package ast;

import compile.SymbolTable;
import java.util.List;

/**
 * AST node representing a method call expression.
 * For example: foo(x, y)
 */
public class ExpMethodCall extends Exp {
    public final String methodName;
    public final List<Exp> arguments;

    /**
     * Constructs a method call expression.
     *
     * @param methodName The name of the method to call
     * @param arguments List of expressions used as arguments
     */
    public ExpMethodCall(String methodName, List<Exp> arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }

    // push size of arguments
    @Override
    public void compile(SymbolTable st) {
        for (int i = arguments.size() - 1; i >= 0; i--) {
            arguments.get(i).compile(st);
        }

        emit("push " + arguments.size());


        emit("calli " + st.methodLabel(methodName));
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }


}