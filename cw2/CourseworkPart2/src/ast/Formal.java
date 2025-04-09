package ast;

public class Formal extends AST {
    public final Type type;
    public final String name;

    public Formal(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
