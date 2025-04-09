package compile;

import ast.Type;

/**
 * Holds scope information for a variable.
 */
public class VarInfo {
    public final String varInfo;

    public final Type type;
    public final int off;

    public VarInfo(String varInfo, int offset, Type type) {
        this.varInfo = varInfo;
        this.off = offset;
        this.type = type;
    }
    public String getVarI() { return varInfo; }
    public Type getType() { return type; }
    public int getOffset() { return off; }

}