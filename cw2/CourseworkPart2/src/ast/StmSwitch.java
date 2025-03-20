package ast;

import compile.SymbolTable;

import java.util.List;

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

        String caseLabel = "$_case_";
        String testLabel = "$_test_e_";
        String endLabel = "$_e_";
        String defaultLabel = "$_deg_";


        emit("jumpi " + testLabel);

        for (Case a : cases) {

            if (a.caseNumber < 0) {
              emit(caseLabel + "_" + Math.abs(a.caseNumber) + ":");
            } else {
                emit(caseLabel + a.caseNumber + ":");
            }
            a.stm.compile(st);
            emit("jumpi " + endLabel);
        }

        emit(testLabel + ":");
        caseExp.compile(st);
        for (Case a : cases) {
            emit("push " + a.caseNumber);
            emit("sub");
            emit("dup");

            if (a.caseNumber < 0) {
                emit("jumpi_z"+caseLabel+"_"+Math.abs(a.caseNumber));
            } else {
                emit("jumpi_z"+caseLabel + a.caseNumber);
            }

            emit("push " + a.caseNumber);
            emit("add");
        }


        emit("jumpi " + defaultLabel);


        emit(defaultLabel + ":");
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
