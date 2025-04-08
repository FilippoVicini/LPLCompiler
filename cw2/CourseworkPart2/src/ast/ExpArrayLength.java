package ast;

import ast.util.Visitor;
import compile.SymbolTable;

/**
 * Expression representing access to the length of an array.
 * For example: arr.length
 */
public class ExpArrayLength extends Exp {

    private final Exp array;

    /**
     * Constructs a new array length expression.
     *
     * @param array The expression that evaluates to an array.
     */
    public ExpArrayLength(Exp array) {
        this.array = array;
    }

    /**
     * Gets the array expression.
     *
     * @return The expression that evaluates to an array.
     */
    public Exp getArray() {
        return array;
    }

    @Override
    public void compile(SymbolTable st) {
        // First, compile the array expression to get its reference on the stack
        array.compile(st);

        // Then, emit an instruction to get the length of the array
        emit("array_length");
    }

    @Override
    public String toString() {
        return array.toString() + ".length";
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}