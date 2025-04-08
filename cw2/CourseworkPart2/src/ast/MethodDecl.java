
package ast;

import compile.SymbolTable;

import java.util.List;
import java.util.Collections;

public class MethodDecl extends AST {
    public Type returnType;
    public String name;
    private List<VarDecl> params;
    private List<VarDecl> locals;
    private List<Stm> statements;

    /**
     * Creates a new method declaration.
     * @param returnType the return type of the method
     * @param name the name of the method
     * @param params the list of parameter declarations
     * @param locals the list of local variable declarations
     * @param statements the list of statements in the method body
     */
    public MethodDecl(Type returnType, String name, List<VarDecl> params,
                      List<VarDecl> locals, List<Stm> statements) {
        this.returnType = returnType;
        this.name = name;
        this.params = params != null ? params : Collections.emptyList();
        this.locals = locals != null ? locals : Collections.emptyList();
        this.statements = statements != null ? statements : Collections.emptyList();
    }

    /**
     * Gets the method name.
     * @return the name of the method
     */
    public String getName() {
        return name;
    }

    public void compileBody(SymbolTable st) {
        // Compile all statements in the method body
        for (Stm stm : statements) {
            stm.compile(st);
        }

        // Check if the last statement is a return
        boolean hasExplicitReturn = !statements.isEmpty() && statements.get(statements.size() - 1) instanceof StmReturn;

        // If the method doesn't end with a return, add a default one
        if (!hasExplicitReturn) {
            // Default return value
            emit("push 0");

            // Return from method
            emit("ret");
        }
    }
    /**
     * Gets the list of parameter declarations.
     * @return an unmodifiable list of parameter declarations
     */
    public List<VarDecl> getParams() {
        return Collections.unmodifiableList(params);
    }

    /**
     * Gets the list of local variable declarations.
     * @return an unmodifiable list of local variable declarations
     */
    public List<VarDecl> getLocals() {
        return Collections.unmodifiableList(locals);
    }

    /**
     * Gets the list of statements in the method body.
     * @return an unmodifiable list of statements
     */
    public List<Stm> getStatements() {
        return Collections.unmodifiableList(statements);
    }


}