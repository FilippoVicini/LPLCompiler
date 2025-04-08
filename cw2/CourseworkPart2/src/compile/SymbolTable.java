package compile;

import ast.Program;
import ast.Type;
import ast.VarDecl;
import ast.MethodDecl;

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

        // Initialize global variables
        for (VarDecl decl: program.varDecls) {
            this.globals.put(decl.name, decl.type);
        }

        for (MethodDecl method : program.funcs) {
            if (methodScopes.containsKey(method.name)) {
                throw new StaticAnalysisException("Duplicate method declaration: " + method.name);
            }
            MethodScope scope = new MethodScope(method.returnType);

            // Process parameters with indices
            int paramIndex = 0;
            for (VarDecl param : method.getParams()) {
                if (scope.parameters.put(param.name, param.type) != null) {
                    throw new StaticAnalysisException("Duplicate parameter in method " + method.name + ": " + param.name);
                }
                scope.parameterIndices.put(param.name, paramIndex++);
            }

            // Process local variables with indices
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

    /**
     * Push a method context onto the stack
     */
    public void pushMethodContext(String methodName) {
        if (!methodScopes.containsKey(methodName)) {
            throw new StaticAnalysisException("Attempting to enter unknown method context: " + methodName);
        }
        methodContextStack.push(methodName);
    }

    /**
     * Pop the current method context from the stack
     */
    public String popMethodContext() {
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
     * Get the parameter index directly from the cached map for better performance
     */
    public int getParameterIndex(String methodName, String paramName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        Integer index = scope.parameterIndices.get(paramName);
        if (index == null) {
            throw new StaticAnalysisException("Unknown parameter '" + paramName + "' in method '" + methodName + "'");
        }
        return index;
    }

    /**
     * Get the local variable index directly from the cached map for better performance
     */
    public int getLocalIndex(String methodName, String localName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        Integer index = scope.localIndices.get(localName);
        if (index == null) {
            throw new StaticAnalysisException("Unknown local variable '" + localName + "' in method '" + methodName + "'");
        }
        return index;
    }

    public Type getVarType(String name) {
        // First check if we're in a method scope
        if (!methodContextStack.isEmpty()) {
            String methodName = methodContextStack.peek();
            MethodScope scope = methodScopes.get(methodName);

            // Check parameters
            if (scope.parameters.containsKey(name)) {
                return scope.parameters.get(name);
            }

            // Check locals
            if (scope.locals.containsKey(name)) {
                return scope.locals.get(name);
            }
        }

        // Fall back to global variables
        Type t = globals.get(name);
        if (t == null) {
            throw new StaticAnalysisException("Undeclared variable: " + name);
        }
        return t;
    }

    public Set<String> globalNames() {
        return new HashSet<>(globals.keySet());
    }

    public Set<String> getMethodParameterNames(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return new HashSet<>(scope.parameters.keySet());
    }

    public Set<String> getMethodLocalNames(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return new HashSet<>(scope.locals.keySet());
    }

    public int getParameterCount(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return scope.parameters.size();
    }

    public int getLocalCount(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return scope.locals.size();
    }

    public static String makeVarLabel(String sourceName) {
        return "$_" + sourceName;
    }

    public String freshLabel(String prefix) {
        return "$$_" + prefix + "_" + (freshNameCounter++);
    }

    public String methodLabel(String name) {
        return "$_" + name;
    }
}