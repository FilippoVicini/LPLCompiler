package ast;

import compile.SymbolTable;
import java.util.List;
import java.util.Collections;

public class MethodDecl extends AST {
    public final String id;
    public final List<Formal> formals;
    public final List<VarDecl> locals;
    public final List<Stm> body;
    public final Type returnType;
    public final boolean isFunction;

    /**
     * Creates a new method declaration (either function or procedure).
     *
     */
    public MethodDecl(String id, Type returnType, List<Formal> formals, List<VarDecl> locals, List<Stm> body) {
        this.id = id;
        this.returnType = returnType;
        this.isFunction = (returnType != null);
        this.formals = Collections.unmodifiableList(formals);
        this.locals = Collections.unmodifiableList(locals);
        this.body = Collections.unmodifiableList(body);
    }

    /**
     * Creates a new procedure declaration (no return type).
     *
     */
    public MethodDecl(String id, List<Formal> formals, List<VarDecl> locals, List<Stm> body) {
        this(id, null, formals, locals, body);
    }

    /**
     * Gets the name of the method.
     */
    public String getMethodName() {
        return id;
    }


    /**
     * Compiles the method body into assembly instructions.
     */
    public void compileBody(SymbolTable st) {
        for (Stm s : body) {
            s.compile(st);
        }

        String methodType = isFunction ? "function" : "procedure";
        emit("// Implicit return for " + methodType + " fallthrough");
        emit("push 0");

        int numParams = formals.size();
        int numLocals = locals.size();
        emit("push " + (numParams + numLocals));
        emit("ret");
    }

    /**
     * Accept method for the visitor pattern.
     */
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}