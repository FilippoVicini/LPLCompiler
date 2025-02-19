package ast;

import compile.StaticAnalysisException;
import compile.SymbolTable;

import java.util.*;

public class StmSwitch extends Stm {

    public final Exp caseExp;
    public final Stm defaultCase;
    public final List<Case> cases;

    public StmSwitch(Exp caseExp, Stm defaultCase, List<Case> cases) {
        this.caseExp = caseExp;
        this.defaultCase = defaultCase;
        this.cases = cases;
    }

    @Override
    public void compile(SymbolTable st) {
        // To Be Completed
    }

    public static class Case {

        public final int caseNumber;
        public final Stm stm;

        public Case(int caseNumber, Stm stm) {
            this.caseNumber = caseNumber;
            this.stm = stm;
        }
    }

    @Override
    public <T> T accept(ast.util.Visitor<T> visitor) { return visitor.visit(this); }

}
