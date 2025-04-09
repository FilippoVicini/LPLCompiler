package compile;

import ast.Formal;
import ast.Type;
import ast.VarDecl;

import java.util.*;

/**
 * Stores information about a method declaration.
 */
public class MethodsInfo {
    private final Map<String, Type> pTypes;
    private final Map<String, Type> lTypes;

    public final String methodName;
    public final Type returnType;
    private final List<Formal> params;
    private final List<VarDecl> lcls;
    private final Map<String, Integer> paramOffsets;
    private final Map<String, Integer> localOffsets;

    public MethodsInfo(String name, Type retType, List<Formal> params, List<VarDecl> localDecls) {
        this.methodName = name;
        this.returnType = retType;
        this.params = Collections.unmodifiableList(new ArrayList<>(params));
        this.lcls = Collections.unmodifiableList(new ArrayList<>(localDecls));

        this.paramOffsets = new HashMap<>();
        this.localOffsets = new HashMap<>();
        this.pTypes = new HashMap<>();
        this.lTypes = new HashMap<>();

        initLocals(localDecls);
        initParams(params);
    }

    private void initLocals(List<VarDecl> localDecls) {
        int localOffsetCounter = 1;
        for (VarDecl l : localDecls) {
            localOffsets.put(l.name, localOffsetCounter++);
            lTypes.put(l.name, l.type);
        }
    }

    private void initParams(List<Formal> params) {
        int paramOffsetCounter = 1;
        for (Formal p : params) {
            paramOffsets.put(p.name, paramOffsetCounter++);
            pTypes.put(p.name, p.type);
        }
    }


    public Type getParamType(String n) {
        return pTypes.get(n);
    }

    public Type getLocalType(String n) {
        return lTypes.get(n);
    }

    public Integer getParamOffset(String n) {
        return paramOffsets.get(n);
    }

    public Integer getLocalOffset(String n) {
        return localOffsets.get(n);
    }

    public int getParamCount() {
        return params.size();
    }

    public int getLocalCount() {
        return lcls.size();
    }
}