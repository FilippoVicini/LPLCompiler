package compile;

import ast.*;
import compile.MethodsInfo;

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
    private String currentMethodName;
    private int freshNameCounter;

    /**
     * Initializes a new symbol table.
     * @param program the program to analyze
     * @throws StaticAnalysisException if there are duplicate declarations or other static errors
     */
    public Symbols(Program program) {
        this.freshNameCounter = 0;
        this.globals = new HashMap<>();
        this.methods = new HashMap<>();
        this.currentMethodName = null;

        initializeGlobalVariables(program.varDecls);
        initializeMethods(program.methods);
    }

    /**
     * Initializes global variables in the symbol table.
     *
     * @param varDecls the list of variable declarations
     * @throws StaticAnalysisException if there are duplicate global variables
     */
    private void initializeGlobalVariables(List<VarDecl> varDecls) {
        int nextGlobalAddress = 0;

        for (VarDecl decl: varDecls) {
            if (globals.containsKey(decl.name)) {
                throw new StaticAnalysisException("Duplicate global variable: " + decl.name);
            }
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
    private void initializeMethods(List<MethodDecl> met) {
        for (MethodDecl methodDecl : met) {
            String name = methodDecl.getMethodName();
            if (methods.containsKey(name)) {
                throw new StaticAnalysisException("Duplicate method name: " + name);
            }

            Type returnType = methodDecl instanceof FunDecl ?
                    ((FunDecl) methodDecl).returnType : null;

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
    public VarInfo getVarScopeInfo(String varName) {
        // Check local and parameter scope if in a method
        if (isInMethodScope()) {
            MethodsInfo currentMethod = methods.get(currentMethodName);

            // Check local variables first
            Integer localOffset = currentMethod.getLocalOffset(varName);
            if (localOffset != null) {
                return new VarInfo(INFO_LOCALS, localOffset, currentMethod.getLocalType(varName));
            }

            // Check parameters next
            Integer paramOffset = currentMethod.getParamOffset(varName);
            if (paramOffset != null) {
                return new VarInfo(INFO_PARAMETERS, paramOffset, currentMethod.getParamType(varName));
            }
        }

        // Check global scope
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
        if (!methods.containsKey(methodName)) {
            throw new StaticAnalysisException("Call to undefined method: " + methodName);
        }
        return "$_" + methodName;
    }

    /**
     * Enters the scope of a method.
     *
     * @param methodName the name of the method to enter
     * @throws StaticAnalysisException if the method is not defined
     */
    public void enterMethodScope(String methodName) {
        if (!methods.containsKey(methodName)) {
            throw new StaticAnalysisException(
                    "Internal Compiler Error: Entering scope for unknown method: " + methodName);
        }
        this.currentMethodName = methodName;
    }

    /**
     * Exits the current method scope.
     */
    public void exitMethodScope() {
        this.currentMethodName = null;
    }

    /**
     * Gets the parameter count for the current method.
     *
     * @return the number of parameters in the current method
     * @throws IllegalStateException if not in a method scope
     */
    public int getParamCountForCurrentMethod() {
        checkMethodScope("Cannot get parameter count outside of a method scope.");
        MethodsInfo currentMethod = methods.get(currentMethodName);
        return currentMethod.getParamCount();
    }

    /**
     * Gets the local variable count for the current method.
     *
     * @return the number of local variables in the current method
     * @throws IllegalStateException if not in a method scope
     */
    public int getLocalCountForCurrentMethod() {
        checkMethodScope("Cannot get local variable count outside of a method scope.");
        MethodsInfo currentMethod = methods.get(currentMethodName);
        return currentMethod.getLocalCount();
    }

    /**
     * Checks if the current context is within a method scope.
     *
     * @param errorMessage the error message to throw if not in a method scope
     * @throws IllegalStateException if not in a method scope
     */
    private void checkMethodScope(String errorMessage) {
        if (!isInMethodScope()) {
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * Gets the return type of a method.
     *
     * @param methodName the name of the method
     * @return the return type of the method
     * @throws StaticAnalysisException if the method is not defined
     */
    public Type getMethodReturnType(String methodName) {
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
    public boolean isInMethodScope() {
        return this.currentMethodName != null;
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