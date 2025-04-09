package ast;

import compile.Scope;
import compile.ScopeInfo;
import compile.SymbolTable;

import java.util.Collections;
import java.util.List;

public class Program extends AST {

    public final List<VarDecl> varDecls;
    public final List<Stm> body;
    public final List<MethodDecl> funcs;

    public Program(List<VarDecl> varDecls, List<Stm> body, List<MethodDecl> funcs) {
        this.varDecls = varDecls != null ? Collections.unmodifiableList(varDecls) : Collections.emptyList();
        this.body = body != null ? Collections.unmodifiableList(body) : Collections.emptyList();
        this.funcs = funcs != null ? Collections.unmodifiableList(funcs) : Collections.emptyList();
    }

    /**
     * Emit SSM assembly code for this program.
     */
    public void compile() {
        // Create symbol table
        SymbolTable st = new SymbolTable(this);

        // Process global variables
        for (VarDecl globalVar : varDecls) {
            ScopeInfo info = st.getVarScopeInfo(globalVar.name);
            if (info != null && info.getScope() == Scope.GLOBAL) {
                emit("push 0");
                emit("get_dp");
                emit("push " + info.getOffset());
                emit("add");
                emit("swap");
                emit("store");
            }
        }

        // Compile all statements in body
        for (Stm stm : body) {
            stm.compile(st);
        }
        emit("halt");

        // Compile all method definitions
        for (MethodDecl method : funcs) {
            String methodLabel = st.methodLabel(method.getName());
            emit(methodLabel + ":");

            st.pushMethodContext(method.getName());

            int numLocals = st.getLocalCountForCurrentMethod();
            if (numLocals > 0) {
                emit("malloc " + numLocals);
            }

            // Check if method has an explicit return statement at the end
            boolean hasExplicitReturn = false;
            List<Stm> methodBody = method.getStatements();
            if (!methodBody.isEmpty()) {
                Stm lastStm = methodBody.get(methodBody.size() - 1);
                if (lastStm instanceof StmReturn) {
                    hasExplicitReturn = true;
                }
            }

            // Compile the method body
            method.compileBody(st);

            st.popMethodContext();
        }

        // Add data segment for global variables
        if (!st.globalNames().isEmpty()) {
            emit(".data");
            for (String varName : st.globalNames()) {
                emit(st.makeVarLabel(varName) + ": 0");
            }
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}