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

    @Override
    public void compile(SymbolTable st) {
        // Push arguments in reverse order
        for (int i = arguments.size() - 1; i >= 0; i--) {
            arguments.get(i).compile(st);
            emit("push");
        }

        // Call the method
        emit("call " + methodName);

        // Clean up the stack if there are arguments
        if (!arguments.isEmpty()) {
            emit("pop " + arguments.size());
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName).append('(');

        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(arguments.get(i));
        }

        sb.append(')');
        return sb.toString();
    }
}