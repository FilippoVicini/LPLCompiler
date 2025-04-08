package ast;

import compile.SymbolTable;

public class StmReturn extends Stm {
    private final Exp returnValue;

    /**
     * Constructor for return statement with expression
     */
    public StmReturn(Exp returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * Constructor for return statement without expression (void return)
     */
    public StmReturn() {
        this.returnValue = null;
    }

    @Override
    public void compile(SymbolTable st) {
        if (returnValue != null) {
            // Evaluate the return expression and push its value on the stack
            returnValue.compile(st);
        } else {
            // Push default return value for void methods
            emit("push 0");
        }

        // Return from the method with value on stack
        emit("ret");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}