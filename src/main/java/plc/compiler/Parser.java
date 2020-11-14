package plc.compiler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

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

//FIXME: may not need the stack passed into every method

public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the tokens and returns the parsed AST.
     */
    public static Ast parse(List<Token> tokens) throws ParseException {
        return new Parser(tokens).parseSource();
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        List<Ast.Statement> statements = new ArrayList<>();
        Stack<String> stack = new Stack<String>();
        while (tokens.has(0)) {
            statements.add(parseStatement(stack));
            if (tokens.get(0).getLiteral().equals(";")) {
                tokens.advance();
            }
        }

        return new Ast.Source(statements);
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, assignment, if, or while
     * statement, then it is an expression statement. See these methods for
     * clarification on what starts each type of statement.
     */
    public Ast.Statement parseStatement(Stack<String> stack) throws ParseException {
        if (peek(Token.Type.IDENTIFIER)) {
            if (peek("LET")) {
                return parseDeclarationStatement(stack);
            }
            else if (peek("IF")) {
                return parseIfStatement(stack);
            }
            else if (peek("WHILE")) {
                return parseWhileStatement(stack);
            }
            else {
//                tokens.advance();
                if (peek("=")) {    //FIXME: need to be able to look at next token to check for this
                    return parseAssignmentStatement(stack);
                }
                else {
                    return parseExpressionStatement(stack);
                }
            }
        }
        else {
            return parseExpressionStatement(stack);
        }
    }

    /**
     * Parses the {@code expression-statement} rule. This method is called if
     * the next tokens do not start another statement type, as explained in the
     * javadocs of {@link #()}.
     */
    public Ast.Statement.Expression parseExpressionStatement(Stack<String> stack) throws ParseException {
        Ast.Expression value = parseExpression(stack);
        return new Ast.Statement.Expression(value);
    }

    /**
     * Parses the {@code declaration-statement} rule. This method should only be
     * called if the next tokens start a declaration statement, aka {@code let}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement(Stack<String> stack) throws ParseException {
        tokens.advance();
        String name;
        String type;

        if (peek(Token.Type.IDENTIFIER)) {
            name = tokens.get(0).getLiteral();
            tokens.advance();
            if (peek(":")) {
                tokens.advance();
                if (peek(Token.Type.IDENTIFIER)) {
                    type = tokens.get(0).getLiteral();
                    tokens.advance();
                    if (peek("=")) {
                        tokens.advance();
                        parseExpressionStatement(stack);
                    }
                    else if (peek(";")) {
                        return new Ast.Statement.Declaration(name, type, null);
                    }
                }
            }
        }
        throw new ParseException("Declaration statement incorrect syntax", tokens.index);
    }

    /**
     * Parses the {@code assignment-statement} rule. This method should only be
     * called if the next tokens start an assignment statement, aka both an
     * {@code identifier} followed by {@code =}.
     */
    public Ast.Statement.Assignment parseAssignmentStatement(Stack<String> stack) throws ParseException {
        String name = tokens.get(-1).getLiteral();
        tokens.advance();
        if (tokens.has(0)) {
            Ast.Expression expression = parseExpression(stack);
            return new Ast.Statement.Assignment(name, expression);
        }
        throw new ParseException("Assignment statement is incorrect", tokens.index);
    }

    /**
     * Parses the {@code if-statement} rule. This method should only be called
     * if the next tokens start an if statement, aka {@code if}.
     */
    public Ast.Statement.If parseIfStatement(Stack<String> stack) throws ParseException {
        tokens.advance();
        Ast.Expression condition = parseExpression(stack);
        if (peek("THEN")) {
            tokens.advance();
            if (peek("END")) {
                return new Ast.Statement.If(condition, null, null);     //FIXME: says we don't need statements but AST class says we do
            }
            else {
                List<Ast.Statement> thenStatements = new ArrayList<>();
                while (!peek("ELSE") && !peek("END")) {
                    thenStatements.add(parseStatement(stack));      //FIXME: may have multiple statements
                    tokens.advance();
                }
                if (peek("END")) {
                    return new Ast.Statement.If(condition, thenStatements, null); //FIXME: says we don't need statements but AST class says we do
                }
                else if (peek("ELSE")) {
                    List<Ast.Statement> elseStatements = new ArrayList<>();
                    while(!peek("END")) {
                        elseStatements.add(parseStatement(stack));
                    }
                    if (peek("END")) {
                        return new Ast.Statement.If(condition, thenStatements, elseStatements);
                    }
                }
            }
        }
        throw new ParseException("If statement is incorrect", tokens.index);
    }

    /**
     * Parses the {@code while-statement} rule. This method should only be
     * called if the next tokens start a while statement, aka {@code while}.
     */
    public Ast.Statement.While parseWhileStatement(Stack<String> stack) throws ParseException {
        tokens.advance();
        Ast.Expression expression = parseExpression(stack);
        if (peek("DO")) {
            tokens.advance();
            if (peek("END")) {
                return new Ast.Statement.While(expression, null);     //FIXME: says we don't need statements but AST class says we do
            }
            else {
                List<Ast.Statement> statements = new ArrayList<>();
                while (!peek("END")) {
                    statements.add(parseStatement(stack));      //FIXME: may have multiple statements
                    tokens.advance();
                }
                if (peek("END")) {
                    return new Ast.Statement.While(expression, statements);
                }
            }
        }
        throw new ParseException("While statement is incorrect", tokens.index);
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression(Stack<String> stack) throws ParseException {
        return parseEqualityExpression(stack);
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseEqualityExpression(Stack<String> stack) throws ParseException {
        Ast.Expression first_expr = parseAdditiveExpression(stack);     //FIXME: how to format the expressions in the statements?
        if (tokens.get(0).getLiteral().equals(";")) {
            return first_expr;
        }
        while (peek("==") || peek("!=")) {
            parseAdditiveExpression(stack);
            tokens.advance();
        }
        throw new ParseException("Additive expression is incorrect", tokens.index);
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression(Stack<String> stack) throws ParseException {
        Ast.Expression first_expr = parseMultiplicativeExpression(stack);     //FIXME: how to format the expressions in the statements?
        if (tokens.get(0).getLiteral().equals(";")) {
            return first_expr;
        }
        while (peek("+") || peek("-")) {
            parseMultiplicativeExpression(stack);
            tokens.advance();
        }
        throw new ParseException("Additive expression is incorrect", tokens.index);
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression(Stack<String> stack) throws ParseException {
        Ast.Expression first_expr = parsePrimaryExpression(stack);     //FIXME: how to format the expressions in the statements?
        if (tokens.get(0).getLiteral().equals(";")) {
            return first_expr;
        }
        while (peek("*") || peek("/")) {
            parsePrimaryExpression(stack);
            tokens.advance();
        }
        throw new ParseException("Multiplicative expression is incorrect", tokens.index);
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression(Stack<String> stack) throws ParseException {
        if (peek(Token.Type.IDENTIFIER)) {
            if (tokens.get(0).getLiteral().equals("TRUE")) {
                return new Ast.Expression.Literal(Boolean.TRUE);
            }
            else if (tokens.get(0).getLiteral().equals("FALSE")) {
                return new Ast.Expression.Literal(Boolean.FALSE);
            }

            else {
                Ast.Expression var = new Ast.Expression.Variable(tokens.get(0).getLiteral());
//                if (peek(Token.Type.OPERATOR) && tokens.get(1).getLiteral().equals(";")) {
                    tokens.advance();
                    return var;
//                }
            }
        }
        else if (peek(Token.Type.INTEGER)) {
            return new Ast.Expression.Literal(new BigInteger(tokens.get(0).getLiteral()));
        }
        else if (peek(Token.Type.DECIMAL)) {
            return new Ast.Expression.Literal(new BigDecimal(tokens.get(0).getLiteral()));
        }
        else if (peek(Token.Type.STRING)) {
            return new Ast.Expression.Literal(tokens.get(0).getLiteral());
        }
        else {
            throw new ParseException("Problem parsing primary expression", tokens.index);
        }
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
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i)) {
                return false;
            } else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) {
                    return false;
                }
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false;
                }
            } else {
                throw new AssertionError();
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                tokens.advance();
            }
        }
        return peek;
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