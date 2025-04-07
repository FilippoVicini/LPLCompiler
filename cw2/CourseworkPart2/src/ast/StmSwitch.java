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
        int caseCount = cases.size();
        String[] caseLabels = new String[caseCount + 1];
        for (int i = 0; i < caseCount; ++i) {
            caseLabels[i] = st.freshLabel("case_number_" + i);
        }
        caseLabels[caseCount] = st.freshLabel("default");
        String endLabel = st.freshLabel("switch_end");
        caseExp.compile(st);
        for (int i = 0; i < caseCount; ++i) {
            Case thisCase = cases.get(i);
            emit(caseLabels[i] + ":");
            emit("dup"); // duplicate the switch-value in case this case does NOT match
            emit("push " + thisCase.caseNumber);
            emit("sub", "test_z");
            emit("jumpi_z " + caseLabels[i+1]);
            emit("pop"); // this case matched; the switch-value is not needed any more
            thisCase.stm.compile(st);
            emit("jumpi " + endLabel);
        }
        emit(caseLabels[caseCount] + ":");
        emit("pop"); // the default case has been reached; the switch-value is not needed any more
        defaultCase.compile(st);
        emit(endLabel + ":");
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
