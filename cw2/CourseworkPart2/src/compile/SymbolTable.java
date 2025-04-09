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
    private String currMethodName;
    private final Map<String, MethodsInfo> methods;
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
        int nextAddr = 0;

        for (VarDecl decl: decls) {
            GlobalsInfo info = new GlobalsInfo(decl.type, nextAddr);
            this.globals.put(decl.name, info);
            nextAddr += 4;
        }
    }

    /**
     * Initializes methods in the symbol table.
     *
     */
    private void initMethods(List<MethodDecl> met) {
        for (MethodDecl decl : met) {
            String name = decl.getMethodName();

            Type returnType = decl.returnType;

            MethodsInfo info = new MethodsInfo(name, returnType, decl.formals, decl.locals);
            methods.put(name, info);
        }
    }

    /**
     * Gets the scope information for a variable.
     *
     */
    public VarInfo getVarI(String varName) {
        if (isInMethod()) {
            MethodsInfo currentMethod = methods.get(currMethodName);


            Integer localOff = currentMethod.getLocalOffset(varName);
            if (localOff != null) {
                return new VarInfo(INFO_LOCALS, localOff, currentMethod.getLocalType(varName));
            }


            Integer paramOff = currentMethod.getParamOffset(varName);
            if (paramOff != null) {
                return new VarInfo(INFO_PARAMETERS, paramOff, currentMethod.getParamType(varName));
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
     */
    public String getMethodLabel(String methodName) {
        return "$_" + methodName;
    }

    /**
     * Enters the scope of a method.
     *
     */
    public void enterMethod(String methodName) {
        this.currMethodName = methodName;
    }


    /**
     * Gets the parameter count for the current method.
     *
     */
    public int getPCountCurrMethod() {
        MethodsInfo currentMethod = methods.get(currMethodName);
        return currentMethod.getParamCount();
    }

    /**
     * Gets the local variable count for the current method.
     *
     */
    public int getLCountCurrMethod() {
        MethodsInfo currentMethod = methods.get(currMethodName);
        return currentMethod.getLocalCount();
    }

    /**
     * Exits the current method scope.
     */
    public void exitMethod() {
        this.currMethodName = null;
    }

    /**
     * Gets the return type of a method.
     *
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
     */
    public boolean isInMethod() {
        return this.currMethodName != null;
    }

    /**
     * Generates a fresh label with a prefix.
     *
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