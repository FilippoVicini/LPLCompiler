package ast;

import compile.SymbolTable;

public class StmAssign extends Stm {

    public final String varName;
    public final Exp exp;

    public StmAssign(String varName, Exp exp) {
        this.varName = varName;
        this.exp = exp;
    }

    @Override
    public void compile(SymbolTable st) {
        // Try to find the variable in the current method context
        String methodName = st.getCurrentMethodFromContextStack();

        if (methodName != null) {
            if (st.getMethodParameterNames(methodName).contains(varName) ||
                    st.getMethodLocalNames(methodName).contains(varName)) {

                // Calculate the address first
                emit("get_sp");  // Get current stack pointer

                int offset;
                if (st.getMethodParameterNames(methodName).contains(varName)) {
                    // It's a parameter
                    offset = getParameterOffset(st, methodName, varName);
                } else {
                    // It's a local variable
                    offset = getLocalOffset(st, methodName, varName);
                }

                emit("push " + offset);
                emit("add");     // Calculate address: SP + offset

                // Evaluate the expression - result will be on top of stack
                exp.compile(st);

                // Store value to the calculated address
                emit("store");
                return;
            }
        }

        // Not found in method scope, must be a global variable
        if (st.globalNames().contains(varName)) {
            // Evaluate the expression first
            exp.compile(st);
            // Store to absolute memory address
            emit("storei " + st.makeVarLabel(varName));
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