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

        // To do: parse the global-variable declarations and add them to globals

        // To do: parse the program body statements and add them to body

        lex.eat("END");
        return new Program(globals, body);
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
            // To do: add the missing cases
            default:
                throw new ParseException(lex.tok(), "LCBR");
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
