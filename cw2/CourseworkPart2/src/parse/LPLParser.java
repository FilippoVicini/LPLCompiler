package parse;

import ast.*;
import sbnf.ParseException;
import sbnf.lex.Lexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** Parse an LPL program and build its AST.  */
public class LPLParser {

    public static final String LPL_SBNF_FILE = "data/LPL.sbnf";
    private Lexer lex;

    /**
     * Initialise a new LPL parser.
     */
    public LPLParser() {
        lex = new Lexer(LPL_SBNF_FILE);
    }

    /** Parse an LPL source file and return its AST.
     *
     * @param sourcePath a path to an LPL source file
     * @return the Program AST for the parsed LPL program
     * @throws ParseException if the source file contains syntax errors
     * @throws IOException
     */
    public Program parse(String sourcePath) throws IOException {
        lex.readFile(sourcePath);
        lex.next();
        Program prog = Program();
        if (!lex.tok().isType("EOF")) {
            throw new ParseException(lex.tok(), "EOF");
        }
        return prog;
    }

    private Program Program() {
        List<VarDecl> globals = new LinkedList<>();
        List<Stm> body = new LinkedList<>();
        lex.eat("BEGIN");
        while (lex.tok().isType("INT_TYPE")) {
            globals.add(GlobalVarDecl());
        }
        while (!lex.tok().isType("END")) {
            body.add(Stm());
        }
        lex.eat("END");
        return new Program(globals, body);
    }

    private List<VarDecl> GlobalVarDeclKleene() {
        switch(lex.tok().type) {
            case "INT_TYPE": {
                VarDecl varDecl = GlobalVarDecl();
                List<VarDecl> rest = GlobalVarDeclKleene();
                rest.add(0, varDecl);
                return rest;
            }
            default:
                return new ArrayList<>();
        }
    }

    private Type Type() {
        lex.eat("INT_TYPE");
        Type t = new TypeInt();
        return t;
    }

    private VarDecl GlobalVarDecl() {
        Type t = Type();
        String id = lex.eat("ID");
        lex.eat("SEMIC");
        return new VarDecl(t, id);
    }

    private Stm Stm() {
        switch (lex.tok().type) {
            case "ID": {
                String id = lex.eat("ID");
                lex.eat("ASSIGN");
                Exp valueExpression = Exp();
                lex.eat("SEMIC");
                return new StmAssign(id, valueExpression);
            }
            case "IF": {
                lex.next();
                lex.eat("LBR");
                Exp e = Exp();
                lex.eat("RBR");
                Stm trueBranch = Stm();
                lex.eat("ELSE");
                Stm falseBranch = Stm();
                return new StmIf(e, trueBranch, falseBranch);
            }
            case "WHILE": {
                lex.next();
                lex.eat("LBR");
                Exp e = Exp();
                lex.eat("RBR");
                return new StmWhile(e, Stm());
            }
            case "PRINT": {
                lex.next();
                Exp e = Exp();
                lex.eat("SEMIC");
                return new StmPrint(e);
            }
            case "PRINTLN": {
                lex.next();
                Exp e = Exp();
                lex.eat("SEMIC");
                return new StmPrintln(e);
            }
            case "PRINTCH": {
                lex.next();
                Exp e = Exp();
                lex.eat("SEMIC");
                return new StmPrintChar(e);
            }
            case "NEWLINE": {
                lex.next();
                lex.eat("SEMIC");
                return new StmNewline();
            }
            case "LCBR": {
                lex.next();
                List<Stm> stms = new LinkedList<>();
                while (!lex.tok().isType("RCBR")) {
                    stms.add(Stm());
                }
                lex.eat("RCBR");
                return new StmBlock(stms);
            }
            case "SWITCH": {
                lex.next();
                lex.eat("LBR");
                Exp caseExp = Exp();
                lex.eat("RBR");
                lex.eat("LCBR");
                List<StmSwitch.Case> cases = new ArrayList<>();
                while (!lex.tok().isType("DEFAULT")) {
                    cases.add(SwitchCase());
                }
                lex.eat("DEFAULT");
                lex.eat("COLON");
                Stm defaultCase = Stm();
                lex.eat("RCBR");
                return new StmSwitch(caseExp, defaultCase, cases);
            }
            default:
                throw new ParseException(lex.tok(), "ID", "IF", "WHILE", "PRINT", "PRINTLN", "PRINTCH", "NEWLINE", "LCBR", "SWITCH");
        }
    }

    private StmSwitch.Case SwitchCase() {
        lex.eat("CASE");
        int caseNumber = SignedInt();
        lex.eat("COLON");
        Stm stm = Stm();
        return new StmSwitch.Case(caseNumber, stm);
    }

    private int SignedInt() {
        return OptionalSign() * Integer.parseInt(lex.eat("INTLIT"));
    }

    private int OptionalSign() {
        switch (lex.tok().type) {
            case "MINUS":
                lex.next();
                return -1;
            default:
                return 1;
        }
    }

    private Exp Exp() {
        Exp e1 = SimpleExp();
        return OperatorClause(e1);
    }

    private Exp SimpleExp() {
        switch (lex.tok().type) {
            case "ID": {
                String id = lex.tok().image;
                lex.next();
                return new ExpVar(id);
            }
            case "MINUS", "INTLIT": {
                return new ExpInt(SignedInt());
            }
            case "NOT": {
                lex.next();
                return new ExpNot(SimpleExp());
            }
            case "LBR": {
                lex.next();
                Exp e = Exp();
                lex.eat("RBR");
                return e;
            }
            default:
                throw new ParseException(lex.tok(), "ID", "MINUS", "INTLIT", "NOT", "LBR");
        }
    }

    private Exp OperatorClause(Exp e) {
        switch (lex.tok().type) {
            case "MUL": {
                lex.next();
                return new ExpTimes(e, SimpleExp());
            }
            case "DIV": {
                lex.next();
                return new ExpDiv(e, SimpleExp());
            }
            case "MINUS": {
                lex.next();
                return new ExpMinus(e, SimpleExp());
            }
            case "ADD": {
                lex.next();
                return new ExpPlus(e, SimpleExp());
            }
            case "LT": {
                lex.next();
                return new ExpLessThan(e, SimpleExp());
            }
            case "LE": {
                lex.next();
                return new ExpLessThanEqual(e, SimpleExp());
            }
            case "EQ": {
                lex.next();
                return new ExpEqual(e, SimpleExp());
            }
            case "AND": {
                lex.next();
                return new ExpAnd(e, SimpleExp());
            }
            case "OR": {
                lex.next();
                return new ExpOr(e, SimpleExp());
            }
            default:
                return e;
        }
    }

    /**
     * Parse and pretty-print an LPL source file specified
     * by a command line argument.
     * @param args command-line arguments
     * @throws ParseException if the source file contains syntax errors
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: parse.LPLParser <source-file>");
            System.exit(1);
        }
        LPLParser parser = new LPLParser();
        Program program = parser.parse(args[0]);
        System.out.println(program);
    }
}