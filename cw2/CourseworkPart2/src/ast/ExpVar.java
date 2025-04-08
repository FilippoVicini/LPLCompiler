package ast;

import compile.SymbolTable;

public class ExpVar extends Exp {
    public final String varName;

    public ExpVar(String varName) {
        this.varName = varName;
    }

    @Override
    public void compile(SymbolTable st) {
        // Try to find the variable in the current method context
        String methodName = st.getCurrentMethodFromContextStack();

        if (methodName != null) {
            // Check if it's a parameter
            if (st.getMethodParameterNames(methodName).contains(varName)) {
                // For parameters, we need to access them on the stack
                // We can use load with the appropriate offset from SP
                emit("get_sp");
                int offset = getParameterOffset(st, methodName, varName);
                emit("push " + offset);
                emit("add");
                emit("load");
                return;
            }

            // Check if it's a local variable
            if (st.getMethodLocalNames(methodName).contains(varName)) {
                // For locals, we need to access them on the stack relative to SP
                emit("get_sp");  // Get current stack pointer
                int offset = getLocalOffset(st, methodName, varName);
                emit("push " + offset);
                emit("add");
                emit("load");    // Load value from calculated address
                return;
            }
        }

        // Not found in method scope, check if it's a global variable
        if (st.globalNames().contains(varName)) {
            // Load from absolute memory address
            emit("loadi " + st.makeVarLabel(varName));
            return;
        }

        // Variable not found in any scope
        throw new compile.StaticAnalysisException("Undeclared variable: " + varName);
    }

    /**
     * Calculate the offset for a parameter relative to the stack pointer
     */
    private int getParameterOffset(SymbolTable st, String methodName, String varName) {
        // Parameters are accessed from the call stack
        int paramCount = st.getMethodParameterNames(methodName).size();

        // Find parameter index
        int paramIndex = 0;
        for (String param : st.getMethodParameterNames(methodName)) {
            if (param.equals(varName)) {
                break;
            }
            paramIndex++;
        }

        // Calculate offset from SP
        // Here we need to account for all stack elements between SP and the parameter
        return paramCount - paramIndex - 1;
    }

    /**
     * Calculate the offset for a local variable relative to the stack pointer
     */
    private int getLocalOffset(SymbolTable st, String methodName, String varName) {
        // Locals are accessed from the stack
        int paramCount = st.getMethodParameterNames(methodName).size();

        // Find local index
        int localIndex = 0;
        for (String local : st.getMethodLocalNames(methodName)) {
            if (local.equals(varName)) {
                break;
            }
            localIndex++;
        }

        // Calculate offset from SP
        // Locals are stored before parameters in the stack
        return paramCount + localIndex;
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) {
        return visitor.visit(this);
    }
}