package parse;

import ast.*;
import sbnf.ParseException;
import sbnf.lex.Lexer;

import java.io.IOException;
import java.lang.reflect.Method;
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

    /**
     * Entry point
     * Structure = Program -> BEGIN NonFormalVarDecl* Stm* END FunOrProcDef*
     * @return
     */
    private Program Program() {
        // store global variables
        List<VarDecl> globals = new LinkedList<>();
        // store body of the program
        List<Stm> body = new LinkedList<>();
        // store methods of the program
        List<MethodDecl> methods = new LinkedList<>();
        lex.eat("BEGIN");
        while (lex.tok().isType("INT_TYPE")) {
            globals.addAll(NonFormalVarDeclKlenee());
        }
        while (!lex.tok().isType("END")) {
            body.add(Stm());
        }
        lex.eat("END");
        while (lex.tok().isType("FUN") || lex.tok().isType("PROC")) {
            methods.add(FunOrProcDef());
        }
        return new Program(globals, body, methods);
    }

    /**
     * Klenee for Variable Declarations
     */
    private List<VarDecl> NonFormalVarDeclKlenee() {
        List<VarDecl> varDecls = new ArrayList<>();

        // Loop as long as there is INT_TYPE
        while (lex.tok().isType("INT_TYPE")) {
            varDecls.add(NonFormalVarDecl());
        }

        return varDecls;
    }

    /**
     * Handle the Type token
     * format = NonFormalVarDecl -> Type ID SEMIC
     * Where type can be INT_TYPE ArraySpec*
     */
    private VarDecl NonFormalVarDecl() {
        Type t = Type();
        String id = lex.eat("ID");
        lex.eat("SEMIC");
        return new VarDecl(t, id);
    }

    /**
     * Handle Type either INT_TYPE which represents int
     * or ArraySpec* which represents an array of ints
     */
    private Type Type() {
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
     * @return
     */
    private MethodDecl FunOrProcDef() {
        if (lex.tok().type.equals("FUN")) {
            lex.eat("FUN");
            Type returnType = Type();
            return MethodDef(returnType);
        } else{
            lex.eat("PROC");
            return MethodDef(null);
        }
    }

    /**
     * Method definition for the language
     */
    private MethodDecl MethodDef(Type returnType){
        String methodName = lex.eat("ID");

        lex.eat("LBR");
        List<VarDecl> params = Formals();
        lex.eat("RBR");

        lex.eat("LCBR");

        List<VarDecl> nonFormalVars = new ArrayList<>();
        while(lex.tok().isType("INT_TYPE")) {
            nonFormalVars.add(NonFormalVarDecl());
        }
        List<Stm> statements = new ArrayList<>();
        while (!lex.tok().isType("RCBR")) {
            statements.add(Stm());
        }

        lex.eat("RCBR");
        return new MethodDecl(returnType, methodName, params, nonFormalVars, statements);

    }

    /**
     * Method to handle parameters for the method
     */
    private List<VarDecl> Formals(){
        List<VarDecl> params = new ArrayList<>();
        if(!lex.tok().type.equals("INT_TYPE")){
            return params;
        }
        Type t = Type();
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
    private VarDecl AnotherFormal(){
        lex.eat("COMMA");
        Type t = Type();
        String id = lex.eat("ID");
        return new VarDecl(t, id);
    }

    /**
     * Method to manage both method calls and vars assignments
     */
    private Stm StmIdFactor(String id) {
        if (lex.tok().type.equals("LBR")) {

            lex.eat("LBR");
            List<Exp> actuals = Actuals();
            lex.eat("RBR");
            lex.eat("SEMIC");
            return new StmMethodCall(id, actuals);
        } else {

            List<Exp> indexers = new ArrayList<>();
            while (lex.tok().type.equals("LSQBR")) {
                indexers.add(Indexer());
            }

            lex.eat("ASSIGN");
            Exp valueExpression = Exp();
            lex.eat("SEMIC");

            if (indexers.isEmpty()) {
                return new StmAssign(id, valueExpression);
            } else {
                return new StmArrayAssign(id, indexers, valueExpression);
            }
        }
    }

    /**
     * Handles parameters in function call
     */
    private List<Exp> Actuals() {
        List<Exp> actuals = new ArrayList<>();

        if (!lex.tok().isType("RBR")) {
            actuals.add(Exp());

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
        return Exp();
    }

    /**
     * Handle all types of statements
     */
    private Stm Stm() {
        switch (lex.tok().type) {
            case "ID": {
                String id = lex.eat("ID");
                return StmIdFactor(id);
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
            case "RETURN": {
                lex.next();
                if (lex.tok().isType("SEMIC")) {
                    lex.eat("SEMIC");
                    return new StmReturn(null);
                } else {
                    Exp returnExp = Exp();
                    lex.eat("SEMIC");
                    return new StmReturn(returnExp);
                }
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

    /**
     * Indexer for array assignments
     */
    private Exp Indexer() {
        lex.eat("LSQBR");
        Exp index = Exp();
        lex.eat("RSQBR");
        return index;
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

    /**
     * Initial handler for expressions
     */
    private Exp Exp() {
        Exp e1 = SimpleExp();
        return OperatorClause(e1);
    }

    /**
     * Simple Expressions handler
     */
    private Exp SimpleExp() {
        switch (lex.tok().type) {
            case "ID": {
                String id = lex.tok().image;
                lex.next();
                return SimpleIdFactor(id);
            }
            case "MINUS", "INTLIT": {
                return new ExpInt(SignedInt());
            }
            case "NEW": {
                lex.next();
                return NewArrayExp();
            }
            case "NOT": {
                lex.next();
                return new ExpNot(SimpleExp());
            }
            case "NULL": {
                lex.next();
                return null;
            }
            case "LBR": {
                lex.next();
                Exp e = Exp();
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
    private Exp SimpleIdFactor(String id) {
        if (lex.tok().type.equals("LBR")) {
            lex.eat("LBR");
            List<Exp> actuals = Actuals();
            lex.eat("RBR");
            return new ExpMethodCall(id, actuals);
        }

        List<Exp> indexers = new ArrayList<>();
        while (lex.tok().type.equals("LSQBR")) {
            indexers.add(Indexer());
        }

        Exp baseExp;
        if (indexers.isEmpty()) {
            baseExp = new ExpVar(id);
        } else {
            baseExp = new ExpArrayAccess(id, indexers);
        }

        return SimpleIdLexprFactor(baseExp);
    }

    /**
     * Handle array methods calls
     */
    private Exp SimpleIdLexprFactor(Exp baseExp) {
        if (lex.tok().type.equals("DOT")) {
            lex.eat("DOT");
            lex.eat("LENGTH");
            return new ExpArrayLength(baseExp);
        }

        return baseExp;
    }


    /**
     * Handle creation of new array
     * @return
     */
    private Exp NewArrayExp() {
        lex.eat("INT_TYPE");

        List<Exp> dimensions = new ArrayList<>();

        lex.eat("LSQBR");
        dimensions.add(Exp());
        lex.eat("RSQBR");


        while (lex.tok().type.equals("LSQBR")) {
            lex.eat("LSQBR");
            lex.eat("RSQBR");
            dimensions.add(null);
        }

        return new ExpNewArray(new TypeInt(), dimensions);
    }


    /**
     * All possible operators
     */
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