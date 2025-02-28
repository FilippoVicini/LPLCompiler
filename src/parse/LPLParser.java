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

    /**
     * File path for the LPL SBNF file.
     * Note: this is a relative path so at runtime the working directory must
     * be the parent directory of this path (ie the root folder
     * of the project) or the file will not be found.
     */
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
     * @return the AST for the parsed LPL program
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

        // Parse the global-variable declarations and add them to globals
        globals = GlobalVarDeclKleene();

        // Parse the program body statements and add them to body
        while (!lex.tok().isType("END")) {
            body.add(Stm());
        }

        lex.eat("END");
        return new Program(globals, body);
    }

    private List<VarDecl> GlobalVarDeclKleene() {
        switch (lex.tok().type) {
            case "INT_TYPE": {
                VarDecl e = VarDecl();
                List<VarDecl> varDecls = GlobalVarDeclKleene();
                varDecls.add(0,e);
                return varDecls;
            }
            default:
                return new ArrayList<>();
        }
    }
    private VarDecl VarDecl() {
        TypeInt v1 = Type();
        String name = "";
        switch (lex.tok().type) {
            case "ID": {
                String intLit = lex.tok().image;
                lex.next();
                name= intLit;
            }

        }
        return new VarDecl(v1, name);
    }

    private TypeInt Type(){
        switch (lex.tok().type) {
            case "INT_TYPE": {
                String intLit = lex.tok().image;
                lex.next();
                return new TypeInt();
            }

            default:
                throw new ParseException(lex.tok(), "INT", "LBR");
        }
    }


    private Stm Stm() {
        switch (lex.tok().type) {
            case "LCBR": {
                lex.next();
                List<Stm> blockBody = new ArrayList<>();
                while (!lex.tok().isType("RCBR")) {
                    blockBody.add(Stm());
                }
                lex.eat("RCBR");
                return new StmBlock(blockBody);
            }
            case "ID": {
                String id = lex.tok().image;
                lex.next();
                lex.eat("ASSIGN");
                Exp exp = Exp();
                lex.eat("SEMIC");
                return new StmAssign(id, exp);
            }
            case "IF": {
                lex.next();
                lex.eat("LBR");
                Exp condition = Exp();
                lex.eat("RBR");
                Stm thenStm = Stm();
                lex.eat("ELSE");
                Stm elseStm = Stm();
                return new StmIf(condition, thenStm, elseStm);
            }
            case "WHILE": {
                lex.next();
                lex.eat("LBR");
                Exp condition = Exp();
                lex.eat("RBR");
                Stm body = Stm();
                return new StmWhile(condition, body);
            }
            case "PRINT": {
                lex.next();
                Exp exp = Exp();
                lex.eat("SEMIC");
                return new StmPrint(exp);
            }
            case "PRINTLN": {
                lex.next();
                Exp exp = Exp();
                lex.eat("SEMIC");
                return new StmPrintln(exp);
            }
            case "PRINTCH": {
                lex.next();
                Exp exp = Exp();
                lex.eat("SEMIC");
                return new StmPrintch(exp);
            }
            case "NEWLINE": {
                lex.next();
                lex.eat("SEMIC");
                return new StmNewline();
            }
            case "SWITCH": {
                lex.next();
                lex.eat("LBR");
                Exp exp = Exp();
                lex.eat("RBR");
                lex.eat("LCBR");
                List<Case> cases = new ArrayList<>();
                while (lex.tok().isType("CASE")) {
                    cases.add(Case());
                }
                lex.eat("DEFAULT");
                lex.eat("COLON");
                Stm defaultStm = Stm();
                lex.eat("RCBR");
                return new StmSwitch(exp, cases, defaultStm);
            }
            default:
                throw new ParseException(lex.tok(), "LCBR", "ID", "IF", "WHILE", "PRINT", "PRINTLN", "PRINTCH", "NEWLINE", "SWITCH");
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
