package ast;

import compile.SymbolTable;
import java.util.List;

public class FunDecl extends MethodDecl {
    public final Type returnType;

    public FunDecl(Type returnType, String id, List<Formal> formals, List<VarDecl> locals, List<Stm> body) {
        super(id, formals, locals, body);
        this.returnType = returnType;
    }

    @Override
    public void compileBody(SymbolTable st) {
        for (Stm s : body) {
            s.compile(st);
        }

        emit("// Implicit return for function fallthrough");
        emit("push 0");

        int numParams = formals.size();
        int numLocals = locals.size();
        emit("push " + (numParams + numLocals));
        emit("ret");
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
