package compile;

public class ScopeInfo {
    private final Scope scope;
    private final int offset;

    public ScopeInfo(Scope scope, int offset) {
        this.scope = scope;
        this.offset = offset;
    }

    public Scope getScope() {
        return scope;
    }

    public int getOffset() {
        return offset;
    }
}