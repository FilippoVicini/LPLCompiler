package ast;

import compile.SymbolTable;

import java.util.List;

/**
 * AST node representing an array assignment statement.
 * For example: arr[i][j] = expression;
 */
public class StmArrayAssign extends Stm {
    private final String id;
    private final List<Exp> indexers;
    private final Exp value;


    public StmArrayAssign(String id, List<Exp> indexers, Exp value) {
        this.id = id;
        this.indexers = indexers;
        this.value = value;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id);

        for (Exp indexer : indexers) {
            sb.append('[').append(indexer).append(']');
        }

        sb.append(" = ").append(value).append(';');
        return sb.toString();
    }

    @Override
    public void compile(SymbolTable st) {

        emit("load " + id);


        for (int i = 0; i < indexers.size(); i++) {

            indexers.get(i).compile(st);


            if (i < indexers.size() - 1) {
                // For all but the last dimension, we need to load the sub-array
                emit("array_deref");
            }

            emit("array_index");
        }

        emit("store_addr temp_array_addr");

        value.compile(st);


        emit("load_addr temp_array_addr");


        emit("array_store");
    }

}