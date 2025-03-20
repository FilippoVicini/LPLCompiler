package compile;

import ast.Program;
import ast.Type;
import ast.VarDecl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SymbolTable {

    private Map<String, Type> globals;

    private int freshNameCounter;

    /**
     * Initialise a new symbol table.
     * @param program the program
     */
    public SymbolTable(Program program) {
        this.freshNameCounter = 0;
        this.globals = new HashMap<>();
        for (VarDecl decl: program.varDecls) {
            Type duplicate = this.globals.put(decl.name, decl.type);
            if (duplicate != null) {
                throw new StaticAnalysisException("Duplicate global variable: " + decl.name);
            }
        }
    }

    /**
     * The type of the currently effective declaration of the named variable.
     * @param name the variable name
     * @return the type
     * @throws StaticAnalysisException if no declaration is found
     */
    public Type getVarType(String name) {
        Type t = globals.get(name);
        if (t == null) {
            throw new StaticAnalysisException("Undeclared variable: " + name);
        }
        return t;
    }

    /**
     * A set of all the global variable names.
     * @return the set of global variable names
     */
    public Set<String> globalNames() {
        return new HashSet<>(globals.keySet());
    }

    /**
     * Transform an LPL variable-name into an SSM label name.
     * @param sourceName
     * @return sourceName prefixed with "$_"
     */
    public static String makeVarLabel(String sourceName) {
        return "$_" + sourceName;
    }

    /**
     * Each call to this method will return a fresh name which is
     * guaranteed not to clash with any name returned by makeVarLabel(x),
     * where x is any LPL identifier.
     * @param prefix a string to include as part of the generated name.
     * @return a fresh name which is prefixed with "$$_".
     */
    public String freshLabel(String prefix) {
        return "$$_" + prefix + "_" + (freshNameCounter++);
    }
}
