package ast;

import java.util.List;

public class MethodDecl extends VarDecl {
    private Type returnType;
    private String name;
    private List<VarDecl> params;
    private List<VarDecl> locals;
    private List<Stm> statements;

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