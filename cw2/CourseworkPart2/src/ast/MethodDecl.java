package ast;

import java.util.List;

public class MethodDecl extends VarDecl {
    public Type returnType;
    public String name;
    private List<VarDecl> params;
    private List<VarDecl> locals;
    private List<Stm> statements;

    public String getName() {
        return name;
    }

    public MethodDecl(Type returnType, String name, List<VarDecl> params,
                      List<VarDecl> locals, List<Stm> statements) {
                super(returnType, name);

        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.locals = locals;
        this.statements = statements;
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}