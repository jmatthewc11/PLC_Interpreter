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
        while (tokens.has(0)) {
            statements.add(parseStatement());
            if (tokens.has(0) && peek(";")) {
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
    public Ast.Statement parseStatement() throws ParseException {
        if (peek(Token.Type.IDENTIFIER)) {
            if (match("LET")) {
                return parseDeclarationStatement();
            }
            else if (match("IF")) {
                return parseIfStatement();
            }
            else if (match("WHILE")) {
                return parseWhileStatement();
            }
            else {
                if (tokens.get(1).getLiteral().equals("=")) {
                    return parseAssignmentStatement();
                }
                else {
                    return parseExpressionStatement();
                }
            }
        }
        else {
            return parseExpressionStatement();
        }
    }

    /**
     * Parses the {@code expression-statement} rule. This method is called if
     * the next tokens do not start another statement type, as explained in the
     * javadocs of {@link #()}.
     */
    public Ast.Statement.Expression parseExpressionStatement() throws ParseException {
        return new Ast.Statement.Expression(parseExpression());
    }

    /**
     * Parses the {@code declaration-statement} rule. This method should only be
     * called if the next tokens start a declaration statement, aka {@code let}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        String name;
        String type;

        if (match(Token.Type.IDENTIFIER)) {
            name = tokens.get(-1).getLiteral();
            if (match(":")) {
                if (match(Token.Type.IDENTIFIER)) {
                    type = tokens.get(-1).getLiteral();
                    if (match("=")) {
                        Ast.Expression value = parseExpression();
                        return new Ast.Statement.Declaration(name, type, Optional.of(value));
                    }
                    else if (peek(";")) {
                        return new Ast.Statement.Declaration(name, type, Optional.empty());
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
    public Ast.Statement.Assignment parseAssignmentStatement() throws ParseException {
        String name = tokens.get(0).getLiteral();
        tokens.advance();
        if (match("=")) {
            Ast.Expression expression = parseExpression();
            return new Ast.Statement.Assignment(name, expression);
        }
        throw new ParseException("Assignment statement is incorrect", tokens.index);
    }

    /**
     * Parses the {@code if-statement} rule. This method should only be called
     * if the next tokens start an if statement, aka {@code if}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        Ast.Expression condition = parseExpression();
        List<Ast.Statement> thenStatements = new ArrayList<>();
        List<Ast.Statement> elseStatements = new ArrayList<>();
        if (match("THEN")) {
            if (peek("END")) {
                return new Ast.Statement.If(condition, thenStatements, elseStatements);
            }
            else {
                while (!peek("ELSE") && !peek("END")) {
                    thenStatements.add(parseStatement());      //FIXME: may have multiple statements
                    tokens.advance();
                }
                if (peek("END")) {
                    return new Ast.Statement.If(condition, thenStatements, elseStatements);
                }
                else if (match("ELSE")) {
                    while(!peek("END")) {
                        elseStatements.add(parseStatement());
                        tokens.advance();
                    }
                    if (match("END")) {
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
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        Ast.Expression expression = parseExpression();
        if (match("DO")) {
            if (peek("END")) {
                return new Ast.Statement.While(expression, null);     //FIXME: says we don't need statements but AST class says we do
            }
            else {
                List<Ast.Statement> statements = new ArrayList<>();
                while (!peek("END")) {
                    statements.add(parseStatement());      //FIXME: may have multiple statements
                    tokens.advance();
                }
                if (match("END")) {
                    return new Ast.Statement.While(expression, statements);
                }
            }
        }
        throw new ParseException("While statement is incorrect", tokens.index);
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        return parseEqualityExpression();
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseEqualityExpression() throws ParseException {
        Ast.Expression first_expr = parseAdditiveExpression();
        if (!tokens.has(1) || peek(";") || peek(")") || peek("DO") || peek("THEN") || peek("ELSE") || peek("END") || peek(",")) {
            return first_expr;
        }

        if (match("!=") || match("==")) {
            return new Ast.Expression.Binary(tokens.get(-1).getLiteral(), first_expr, parseAdditiveExpression());
        }
        else {
            throw new ParseException("Additive expression is missing operator", tokens.index);
        }
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        Ast.Expression first_expr = parseMultiplicativeExpression();
        if (!tokens.has(1) || peek(";") || peek(")") || peek("DO") || peek("THEN") || peek("ELSE") || peek("END") || peek(",") || peek("==") || peek("!=")) {
            return first_expr;
        }

        if (match("+") || match("-")) {
            return new Ast.Expression.Binary(tokens.get(-1).getLiteral(), first_expr, parseMultiplicativeExpression());
        }
        else {
            throw new ParseException("Additive expression is missing operator", tokens.index);
        }
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
        Ast.Expression first_expr = parsePrimaryExpression();
        if (!tokens.has(1) || peek(";") || peek(")") || peek("DO") || peek("THEN") || peek("ELSE") || peek("END") || peek(",") || peek("==") || peek("!=") || peek("+") || peek("-")) {
            return first_expr;
        }

        if (match("*") || match("/")) {
            return new Ast.Expression.Binary(tokens.get(-1).getLiteral(), first_expr, parsePrimaryExpression());
        }
        else {
            throw new ParseException("Multiplicative expression is missing operator", tokens.index);
        }
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expression parsePrimaryExpression() throws ParseException {
        if (peek(Token.Type.IDENTIFIER)) {
            if (peek("TRUE")) {
                return new Ast.Expression.Literal(Boolean.TRUE);
            }
            else if (peek("FALSE")) {
                return new Ast.Expression.Literal(Boolean.FALSE);
            }
            else {
                if (tokens.has(1) && tokens.get(1).getLiteral().equals("(")) {
                    List<Ast.Expression> args = new ArrayList<>();
                    Ast.Expression func = new Ast.Expression.Function(tokens.get(0).getLiteral(), args);
                    tokens.advance();
                    tokens.advance();
                    while (!(peek(")"))) {
                        args.add(parseExpression());
                        if (match(",")) {}
                        else if (!peek(")")) {
                            throw new ParseException("Invalid expression", tokens.index);
                        }
                    }
                    if (!match(")")) {
                        throw new ParseException("Missing closed parenthesis in expression", tokens.index);
                    }
                    return func;
                }
                else {
                    Ast.Expression var = new Ast.Expression.Variable(tokens.get(0).getLiteral());
                    if (tokens.has(1))
                        tokens.advance();
                    return var;
                }
            }
        }
        else if (match("(")) {
            Ast.Expression expr = parseExpression();
            if (match(")"))
                return new Ast.Expression.Group(expr);
            else
                throw new ParseException("Missing closing parenthesis for grouping", tokens.index);
        }
        else if (peek(Token.Type.INTEGER)) {
            if (tokens.has(1)) {
                tokens.advance();
                return new Ast.Expression.Literal(new BigInteger(tokens.get(-1).getLiteral()));
            }
            return new Ast.Expression.Literal(new BigInteger(tokens.get(0).getLiteral()));
        }
        else if (peek(Token.Type.DECIMAL)) {
            if (tokens.has(1)) {
                tokens.advance();
                return new Ast.Expression.Literal(new BigDecimal(tokens.get(-1).getLiteral()));
            }
            return new Ast.Expression.Literal(new BigDecimal(tokens.get(0).getLiteral()));
        }
        else if (peek(Token.Type.STRING)) {
            String string = tokens.get(0).getLiteral();
            if (tokens.has(1)) {
                tokens.advance();
            }
            return new Ast.Expression.Literal(string.substring(1, string.length() - 1));
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