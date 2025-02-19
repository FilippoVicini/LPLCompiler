package ast;

import compile.SymbolTable;

public class StmNewline extends Stm {

    public StmNewline() {}

    @Override
    public void compile(SymbolTable st) {
        // To Be Completed
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
