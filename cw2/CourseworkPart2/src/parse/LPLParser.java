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
     * Entry point of the Language
     * Structure = Program -> BEGIN NonFormalVarDecl* Stm* END FunOrProcDef*
     * @return
     */
    private Program Program() {
        // store global variables
        List<VarDecl> globals = new LinkedList<>();
        // store body of the program
        List<Stm> body = new LinkedList<>();

        List<MethodDecl> funcs = new LinkedList<>();
        lex.eat("BEGIN");
        while (lex.tok().isType("INT_TYPE")) {
            globals.addAll(NonFormalVarDeclKlenee());
        }
        while (!lex.tok().isType("END")) {
            body.add(Stm());
        }
        lex.eat("END");
        while (lex.tok().isType("FUN") || lex.tok().isType("PROC")) {
            funcs.add(FunOrProcDef());
        }
        return new Program(globals, body, funcs);
    }

    /**
     * Klenee for Variable Declarations
     * @return
     */
    private List<VarDecl> NonFormalVarDeclKlenee() {
        List<VarDecl> varDecls = new ArrayList<>();

        // Loop as long as we see INT_TYPE
        while (lex.tok().isType("INT_TYPE")) {
            varDecls.add(NonFormalVarDecl());
        }

        return varDecls;
    }

    /**
     * Handle the Type token
     * format = NonFormalVarDecl -> Type ID SEMIC
     * Where type can be INT_TYPE ArraySpec*
     * @return
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
     * @return
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
     * Handle creating an array of ints
     * @param baseType
     * @return
     */
    private TypeArray ArraySpec(Type baseType) {
        lex.eat("LSQBR");
        lex.eat("RSQBR");

        return new TypeArray(baseType);
    }

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
    private VarDecl AnotherFormal(){
        lex.eat("COMMA");
        Type t = Type();
        String id = lex.eat("ID");
        return new VarDecl(t, id);
    }

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

    private List<Exp> Actuals() {
        List<Exp> actuals = new ArrayList<>();

        // Check if there are no parameters (right bracket immediately follows left bracket)
        if (!lex.tok().isType("RBR")) {
            actuals.add(Exp());

            while (lex.tok().isType("COMMA")) {
                actuals.add(AnotherActual());
            }
        }

        return actuals;
    }

    private Exp AnotherActual() {
        lex.eat("COMMA");
        return Exp();
    }
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

    private Exp Exp() {
        Exp e1 = SimpleExp();
        return OperatorClause(e1);
    }

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

    private Exp SimpleIdFactor(String id) {
        // Handle function calls
        if (lex.tok().type.equals("LBR")) {
            lex.eat("LBR");
            List<Exp> actuals = Actuals();
            lex.eat("RBR");
            return new ExpMethodCall(id, actuals);
        }

        // Handle array access
        List<Exp> indexers = new ArrayList<>();
        while (lex.tok().type.equals("LSQBR")) {
            indexers.add(Indexer());
        }

        // Create base expression (either variable or array access)
        Exp baseExp;
        if (indexers.isEmpty()) {
            baseExp = new ExpVar(id);
        } else {
            baseExp = new ExpArrayAccess(id, indexers);
        }

        // Apply SimpleIdLexprFactor to the base expression
        return SimpleIdLexprFactor(baseExp);
    }

    private Exp SimpleIdLexprFactor(Exp baseExp) {
        // Check if we have a DOT LENGTH access
        if (lex.tok().type.equals("DOT")) {
            lex.eat("DOT");
            lex.eat("LENGTH");
            return new ExpArrayLength(baseExp);
        }

        // Default case: just return the base expression unchanged
        return baseExp;
    }


    private Exp NewArrayExp() {
        lex.eat("INT_TYPE");

        List<Exp> dimensions = new ArrayList<>();

        // Process first dimension with expression
        lex.eat("LSQBR");
        dimensions.add(Exp());
        lex.eat("RSQBR");

        // Process additional empty dimensions (ArraySpec*)
        while (lex.tok().type.equals("LSQBR")) {
            lex.eat("LSQBR");
            lex.eat("RSQBR");  // No expression here, just empty brackets
            dimensions.add(null);  // Or some placeholder to indicate an empty dimension
        }

        return new ExpNewArray(new TypeInt(), dimensions);
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