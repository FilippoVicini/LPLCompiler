package ast;

import compile.SymbolTable;
import java.util.List;

/**
 * AST node representing an array access expression.
 * For example: arr[i][j]
 */
public class ExpArrayAccess extends Exp {
    public final String id;
    public final List<Exp> indexers;

    /**
     * Constructs an array access expression.
     *
     * @param id The identifier (name) of the array
     * @param indexers List of expressions used as array indices
     */
    public ExpArrayAccess(String id, List<Exp> indexers) {
        this.id = id;
        this.indexers = indexers;
    }

    @Override
    public void compile(SymbolTable st) {
        // Load base address of the array
        emit("load " + id);

        // Handle each indexer
        for (Exp indexer : indexers) {
            // Compute the index
            indexer.compile(st);

            // Index into the array
            emit("array_index");
        }

        // Load the value at the calculated address
        emit("load_indirect");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);

        for (Exp indexer : indexers) {
            sb.append('[').append(indexer).append(']');
        }

        return sb.toString();
    }
}