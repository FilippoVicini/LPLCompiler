package compile;

import ast.Program;
import ast.Type;
import ast.VarDecl;
import ast.MethodDecl;
import compile.Scope;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class SymbolTable {
    private Map<String, Type> globals;
    private Map<String, MethodScope> methodScopes;
    private int freshNameCounter;
    private Stack<String> methodContextStack; // Stack to track method context
    private int recursionDepth = 0;
    private static final int MAX_RECURSION_DEPTH = 1000;

    private static class MethodScope {
        final Type returnType;
        final Map<String, Type> parameters;
        final Map<String, Type> locals;
        final Map<String, Integer> parameterIndices; // Cache parameter indices for faster lookup
        final Map<String, Integer> localIndices;    // Cache local indices for faster lookup

        MethodScope(Type returnType) {
            this.returnType = returnType;
            this.parameters = new HashMap<>();
            this.locals = new HashMap<>();
            this.parameterIndices = new HashMap<>();
            this.localIndices = new HashMap<>();
        }
    }

    public SymbolTable(Program program) {
        this.freshNameCounter = 0;
        this.globals = new HashMap<>();
        this.methodScopes = new HashMap<>();
        this.methodContextStack = new Stack<>();

        for (VarDecl decl: program.varDecls) {
            this.globals.put(decl.name, decl.type);
        }

        for (MethodDecl method : program.funcs) {
            if (methodScopes.containsKey(method.name)) {
                throw new StaticAnalysisException("Duplicate method declaration: " + method.name);
            }
            MethodScope scope = new MethodScope(method.returnType);

            int paramIndex = 0;
            for (VarDecl param : method.getParams()) {
                if (scope.parameters.put(param.name, param.type) != null) {
                    throw new StaticAnalysisException("Duplicate parameter in method " + method.name + ": " + param.name);
                }
                scope.parameterIndices.put(param.name, paramIndex++);
            }


            int localIndex = 0;
            for (VarDecl local : method.getLocals()) {
                if (scope.locals.put(local.name, local.type) != null) {
                    throw new StaticAnalysisException("Duplicate local variable in method " + method.name + ": " + local.name);
                }
                scope.localIndices.put(local.name, localIndex++);
            }

            methodScopes.put(method.name, scope);
        }
    }


    public void pushMethodContext(String methodName) {
        recursionDepth++;
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            throw new StaticAnalysisException("Maximum recursion depth exceeded: " + recursionDepth);
        }

        if (!methodScopes.containsKey(methodName)) {
            throw new StaticAnalysisException("Attempting to enter unknown method context: " + methodName);
        }
        methodContextStack.push(methodName);
    }

    /**
     * Pop the current method context from the stack
     */
    public String popMethodContext() {
        recursionDepth--;
        if (methodContextStack.isEmpty()) {
            throw new StaticAnalysisException("Attempting to exit method context when no context is active");
        }
        return methodContextStack.pop();
    }

    /**
     * Get the current method context from the stack without removing it
     */
    public String getCurrentMethodFromContextStack() {
        if (methodContextStack.isEmpty()) {
            return null;
        }
        return methodContextStack.peek();
    }


    /**
     * Get variable scope information including scope type and offset
     */
    public ScopeInfo getVarScopeInfo(String varName) {
        // First check if we're in a method scope
        if (!methodContextStack.isEmpty()) {
            String methodName = methodContextStack.peek();
            MethodScope scope = methodScopes.get(methodName);

            // Check parameters
            if (scope.parameters.containsKey(varName)) {
                int index = scope.parameterIndices.get(varName);
                return new ScopeInfo(Scope.PARAMETER, index);
            }

            // Check locals
            if (scope.locals.containsKey(varName)) {
                int index = scope.localIndices.get(varName);
                return new ScopeInfo(Scope.LOCAL, index);
            }
        }

        // Check global variables
        if (globals.containsKey(varName)) {
            // For global variables, the offset is determined by the order in which they
            // are declared, but it's stored at a global data segment
            return new ScopeInfo(Scope.GLOBAL, 0); // Offset will be determined by label, not by direct index
        }

        return null; // Variable not found in any scope
    }

    /**
     * Get all global variable names
     */
    public Set<String> globalNames() {
        return new HashSet<>(globals.keySet());
    }

    /**
     * Get all parameter names for a method
     */
    public Set<String> getMethodParameterNames(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return new HashSet<>(scope.parameters.keySet());
    }

    /**
     * Get all local variable names for a method
     */
    public Set<String> getMethodLocalNames(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return new HashSet<>(scope.locals.keySet());
    }

    /**
     * Get number of parameters for a method
     */
    public int getParameterCount(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return scope.parameters.size();
    }

    /**
     * Get number of local variables for a method
     */
    public int getLocalCount(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return scope.locals.size();
    }

    /**
     * Get the number of local variables for the current method context
     */
    public int getLocalCountForCurrentMethod() {
        if (methodContextStack.isEmpty()) {
            return 0; // No method context active
        }
        String currentMethod = methodContextStack.peek();
        return getLocalCount(currentMethod);
    }


    /**
     * Create a variable label for code generation
     */
    public static String makeVarLabel(String sourceName) {
        return "$_" + sourceName;
    }

    /**
     * Generate a fresh unique label with prefix
     */
    public String freshLabel(String prefix) {
        return "$$_" + prefix + "_" + (freshNameCounter++);
    }

    /**
     * Create a method label for code generation
     */
    public String methodLabel(String name) {
        return "$_" + name;
    }
}