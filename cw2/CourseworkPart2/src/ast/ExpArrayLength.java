package ast;

import ast.util.Visitor;
import compile.SymbolTable;


public class ExpArrayLength extends Exp {

    private final Exp array;


    public ExpArrayLength(Exp array) {
        this.array = array;
    }


    public Exp getArray() {
        return array;
    }

    @Override
    public void compile(SymbolTable st) {
        array.compile(st);

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