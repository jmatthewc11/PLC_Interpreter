package plc.interpreter;

import jdk.nashorn.internal.codegen.types.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    private Parser(String input) {
        tokens = new TokenStream(Lexer.lex(input));
    }

    /**
     * Parses the input and returns the AST
     */
    public static Ast parse(String input) {
        return new Parser(input).parse();
    }

    /**
     * Repeatedly parses a list of ASTs, returning the list as arguments of an
     * {@link Ast.Term} with the identifier {@code "source"}.
     */
    private Ast parse() {
        return parseAst();
    }

    /**
     * Parses an AST from the given tokens based on the provided grammar. Like
     * the lexToken method, you may find it helpful to have this call other
     * methods like {@code parseTerm()}. In a recursive descent parser, each
     * rule in the grammar would correspond with a {@code parseX()} function.
     *
     * Additionally, here is an example of parsing a function call in a language
     * like Java, which has the form {@code name(args...)}.
     *
     * <pre>
     * {@code
     *     private Ast.FunctionExpr parseFunctionExpr() {
     *         //In a real parser this would be more complex, as the parser
     *         //wouldn't know this should be a function call until reaching the
     *         //opening parenthesis, like name(... <- here. You won't have this
     *         //problem in this project, but will for the compiler project.
     *         if (!match(Token.Type.IDENTIFIER)) {
     *             throw new ParseException("Expected the name of a function.");
     *         }
     *         String name = tokens.get(-1).getLiteral();
     *         if (!match("(")) {
     *             throw new ParseException("Expected opening bra
     *         }
     *         List<Ast> args = new ArrayList<>();
     *         while (!match(")")) {
     *             //recursive call to parseExpr(), not shown here
     *             args.add(parseExpr());
     *             //next token must be a closing parenthesis or comma
     *             if (!peek(")") && !match(",")) {
     *                 throw new ParseException("Expected closing parenthesis or comma after argument.", tokens.get(-1).getIndex());
     *             }
     *         }
     *         return new Ast.FunctionExpr(name, args);
     *     }
     * }
     * </pre>
     */
    private Ast parseAst() {
        List<Ast> terms = new ArrayList<>();
        if (match("(") || match("[")) { //FIXME: need to keep track of which closing symbol needed?
                                                         //FIXME: double matches on symbol too?
            if (match(Token.Type.IDENTIFIER)) {
                String name = tokens.get(-1).getLiteral();  //get identifier from before current term
                List<Ast> args = new ArrayList<>();         //make list of args for term

                while (!(peek(")") || peek("]"))) {
                    if (peek(Token.Type.NUMBER)) {
                        args.add(parseNum());
                        tokens.advance();
                    }
                    else if (peek(Token.Type.STRING)) {
                        args.add(parseString());
                        tokens.advance();
                    }
                    else if (peek(Token.Type.IDENTIFIER)) {
                        args.add(parseIdentifier());
                        tokens.advance();
                    }
                    else if (peek("(") || peek("[")) {
                        parseAst();     //FIXME: recursive call here right?
                    }
                    else if (!tokens.has(0)) {
                        break;
                    }
                    else {
                        throw new ParseException("Illegal token after identifier in term", tokens.get(-1).getIndex());
                    }
                }

                Ast term = new Ast.Term(name, args);    //make a term out of the name, arguments
                terms.add(term);                        //add to list of terms

                while (match(")") || match("]")) {}

                if (tokens.has(0))
                    parseAst();
                else
//                    return new Ast.Term("source", terms); //First two tests pass with this instead
                    return new Ast.Term(name, args);        //FIXME: not returning the right thing, everything gets
            }                                               // overwritten at the end by the beginning term
            else {
                throw new ParseException("Expected an identifier for term", tokens.get(0).getIndex());
            }

            return new Ast.Term("source", terms);     //FIXME: never hits this, terms is overwritten anyway
        }
        else if (peek(Token.Type.NUMBER) || peek(Token.Type.STRING) || peek(Token.Type.IDENTIFIER)) {
            List<Ast> args = new ArrayList<>();         //make list of literals for AST

            while (!(peek("(") || peek("["))) {
                if (peek(Token.Type.NUMBER)) {
                    args.add(parseNum());
                    tokens.advance();
                }
                else if (peek(Token.Type.STRING)) {
                    args.add(parseString());
                    tokens.advance();
                }
                else if (peek(Token.Type.IDENTIFIER)) { //FIXME: need to make sure this identifier
                    args.add(parseIdentifier());        // doesn't precede (,[ else it's part of a term
                    tokens.advance();
                }
                else {
                    throw new ParseException("Illegal token in class", tokens.get(0).getIndex());
                }
            }

            if (peek("(") || peek("[")) {
                parseAst();     //FIXME: needs to recurse here to hit first if statement for handling terms
            }

            return new Ast.Term("source", args);
        }
        else {
            throw new ParseException("Code starts with illegal token", tokens.get(0).getIndex());
        }
    }

    private Ast parseNum() {
        return new Ast.NumberLiteral(new BigDecimal(tokens.get(0).getLiteral()));
    }

    private Ast parseString() {
        String string = tokens.get(0).getLiteral();     //gets the literal, easier to read substring
        return new Ast.StringLiteral(string.substring(1, string.length() - 1));
    }

    private Ast parseIdentifier() {
        return new Ast.Identifier(tokens.get(0).getLiteral());
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        int i = 0;
        int count = 0;
        int offset = 0;
        for (i = 0; i < patterns.length; i++) {
            if (tokens.has(offset)) {
                if (patterns[i] instanceof String) {
                    if (patterns[i].equals(tokens.get(offset).getLiteral())) {
                        count++;
                        offset++;
                    } else {
                        return false;
                    }
                } else {
                    if (patterns[i].equals(tokens.get(offset).getType())) {
                        count++;
                        offset++;
                    } else {
                        return false;
                    }
                }
            }
            else {
                return false;
            }
        }

        return (count == 0 || i == patterns.length);
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        int i = 0;
        int count = 0;
        int offset = 0;
        for (i = 0; i < patterns.length; i++) {
            if (tokens.has(offset)) {
                if (patterns[i] instanceof String) {
                    if (patterns[i].equals(tokens.get(offset).getLiteral())) {
                        count++;
                        offset++;
//                        while (tokens.has(offset) && patterns[i].equals(tokens.get(offset).getLiteral())) {
//                            count++;
//                            offset++;
//                        }
                    }
                    else {
                        return false;
                    }
                }
                else {
                    if (patterns[i].equals(tokens.get(offset).getType())) {
                        count++;
                        offset++;
//                        while (tokens.has(offset) && patterns[i].equals(tokens.get(offset).getLiteral())) {
//                            count++;
//                            offset++;
//                        }
                    }
                    else {
                        return false;
                    }
                }
            }
            else {
                break;
            }
        }

        if (count == 0 || i != patterns.length) {
            return false;
        }

        while (count > 0) {
            tokens.advance();
            count--;
        }
        return true;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }
}
