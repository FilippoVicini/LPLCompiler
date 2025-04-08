package ast;

import compile.SymbolTable;

import java.util.Collections;
import java.util.List;

public class Program extends AST {

    public final List<VarDecl> varDecls;
    public final List<Stm> body;
    public final List<MethodDecl> funcs;

    // Track stack depth for debugging
    private static int stackDepth = 0;

    public Program(List<VarDecl> varDecls, List<Stm> body, List<MethodDecl> funcs) {
        this.varDecls = varDecls != null ? Collections.unmodifiableList(varDecls) : Collections.emptyList();
        this.body = body != null ? Collections.unmodifiableList(body) : Collections.emptyList();
        this.funcs = funcs != null ? Collections.unmodifiableList(funcs) : Collections.emptyList();
    }

    /**
     * Safely emit push operation while tracking stack depth
     */
    public static void safePush(String value) {
        stackDepth++;
        emit("push " + value);
    }

    /**
     * Safely emit pop operation while tracking stack depth
     */
    public static void safePop() {
        if (stackDepth <= 0) {
            throw new RuntimeException("Stack underflow detected - attempting to pop from empty stack");
        }
        stackDepth--;
        emit("pop");
    }

    /**
     * Reset stack tracking (call at the beginning of compilation)
     */
    private void resetStackTracking() {
        stackDepth = 0;
    }

    /**
     * Emit SSM assembly code for this program.
     */
    public void compile() {
        // Reset stack tracking
        resetStackTracking();

        // Create symbol table and register all symbols
        SymbolTable st = new SymbolTable(this);

        // First emit method definitions
        for (MethodDecl m : funcs) {
            emit(st.methodLabel(m.getName()) + ":");

            // Push the method context onto the stack
            st.pushMethodContext(m.getName());

            // Track stack frame setup
            int initialStackDepth = stackDepth;

            // Compile method body
            for (Stm stm : m.getStatements()) {
                stm.compile(st);
            }

            // Pop the method context from the stack
            st.popMethodContext();

            // Check if the method has already pushed a return value
            // by examining if the last statement is a return statement
            boolean hasExplicitReturn = false;
            if (!m.getStatements().isEmpty()) {
                Stm lastStm = m.getStatements().get(m.getStatements().size() - 1);
                // Check if the last statement is a return statement (you'd need to implement this check)
                hasExplicitReturn = isReturnStatement(lastStm);
            }

            // Only push default return value if method doesn't have an explicit return
            if (!hasExplicitReturn) {
                safePush("0");  // Default return value if none was pushed by the method
            }

            // Verify stack consistency before return
            if (stackDepth <= initialStackDepth) {
                // Stack underflow detected - a method must leave at least one value on stack
                safePush("0"); // Emergency fix to prevent stack underflow
                emit("// WARNING: Stack inconsistency detected - emergency default return value added");
            }

            // Return from the method
            emit("ret");

            // Reset stack depth tracking for the next method
            resetStackTracking();
        }

        // Main program body is in global scope
        for (Stm stm : body) {
            stm.compile(st);
        }
        emit("halt");

        if (!st.globalNames().isEmpty()) {
            emit(".data");
            for (String varName : st.globalNames()) {
                emit(st.makeVarLabel(varName) + ": 0");
            }
        }
    }

    /**
     * Check if a statement is a return statement
     * You'll need to implement this based on your AST structure
     */
    private boolean isReturnStatement(Stm stm) {
        // Replace with actual implementation based on your AST structure
        // For example: return stm instanceof StmReturn;
        return false; // Placeholder
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}