package ast;

import compile.SymbolTable;
import java.util.List;

/**
 * AST node representing a new array expression.
 * For example: new int[10][20]
 */
public class ExpNewArray extends Exp {
    public final Type baseType;
    public final List<Exp> dimensions;

    /**
     * Constructs a new array expression.
     *
     */
    public ExpNewArray(Type baseType, List<Exp> dimensions) {
        this.baseType = baseType;
        this.dimensions = dimensions;
    }

    @Override
    public void compile(SymbolTable st) {

        for (Exp dimension : dimensions) {
            dimension.compile(st);
            emit("push");
        }

        emit("new_array " + dimensions.size());

    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ").append(baseType);

        for (Exp dimension : dimensions) {
            sb.append('[').append(dimension).append(']');
        }

        return sb.toString();
    }
}