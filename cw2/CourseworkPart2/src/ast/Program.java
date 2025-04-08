package ast;

import compile.SymbolTable;

import java.util.Collections;
import java.util.List;

public class Program extends AST {

    public final List<VarDecl> varDecls;
    public final List<Stm> body;
    public final List<MethodDecl> funcs;

    /**
     * Initialise a new Program AST.
     * @param varDecls the global variable declarations
     * @param body the statements in the main body of the program
     * @param funcs the method declarations
     */
    public Program(List<VarDecl> varDecls, List<Stm> body, List<MethodDecl> funcs) {
        this.varDecls = varDecls != null ? Collections.unmodifiableList(varDecls) : Collections.emptyList();
        this.body = body != null ? Collections.unmodifiableList(body) : Collections.emptyList();
        this.funcs = funcs != null ? Collections.unmodifiableList(funcs) : Collections.emptyList();
    }

    /**
     * Emit SSM assembly code for this program.
     */
    public void compile() {
        // Create symbol table and register all symbols
        SymbolTable st = new SymbolTable(this);

        // First emit method definitions
        for (MethodDecl m : funcs) {
            emit(st.methodLabel(m.getName()) + ":");
            emit("link 0");  // Initialize frame

            // Compile method body
            for (Stm stm : m.getStatements()) {
                stm.compile(st);
            }

            // Method return
            emit("unlink");
            emit("ret");
        }

        // Then emit main program body
        for (Stm stm : body) {
            stm.compile(st);
        }
        emit("halt");


        if (!st.globalNames().isEmpty()) {
            emit(".data");
            for (String varName : st.globalNames()) {
                emit(st.makeVarLabel(varName, null) + ": 0");
            }
        }
    }

    /**
     * Get a method by name
     * @param name the method name
     * @return the method declaration or null if not found
     */
    public MethodDecl getMethod(String name) {
        for (MethodDecl m : funcs) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}