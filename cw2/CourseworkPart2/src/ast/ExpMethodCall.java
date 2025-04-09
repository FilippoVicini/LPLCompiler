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

    public ExpMethodCall(String methodName, List<Exp> arguments) {
        this.methodName = methodName;
        this.arguments = arguments;
    }

    @Override
    public void compile(SymbolTable st) {
        // Push arguments onto the stack in reverse order (right-to-left)
        for (int i = arguments.size() - 1; i >= 0; i--) {
            arguments.get(i).compile(st);
        }

        // Push the number of arguments
        emit("push " + arguments.size());

        // Call the method
        emit("calli " + st.getMethodLabel(methodName));

        // The return value is now on top of the stack
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}