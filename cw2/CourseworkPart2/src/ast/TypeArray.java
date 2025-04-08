package ast;

public class TypeArray extends Type{
        private Type elementType;

        public TypeArray(Type elementType) {
            this.elementType = elementType;
        }

    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }
}
