package compile;

import ast.Type;

/**
 * Stores information about a global variable.
 */
public class GlobalsInfo {
    public final Type type;
    private final int memAddr;

    public GlobalsInfo(Type t, int address) {
        this.memAddr = address;
        this.type = t;

    }

    /**
     * Gets the memory address of this global variable.
     *
     */
    public int getAddress() {
        return memAddr;
    }
}