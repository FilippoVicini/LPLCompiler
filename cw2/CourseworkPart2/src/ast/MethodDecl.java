package ast;

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

    /**
     * Gets the method's return type.
     * @return the return type
     */
    public Type getReturnType() {
        return returnType;
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

    /**
     * Gets the number of parameters for this method.
     * @return the number of parameters
     */
    public int getParameterCount() {
        return params.size();
    }

    /**
     * Gets the number of local variables for this method.
     * @return the number of local variables
     */
    public int getLocalCount() {
        return locals.size();
    }

    /**
     * Checks if this method has a return type of void.
     * @return true if the method returns void, false otherwise
     */


    /**
     * Returns a parameter declaration by index.
     * @param index the index of the parameter
     * @return the parameter declaration at the given index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public VarDecl getParameter(int index) {
        return params.get(index);
    }

    /**
     * Returns a local variable declaration by index.
     * @param index the index of the local variable
     * @return the local variable declaration at the given index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public VarDecl getLocal(int index) {
        return locals.get(index);
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType).append(" ").append(name).append("(");

        // Add parameters
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i));
        }
        sb.append(") {\n");

        // Add local variables
        for (VarDecl local : locals) {
            sb.append("  ").append(local).append(";\n");
        }

        // Add statements
        for (Stm statement : statements) {
            sb.append("  ").append(statement).append("\n");
        }
        sb.append("}");

        return sb.toString();
    }
}