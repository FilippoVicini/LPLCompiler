package compile;

import ast.Program;
import ast.Type;
import ast.VarDecl;
import ast.MethodDecl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SymbolTable {
    private Map<String, Type> globals;
    private  Map<String, MethodScope> methodScopes;
    private int freshNameCounter;

    // Inner class to represent method scope
    private static class MethodScope {
        final Type returnType;
        final Map<String, Type> parameters;
        final Map<String, Type> locals;

        MethodScope(Type returnType) {
            this.returnType = returnType;
            this.parameters = new HashMap<>();
            this.locals = new HashMap<>();
        }
    }

    /**
     * Initialise a new symbol table.
     * @param program the program
     */
    public SymbolTable(Program program) {
        this.freshNameCounter = 0;
        this.globals = new HashMap<>();
        this.methodScopes = new HashMap<>();

        // Initialize global variables
        for (VarDecl decl: program.varDecls) {
            Type duplicate = this.globals.put(decl.name, decl.type);
            if (duplicate != null) {
                throw new StaticAnalysisException("Duplicate global variable: " + decl.name);
            }
        }

        // Initialize methods
        for (MethodDecl method : program.funcs) {
            if (methodScopes.containsKey(method.name)) {
                throw new StaticAnalysisException("Duplicate method declaration: " + method.name);
            }
            MethodScope scope = new MethodScope(method.returnType);

            // Add parameters to method scope
            for (VarDecl param : method.getParams()) {
                if (scope.parameters.put(param.name, param.type) != null) {
                    throw new StaticAnalysisException("Duplicate parameter in method " + method.name + ": " + param.name);
                }
            }

            // Add local variables to method scope
            for (VarDecl local : method.getLocals()) {
                if (scope.locals.put(local.name, local.type) != null) {
                    throw new StaticAnalysisException("Duplicate local variable in method " + method.name + ": " + local.name);
                }
            }

            methodScopes.put(method.name, scope);
        }
    }

    /**
     * Add a method to the symbol table
     * @param name the method name
     * @param method the method declaration
     */
    public void addMethod(String name, MethodDecl method) {
        if (methodScopes.containsKey(name)) {
            throw new StaticAnalysisException("Duplicate method declaration: " + name);
        }
        methodScopes.put(name, new MethodScope(method.returnType));
    }

    /**
     * Add a parameter to a method's scope
     * @param methodName the method name
     * @param paramName the parameter name
     * @param type the parameter type
     */
    public void addParameter(String methodName, String paramName, Type type) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        if (scope.parameters.put(paramName, type) != null) {
            throw new StaticAnalysisException("Duplicate parameter in method " + methodName + ": " + paramName);
        }
    }

    /**
     * Add a local variable to a method's scope
     * @param methodName the method name
     * @param localName the local variable name
     * @param type the variable type
     */
    public void addLocal(String methodName, String localName, Type type) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        if (scope.parameters.containsKey(localName)) {
            throw new StaticAnalysisException("Local variable shadows parameter in method " + methodName + ": " + localName);
        }
        if (scope.locals.put(localName, type) != null) {
            throw new StaticAnalysisException("Duplicate local variable in method " + methodName + ": " + localName);
        }
    }
    /**
     * Get the type of a variable in the current method scope.
     * @param methodName the name of the current method
     * @param name the variable name
     * @return the type of the variable
     * @throws StaticAnalysisException if the variable is not found
     */

    public Type getVarType(String methodName, String name) {
        // Check if we're in a method scope
        if (methodName != null) {
            MethodScope scope = methodScopes.get(methodName);
            if (scope != null) {
                // First check parameters
                Type paramType = scope.parameters.get(name);
                if (paramType != null) {
                    return paramType;
                }
                // Then check locals
                Type localType = scope.locals.get(name);
                if (localType != null) {
                    return localType;
                }
            }
        }
        // Finally check globals
        Type globalType = globals.get(name);
        if (globalType != null) {
            return globalType;
        }
        throw new StaticAnalysisException("Undeclared variable: " + name);
    }

    /**
     * Get the type of a global variable.
     * @param name the variable name
     * @return the type
     * @throws StaticAnalysisException if no declaration is found
     */
    public Type getGlobalVarType(String name) {
        Type t = globals.get(name);
        if (t == null) {
            throw new StaticAnalysisException("Undeclared global variable: " + name);
        }
        return t;
    }

    /**
     * Check if a variable is a parameter in a method.
     * @param methodName the method name
     * @param varName the variable name
     * @return true if the variable is a parameter
     */
    public boolean isParameter(String methodName, String varName) {
        MethodScope scope = methodScopes.get(methodName);
        return scope != null && scope.parameters.containsKey(varName);
    }

    /**
     * Check if a variable is a local variable in a method.
     * @param methodName the method name
     * @param varName the variable name
     * @return true if the variable is a local variable
     */
    public boolean isLocal(String methodName, String varName) {
        MethodScope scope = methodScopes.get(methodName);
        return scope != null && scope.locals.containsKey(varName);
    }

    /**
     * Check if a variable is a global variable.
     * @param varName the variable name
     * @return true if the variable is global
     */
    public boolean isGlobal(String varName) {
        return globals.containsKey(varName);
    }

    /**
     * A set of all the global variable names.
     * @return the set of global variable names
     */
    public Set<String> globalNames() {
        return new HashSet<>(globals.keySet());
    }

    /**
     * Get the parameter names for a method.
     * @param methodName the method name
     * @return set of parameter names
     */
    public Set<String> getMethodParameterNames(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return new HashSet<>(scope.parameters.keySet());
    }

    /**
     * Get the local variable names for a method.
     * @param methodName the method name
     * @return set of local variable names
     */
    public Set<String> getMethodLocalNames(String methodName) {
        MethodScope scope = methodScopes.get(methodName);
        if (scope == null) {
            throw new StaticAnalysisException("Unknown method: " + methodName);
        }
        return new HashSet<>(scope.locals.keySet());
    }

    /**
     * Transform an LPL variable-name into an SSM label name.
     * @param sourceName the source name
     * @param methodName the current method name (null for globals)
     * @return appropriately formatted label name
     */
    public String makeVarLabel(String sourceName, String methodName) {  // Remove static keyword
        if (methodName != null) {
            MethodScope scope = methodScopes.get(methodName);
            if (scope != null) {
                if (scope.parameters.containsKey(sourceName)) {
                    return "$_param_" + methodName + "_" + sourceName;
                }
                if (scope.locals.containsKey(sourceName)) {
                    return "$_local_" + methodName + "_" + sourceName;
                }
            }
        }
        return "$_global_" + sourceName;
    }

    /**
     * Each call to this method will return a fresh name.
     * @param prefix a string to include as part of the generated name.
     * @return a fresh name which is prefixed with "$$_".
     */
    public String freshLabel(String prefix) {
        return "$$_" + prefix + "_" + (freshNameCounter++);
    }

    /**
     * Get the SSM label for a method.
     * @param name the method name
     * @return method name prefixed with "$_"
     */
    public String methodLabel(String name) {
        return "$_" + name;
    }
}