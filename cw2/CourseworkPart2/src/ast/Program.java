package ast;

import compile.VarInfo;
import compile.SymbolTable;

import java.util.Collections;
import java.util.List;

import static compile.SymbolTable.INFO_GLOBALS;

public class Program extends AST {

    public final List<VarDecl> varDecls;
    public final List<Stm> body;
    public final List<MethodDecl> methods;

    public Program(List<VarDecl> varDecls, List<Stm> body, List<MethodDecl> methods) {
        this.varDecls = Collections.unmodifiableList(varDecls);
        this.body = Collections.unmodifiableList(body);
        this.methods = Collections.unmodifiableList(methods);
    }

    /**
     * Emit SSM assembly code for this program.
     */
    public void compile() {
        SymbolTable st = new SymbolTable(this);

        for (VarDecl globalVar : varDecls) {
            VarInfo info = st.getVarScopeInfo(globalVar.name);
            if (info != null && info.getVarInfo() == INFO_GLOBALS) {
                emit("push 0");
                emit("get_dp");
                emit("push " + info.getOffset());
                emit("add");
                emit("swap");
                emit("store");
            }
        }

        for (Stm stm : body) {
            stm.compile(st);
        }
        emit("halt");

        for (MethodDecl method : methods) {
            String methodLabel = st.getMethodLabel(method.getMethodName());
            emit(methodLabel + ":");

            st.enterMethodScope(method.getMethodName());

            int numLocals = st.getLocalCountForCurrentMethod();
            if (numLocals > 0) {
                emit("salloc " + numLocals);
            }

            method.compileBody(st);

            st.exitMethodScope();
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
