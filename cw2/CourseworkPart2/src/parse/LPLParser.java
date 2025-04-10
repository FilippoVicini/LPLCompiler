package parse;

import ast.*;
import sbnf.ParseException;
import sbnf.lex.Lexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Parse an LPL program and build its AST.
 */
public class LPLParser {

    public static final String LPL_GRAMMAR_FILE = "data/LPL.sbnf";

    private final Lexer lex;


    public LPLParser() {
        lex = new Lexer(LPL_GRAMMAR_FILE);
    }

    /**
     * Parse an LPL source file and return its AST.
     *
     * @param sourcePath a path to an LPL source file
     * @return the Program AST for the parsed LPL program
     * @throws ParseException if the source file contains syntax errors
     * @throws IOException
     */
    public Program parse(String sourcePath) throws IOException {
        lex.readFile(sourcePath);
        lex.next();
        Program prog = program();
        if (!lex.tok().isType("EOF")) {
            throw new ParseException(lex.tok(), "EOF");
        }
        return prog;
    }

    private Program Program() {
        // store global variables
        List < VarDecl > globals = new LinkedList < > ();
        // store body of the program
        List < Stm > body = new LinkedList < > ();
        // store methods of the program
        List < MethodDecl > methods = new LinkedList < > ();
        lex.eat("BEGIN");
        while (lex.tok().isType("INT_TYPE")) {
            globals.add(nonFormalVarDecl());
        }
        while (!lex.tok().isType("END")) {
            body.add(stm());
        }
        lex.eat("END");
        while (lex.tok().isType("FUN") || lex.tok().isType("PROC")) {
            methods.add(funOrProcDef());
        }
        return new Program(globals, body, methods);
    }

    private Program program() {
        // store global variables
        List < VarDecl > globals = new LinkedList < > ();
        // store body of the program
        List < Stm > body = new LinkedList < > ();
        // store methods of the program
        List < MethodDecl > methods = new LinkedList < > ();
        lex.eat("BEGIN");
        while (lex.tok().isType("INT_TYPE")) {
            globals.add(nonFormalVarDecl());
        }
        while (!lex.tok().isType("END")) {
            body.add(stm());
        }
        lex.eat("END");
        while (lex.tok().isType("FUN") || lex.tok().isType("PROC")) {
            methods.add(funOrProcDef());
        }
        return new Program(globals, body, methods);
    }

    /**
     * Klenee for Variable Declarations
     */
    private List < VarDecl > NonFormalVarDeclKlenee() {
        List < VarDecl > varDecls = new ArrayList < > ();

        // Loop as long as there is INT_TYPE
        while (lex.tok().isType("INT_TYPE")) {
            varDecls.add(nonFormalVarDecl());
        }

        return varDecls;
    }

    /**
     * Handle the Type token
     * format = NonFormalVarDecl -> Type ID SEMIC
     * Where type can be INT_TYPE ArraySpec*
     */
    private VarDecl nonFormalVarDecl() {
        Type t = type();
        String id = lex.eat("ID");
        lex.eat("SEMIC");
        return new VarDecl(t, id);
    }


    /**
     * Handle Type either INT_TYPE which represents int
     * or ArraySpec* which represents an array of ints
     */
    private Type type() {
        lex.eat("INT_TYPE");
        Type baseType = new TypeInt();

        while (lex.tok().type.equals("LSQBR")) {
            baseType = ArraySpec(baseType);
        }
        return baseType;
    }


    /**
     * Handler to create an array of ints
     */
    private TypeArray ArraySpec(Type baseType) {
        lex.eat("LSQBR");
        lex.eat("RSQBR");

        return new TypeArray(baseType);
    }

    /**
     * Handler to manage method declaration either FUN(return type) or PROC(no retrun type)
     *
     * @return
     */
    private MethodDecl funOrProcDef() {
        boolean isFunc = false;
        if (lex.tok().isType("FUN")) {
            lex.eat("FUN");
            Type returnType = type();
            isFunc = true;
            return methodDef(returnType, isFunc);
        } else if (lex.tok().isType("PROC")) {
            lex.next();
            return methodDef(null, isFunc);
        } else {
            throw new ParseException(lex.tok(), "FUN", "PROC");
        }

    }


    /**
     * Method definition for the language
     */
    private MethodDecl methodDef(Type returnType, boolean isFunc) {
        String methodName = lex.eat("ID");

        lex.eat("LBR");

        List < VarDecl > formals = formals();

        lex.eat("RBR");
        lex.eat("LCBR");

        List < VarDecl > nonFormalVars = new ArrayList < > ();

        while (lex.tok().isType("INT_TYPE")) {
            nonFormalVars.add(nonFormalVarDecl());
        }

        List < Stm > statements = new ArrayList < > ();
        while (!lex.tok().isType("RCBR")) {
            statements.add(stm());
        }
        lex.eat("RCBR");

        if (isFunc) {
            return new MethodDecl(methodName, returnType, formals, nonFormalVars, statements);
        } else {
            // For procedures, use the constructor without return type
            return new MethodDecl(methodName, formals, nonFormalVars, statements);
    }
        }

    /**
     * Method to handle parameters for the method
     */
    private List < VarDecl > formals() {
        List < VarDecl > params = new ArrayList < > ();
        if (!lex.tok().type.equals("INT_TYPE")) {
            return params;
        }

        Type t = type();
        String id = lex.eat("ID");
        params.add(new VarDecl(t, id));

        while (lex.tok().type.equals("COMMA")) {
            VarDecl param = AnotherFormal();
            params.add(param);
        }
        return params;
    }
    /**
     * Method to handle multiple parameters in the method
     */
    private VarDecl AnotherFormal() {
        lex.eat("COMMA");
        Type t = type();
        String id = lex.eat("ID");
        return new VarDecl(t, id);
    }
    /**
     * Method to manage both method calls and vars assignments
     */
    private Stm stmIdFactor(String id) {
        if (lex.tok().isType("LBR")) {
            lex.next();
            List < Exp > actuals = actuals();
            lex.eat("RBR");
            lex.eat("SEMIC");
            return new StmMethodCall(id, actuals);
        } else {

            List < Exp > idx = new ArrayList < > ();
            while (lex.tok().type.equals("LSQBR")) {
                idx.add(Indexer());
            }

            lex.eat("ASSIGN");
            Exp exps = exp();
            lex.eat("SEMIC");

            if (idx.isEmpty()) {
                return new StmAssign(id, exps);
            } else {
                return new StmArrayAssign(id, idx, exps);
            }
        }
    }

    /**
     * Handles parameters in function call
     */
    private List < Exp > actuals() {
        List < Exp > actuals = new ArrayList < > ();

        if (!lex.tok().isType("RBR")) {
            actuals.add(exp());

            while (lex.tok().isType("COMMA")) {
                actuals.add(AnotherActual());
            }
        }

        return actuals;
    }
    /**
     * Handles multiple parameters calls
     */
    private Exp AnotherActual() {
        lex.eat("COMMA");
        return exp();
    }


    /**
     * Handle all types of statements
     */
    private Stm stm() {
        switch (lex.tok().type) {
            case "ID":
            {
                String id = lex.eat("ID");
                return stmIdFactor(id);
            }
            case "IF":
            {
                lex.next();
                lex.eat("LBR");
                Exp e = exp();
                lex.eat("RBR");
                Stm trueBranch = stm();
                lex.eat("ELSE");
                Stm falseBranch = stm();
                return new StmIf(e, trueBranch, falseBranch);
            }
            case "WHILE":
            {
                lex.next();
                lex.eat("LBR");
                Exp e = exp();
                lex.eat("RBR");
                Stm body = stm();
                return new StmWhile(e, body);
            }
            case "PRINT":
            {
                lex.next();
                Exp e = exp();
                lex.eat("SEMIC");
                return new StmPrint(e);
            }
            case "PRINTLN":
            {
                lex.next();
                Exp e = exp();
                lex.eat("SEMIC");
                return new StmPrintln(e);
            }
            case "PRINTCH":
            {
                lex.next();
                Exp e = exp();
                lex.eat("SEMIC");
                return new StmPrintChar(e);
            }
            case "NEWLINE":
            {
                lex.next();
                lex.eat("SEMIC");
                return new StmNewline();
            }
            case "RETURN":
            {
                lex.next();
                if (lex.tok().isType("SEMIC")) {
                    lex.eat("SEMIC");
                    return new StmReturn(null);
                } else {
                    Exp returnExp = exp();
                    lex.eat("SEMIC");
                    return new StmReturn(returnExp);
                }
            }
            case "LCBR":
            {
                lex.next();
                List < Stm > stms = new ArrayList < > ();
                while (!lex.tok().isType("RCBR") && !lex.tok().isType("EOF")) {
                    stms.add(stm());
                }
                lex.eat("RCBR");
                return new StmBlock(stms);
            }
            case "SWITCH":
            {
                lex.next();
                lex.eat("LBR");
                Exp caseExp = exp();
                lex.eat("RBR");
                lex.eat("LCBR");
                List < StmSwitch.Case > cases = new ArrayList < > ();
                while (lex.tok().isType("CASE")) {
                    cases.add(switchCase());
                }
                lex.eat("DEFAULT");
                lex.eat("COLON");
                Stm defaultCase = stm();
                lex.eat("RCBR");
                return new StmSwitch(caseExp, defaultCase, cases);
            }
            default:
                throw new ParseException(lex.tok(), "ID", "IF", "WHILE", "PRINT", "PRINTLN", "PRINTCH", "NEWLINE", "RETURN", "LCBR", "SWITCH");
        }
    }

    /**
     * Indexer for array assignments
     */
    private Exp Indexer() {
        lex.eat("LSQBR");
        Exp index = exp();
        lex.eat("RSQBR");
        return index;
    }

    private StmSwitch.Case switchCase() {
        lex.eat("CASE");
        int caseNumber = signedInt();
        lex.eat("COLON");
        Stm stm = stm();
        return new StmSwitch.Case(caseNumber, stm);
    }


    private int signedInt() {
        return optionalSign() * Integer.parseInt(lex.eat("INTLIT"));
    }

    private int optionalSign() {
        switch (lex.tok().type) {
            case "MINUS":
                lex.next();
                return -1;
            default:
                return 1;
        }
    }
    /**
     * Initial handler for expressions
     */
    private Exp exp() {
        Exp e1 = simpleExp();
        return operatorClause(e1);
    }

    /**
     * Simple Expressions handler
     */
    private Exp simpleExp() {
        switch (lex.tok().type) {
            case "ID":
            {
                String id = lex.tok().image;
                lex.next();
                return simpleIdFactor(id);
            }
            case "MINUS", "INTLIT":
            {
                return new ExpInt(signedInt());
            }
            case "NEW":
            {
                return newArrayExp();
            }
            case "NOT":
            {
                lex.next();
                return new ExpNot(simpleExp());
            }
            case "NULL":
            {
                lex.next();
                return null;
            }
            case "LBR":
            {
                lex.next();
                Exp e = exp();
                lex.eat("RBR");
                return e;
            }
            default:
                throw new ParseException(lex.tok(), "ID", "MINUS", "INTLIT", "NEW", "NOT", "NULL", "LBR");
        }
    }
    /**
     * Handle ID statements either array access or method calls
     */
    private Exp simpleIdFactor(String id) {
        if (lex.tok().isType("LBR")) {
            lex.next();
            List < Exp > actuals = actuals();
            lex.eat("RBR");
            return new ExpMethodCall(id, actuals);
        }
        List < Exp > indexers = new ArrayList < > ();
        while (lex.tok().type.equals("LSQBR")) {
            indexers.add(Indexer());
        }

        Exp baseExp;
        if (indexers.isEmpty()) {
            baseExp = new ExpVar(id);
        } else {
            baseExp = new ExpArrayAccess(id, indexers);
        }
        return simpleIdLexprFactor(baseExp);
    }

    /**
     * Handle array methods calls
     */
    private Exp simpleIdLexprFactor(Exp baseExp) {
        if (lex.tok().type.equals("DOT")) {
            lex.eat("DOT");
            lex.eat("LENGTH");
            return new ExpArrayLength(baseExp);
        }

        return baseExp;
    }

    /**
     * Handle creation of new array
     */
    private Exp newArrayExp() {
        lex.eat("NEW");
        lex.eat("INT_TYPE");
        lex.eat("LSQBR");
        Exp sizeExp = exp();
        lex.eat("RSQBR");
        while (lex.tok().isType("LSQBR")) {
            lex.eat("LSQBR");
            lex.eat("RSQBR");
        }
        return new ExpInt(0);
    }


    /**
     * All possible operators
     */
    private Exp operatorClause(Exp e) {
        switch (lex.tok().type) {
            case "MUL":
            {
                lex.next();
                return new ExpTimes(e, simpleExp());
            }
            case "DIV":
            {
                lex.next();
                return new ExpDiv(e, simpleExp());
            }
            case "MINUS":
            {
                lex.next();
                return new ExpMinus(e, simpleExp());
            }
            case "ADD":
            {
                lex.next();
                return new ExpPlus(e, simpleExp());
            }
            case "LT":
            {
                lex.next();
                return new ExpLessThan(e, simpleExp());
            }
            case "LE":
            {
                lex.next();
                return new ExpLessThanEqual(e, simpleExp());
            }
            case "EQ":
            {
                lex.next();
                return new ExpEqual(e, simpleExp());
            }
            case "AND":
            {
                lex.next();
                return new ExpAnd(e, simpleExp());
            }
            case "OR":
            {
                lex.next();
                return new ExpOr(e, simpleExp());
            }
            default:
                return e;
        }
    }


    /**
     * Parse and pretty-print an LPL source file specified
     * by a command line argument.
     *
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