package parse;

import ast.*;
import sbnf.ParseException;
import sbnf.lex.Lexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LPLParser {

    public static final String LPL_SBNF_FILE = "data/LPL.sbnf";
    private Lexer lex;

    public LPLParser() {
        lex = new Lexer(LPL_SBNF_FILE);
    }

    public Program parse(String sourcePath) throws IOException {
        lex.readFile(sourcePath);
        lex.next();
        Program prog = Program();
        if (!lex.tok().isType("EOF")) {
            throw new ParseException(lex.tok(), "EOF");
        }
        return prog;
    }

    // Start method for the rules
    private Program Program() {
        // checks that head is "BEGIN"
        lex.eat("BEGIN");
        // Program is a composition of GlobalVarDecl* and Stm*
        List<VarDecl> globals = GlobalVarDeclKleene();
        List<Stm> body = StmKleene();
        // checks that head is "END" and finishes the program
        lex.eat("END");
        return new Program(globals, body);
    }

    // Klenee method for the GlobalVarDecl
    private List<VarDecl> GlobalVarDeclKleene() {
        // List of variable declarations
        List<VarDecl> varDecls = new ArrayList<>();
        // Type can only be of "INT_TYPE" if it is add it to the list
        while (lex.tok().isType("INT_TYPE")) {
            varDecls.add(VarDecl());
        }
        return varDecls;
    }

    // Klenee method for the Stm statements
    private List<Stm> StmKleene() {
        // List to contain the Stms
        List<Stm> stms = new ArrayList<>();
        while (!lex.tok().isType("END") && !lex.tok().isType("RCBR") &&
                !lex.tok().isType("CASE") && !lex.tok().isType("DEFAULT")) {
            stms.add(Stm());
        }
        return stms;
    }

    // Method to parse the VarDecl for the GlobalVarDecl
    private VarDecl VarDecl() {
        TypeInt type = Type();
        String name = lex.tok().image;
        lex.eat("ID");
        lex.eat("SEMIC");
        return new VarDecl(type, name);
    }

    // Method to check if Type rule
    private TypeInt Type() {
        lex.eat("INT_TYPE");
        return new TypeInt();
    }

    // Method to handle all Stm rules
    private Stm Stm() {
        // switch all cases for the different rules for the Stm cases
        switch (lex.tok().type) {
            // assign statement
            case "ID":
                String var = lex.tok().image;
                lex.eat("ID");
                lex.eat("ASSIGN");
                Exp expr = Exp();
                lex.eat("SEMIC");
                return new StmAssign(var, expr);
            // if statement
            case "IF":
                lex.eat("IF");
                lex.eat("LBR");
                Exp cond = Exp();
                lex.eat("RBR");
                Stm thenBranch = Stm();
                lex.eat("ELSE");
                Stm elseBranch = Stm();
                return new StmIf(cond, thenBranch, elseBranch);
            // while statement
            case "WHILE":
                lex.eat("WHILE");
                lex.eat("LBR");
                Exp whileCond = Exp();
                lex.eat("RBR");
                Stm whileBody = Stm();
                return new StmWhile(whileCond, whileBody);
            // print statement
            case "PRINT":
                lex.eat("PRINT");
                Exp printExpr = Exp();
                lex.eat("SEMIC");
                return new StmPrint(printExpr);
            // println statement
            case "PRINTLN":
                lex.eat("PRINTLN");
                Exp printlnExpr = Exp();
                lex.eat("SEMIC");
                return new StmPrintln(printlnExpr);
            // print ch statement
            case "PRINTCH":
                lex.eat("PRINTCH");
                Exp printchExpr = Exp();
                lex.eat("SEMIC");
                return new StmPrintChar(printchExpr);
            // newline statement
            case "NEWLINE":
                lex.eat("NEWLINE");
                lex.eat("SEMIC");
                return new StmNewline();
            // left curly brace statement
            case "LCBR":
                lex.eat("LCBR");
                List<Stm> blockBody = StmKleene();
                lex.eat("RCBR");
                return new StmBlock(blockBody);
            // switch statement
            case "SWITCH":
                lex.eat("SWITCH");
                lex.eat("LBR");
                Exp switchExpr = Exp();
                lex.eat("RBR");
                lex.eat("LCBR");
                List<StmSwitch.Case> cases = CaseKleene();
                lex.eat("DEFAULT");
                lex.eat("COLON");
                Stm defaultStm = Stm();
                lex.eat("RCBR");
                return new StmSwitch(switchExpr, defaultStm, cases);
            default:
                throw new ParseException(lex.tok(), "statement");
        }
    }

    // method to handle the "case" in the switch statement
    private List<StmSwitch.Case> CaseKleene() {
        List<StmSwitch.Case> cases = new ArrayList<>();
        while (lex.tok().isType("CASE")) {
            cases.add(Case());
        }
        return cases;
    }


    private StmSwitch.Case Case() {
        lex.eat("CASE");

        int sign = 1;
        if (lex.tok().isType("MINUS")) {
            sign = -1;
            lex.eat("MINUS");
        }

        // Parse the integer literal for the case value
        if (!lex.tok().isType("INTLIT")) {
            throw new ParseException(lex.tok(), "integer literal");
        }
        int value = Integer.parseInt(lex.tok().image) * sign;
        lex.eat("INTLIT");


        lex.eat("COLON");

        Stm stm = Stm();

        return new StmSwitch.Case(value, stm);
    }

    // Method to handle the Exp statements with all the possibilities
    private Exp Exp() {
        Exp left = SimpleExp();
        if (lex.tok().isType("ADD") || lex.tok().isType("MINUS") || lex.tok().isType("MUL") ||
                lex.tok().isType("DIV") || lex.tok().isType("LT") || lex.tok().isType("LE") ||
                lex.tok().isType("EQ") || lex.tok().isType("AND") || lex.tok().isType("OR")) {
            String op = lex.tok().type;
            lex.next();
            Exp right = SimpleExp();

            // create all possibilities for the Exp statements
            switch (op) {
                case "ADD":
                    return new ExpPlus(left, right);
                case "MINUS":
                    return new ExpMinus(left, right);
                case "MUL":
                    return new ExpTimes(left, right);
                case "DIV":
                    return new ExpDiv(left, right);
                case "AND":
                    return new ExpAnd(left, right);
                case "OR":
                    return new ExpOr(left, right);
                case "LT":
                    return new ExpLessThan(left, right);
                case "LE" :
                    return new ExpLessThanEqual(left, right);
                case "EQ":
                    return new ExpEqual(left, right);
                default:
                    throw new ParseException(lex.tok(), "binary operator");
            }
        }
        return left;
    }

    // Method to handle simple exps
    private Exp SimpleExp() {
        switch (lex.tok().type) {
            case "ID":
                String id = lex.tok().image;
                lex.eat("ID");
                return new ExpVar(id);
            case "INTLIT":
                int value = Integer.parseInt(lex.tok().image);
                lex.eat("INTLIT");
                return new ExpInt(value);
            case "MINUS":

                lex.eat("MINUS");
                if (lex.tok().isType("INTLIT")) {
                    int val = -Integer.parseInt(lex.tok().image);
                    lex.eat("INTLIT");
                    return new ExpInt(val);
                } else {
                    throw new ParseException(lex.tok(), "INTLIT");
                }
            case "NOT":
                lex.eat("NOT");
                Exp notExpr = SimpleExp();
                return new ExpNot(notExpr);
            case "LBR":
                lex.eat("LBR");
                Exp expr = Exp();
                lex.eat("RBR");
                return expr;
            default:
                throw new ParseException(lex.tok(), "expression");
        }
    }

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
