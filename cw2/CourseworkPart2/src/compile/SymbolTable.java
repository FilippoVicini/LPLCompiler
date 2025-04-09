package compile;

import ast.*;

import java.util.*;

/**
 * Symbol table that maintains information about declarations and scopes
 * for a compiler implementation.
 */
public class SymbolTable {

    public static final String INFO_GLOBALS = "GLOBAL";

    public static final String INFO_PARAMETERS = "PARAMETER";

    public static final String INFO_LOCALS = "LOCAL";


    private final Map<String, GlobalsInfo> globals;
    private final Map<String, MethodsInfo> methods;
    private String currMethodName;
    private int freshNameCounter;

    /**
     * Constructor
     */
    public SymbolTable(Program program) {
        this.freshNameCounter = 0;
        this.globals = new HashMap<>();
        this.methods = new HashMap<>();
        this.currMethodName = null;

        initGlobalVars(program.varDecls);
        initMethods(program.methods);
    }

    /**
     * Initializes global variables in the symbol table.
     *
     */
    private void initGlobalVars(List<VarDecl> decls) {
        int nextGlobalAddress = 0;

        for (VarDecl decl: decls) {
            GlobalsInfo info = new GlobalsInfo(decl.type, nextGlobalAddress);
            this.globals.put(decl.name, info);
            nextGlobalAddress += 4;
        }
    }

    /**
     * Initializes methods in the symbol table.
     *
     *
     * @throws StaticAnalysisException if there are duplicate method names
     */
    private void initMethods(List<MethodDecl> met) {
        for (MethodDecl methodDecl : met) {
            String name = methodDecl.getMethodName();

            // Use the returnType field directly from MethodDecl
            Type returnType = methodDecl.returnType;

            MethodsInfo info = new MethodsInfo(name, returnType, methodDecl.formals, methodDecl.locals);
            methods.put(name, info);
        }
    }

    /**
     * Gets the scope information for a variable.
     *
     * @param varName the name of the variable
     * @return scope information for the variable
     * @throws StaticAnalysisException if the variable is not declared
     */
    public VarInfo getVarInfo(String varName) {
        // Check local and parameter scope if in a method
        if (isInMethod()) {
            MethodsInfo currentMethod = methods.get(currMethodName);


            Integer localOffset = currentMethod.getLocalOffset(varName);
            if (localOffset != null) {
                return new VarInfo(INFO_LOCALS, localOffset, currentMethod.getLocalType(varName));
            }


            Integer paramOffset = currentMethod.getParamOffset(varName);
            if (paramOffset != null) {
                return new VarInfo(INFO_PARAMETERS, paramOffset, currentMethod.getParamType(varName));
            }
        }


        GlobalsInfo globalInfo = globals.get(varName);
        if (globalInfo != null) {
            return new VarInfo(INFO_GLOBALS, globalInfo.getAddress(), globalInfo.type);
        }

        throw new StaticAnalysisException("Undeclared variable: " + varName);
    }

    /**
     * Gets the method label for code generation.
     *
     * @param methodName the name of the method
     * @return the label for the method
     * @throws StaticAnalysisException if the method is not defined
     */
    public String getMethodLabel(String methodName) {
        return "$_" + methodName;
    }

    /**
     * Enters the scope of a method.
     *
     * @param methodName the name of the method to enter
     * @throws StaticAnalysisException if the method is not defined
     */
    public void enterMethodScope(String methodName) {
        this.currMethodName = methodName;
    }

    /**
     * Exits the current method scope.
     */
    public void exitMethodScope() {
        this.currMethodName = null;
    }

    /**
     * Gets the parameter count for the current method.
     *
     * @return the number of parameters in the current method
     * @throws IllegalStateException if not in a method scope
     */
    public int getParamCountForCurrentMethod() {
        MethodsInfo currentMethod = methods.get(currMethodName);
        return currentMethod.getParamCount();
    }

    /**
     * Gets the local variable count for the current method.
     *
     * @return the number of local variables in the current method
     * @throws IllegalStateException if not in a method scope
     */
    public int getLocalCountForCurrentMethod() {
        MethodsInfo currentMethod = methods.get(currMethodName);
        return currentMethod.getLocalCount();
    }

    /**
     * Gets the return type of a method.
     *
     * @param methodName the name of the method
     * @return the return type of the method
     * @throws StaticAnalysisException if the method is not defined
     */
    public Type getMethodRetType(String methodName) {
        MethodsInfo info = methods.get(methodName);
        if (info == null) {
            throw new StaticAnalysisException("Call to undefined method: " + methodName);
        }
        return info.returnType;
    }

    /**
     * Checks if currently in a method scope.
     *
     * @return true if in a method scope, false otherwise
     */
    public boolean isInMethod() {
        return this.currMethodName != null;
    }

    /**
     * Generates a fresh label with a prefix.
     *
     * @param prefix a prefix for the generated label
     * @return a fresh label starting with "$$_"
     */
    public String freshLabel(String prefix) {
        return "$$_" + prefix + "_" + (freshNameCounter++);
    }

    /**
     * Exception class for static analysis errors.
     */
    public static class StaticAnalysisException extends RuntimeException {
        public StaticAnalysisException(String message) {
            super(message);
        }
    }
}