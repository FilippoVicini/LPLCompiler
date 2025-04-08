package ast;

import compile.SymbolTable;

public class ExpVar extends Exp {
    private String currentMethod; // Track current method context
    public final String varName;

    public ExpVar(String varName) {
        this.varName = varName;
    }

    /**
     * Sets the current method context for variable resolution
     * @param methodName the name of the method being compiled
     */
    public void setMethodContext(String methodName) {
        this.currentMethod = methodName;
    }

    @Override
    public void compile(SymbolTable st) {
        // Get the type to ensure variable exists and determine scope
        st.getVarType(currentMethod, varName);

        // Generate the appropriate load instruction based on variable scope
        if (st.isGlobal(varName)) {
            // Global variable access
            emit("loadi " + st.makeVarLabel(varName, null));
        } else if (st.isLocal(currentMethod, varName)) {
            // Local variable access
            emit("load " + getLocalOffset(st, varName));
        } else if (st.isParameter(currentMethod, varName)) {
            // Parameter access - parameters are above the frame pointer
            emit("loadl " + getParameterOffset(st, varName));
        } else {
            // This shouldn't happen as getVarType would have thrown an exception
            throw new RuntimeException("Unexpected variable scope for: " + varName);
        }
    }

    /**
     * Calculate the offset for a local variable relative to the frame pointer
     */
    private int getLocalOffset(SymbolTable st, String varName) {
        int offset = 0;
        for (String local : st.getMethodLocalNames(currentMethod)) {
            if (local.equals(varName)) {
                return offset;
            }
            offset++;
        }
        throw new RuntimeException("Local variable not found: " + varName);
    }

    /**
     * Calculate the offset for a parameter relative to the frame pointer
     */
    private int getParameterOffset(SymbolTable st, String varName) {
        int offset = 2;
        for (String param : st.getMethodParameterNames(currentMethod)) {
            if (param.equals(varName)) {
                return offset;
            }
            offset++;
        }
        throw new RuntimeException("Parameter not found: " + varName);
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return varName;
    }
}