package plc.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException}.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    final CharStream chars;

    Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Lexes the input and returns the list of tokens.
     */
    public static List<Token> lex(String input) throws ParseException {
        return new Lexer(input).lex();
    }

    /**
     * Repeatedly lexes the next token using {@link #lexToken()} until the end
     * of the input is reached, returning the list of tokens lexed. This should
     * also handle skipping whitespace.
     */
    List<Token> lex() throws ParseException {
        List<Token> tokens = new ArrayList<>();
        while (chars.has(0)) {
            if (chars.get(0) == ' ' || chars.get(0) == '\t' || chars.get(0) == '\r' || chars.get(0) == '\n') {
                chars.advance();
                chars.skip();
                continue;
            }
            tokens.add(lexToken());
//            chars.skip();
        }
        return tokens;
    }

    /**
     * Lexes the next token. It may be helpful to have this call other methods,
     * such as {@code lexIdentifier()} or {@code lexString()}, based on the next
     * character(s).
     *
     * Additionally, here is an example of lex a character literal (not used
     * in this assignment) using the peek/match methods below.
     *
     * <pre>
     * {@code
     *     Token lexCharacter() {
     *         if (!match("\'")) {
     *             //Your lexer should prevent this from happening, as it should
     *             // only try to lexer a character literal if the next character
     *             // begins a character literal.
     *             //Additionally, the index being passed back is a 'ballpark'
     *             // value. If we were doing proper diagnostics, we would want
     *             // to provide a range covering the entire error. It's really
     *             // only for debugging / proof of concept.
     *             throw new ParseException("Next character does not begin a character literal.", chars.index);
     *         }
     *         if (!chars.has(0) || match("\'")) {
     *             throw new ParseException("Empty character literal.",  chars.index);
     *         } else if (match("\\")) {
     *             // lexer escape characters...
     *         } else {
     *             chars.advance();
     *         }
     *         if (!match("\'")) {
     *             throw new ParseException("Unterminated character literal.", chars.index);
     *         }
     *         return chars.emit(Token.Type.CHARACTER);
     *     }
     * }
     * </pre>
     */
    Token lexToken() throws ParseException {
        if (peek("[0-9]")) {
            chars.advance();
            return lexNumber();
        }
        else if (peek("\"")) {
            return lexString();
        }
        else if (peek("[A-Za-z_]")) {
            return lexIdentifier();
        }
        else if (peek("[!=]")) {
            return lexOperator();
        }
        else {
            chars.advance();
            return chars.emit(Token.Type.OPERATOR);
        }
    }

    /**
     * Lexes an IDENTIFIER token. Unlike the previous project, fewer characters
     * are allowed in identifiers.
     */
    Token lexIdentifier() throws ParseException {
        while (match("[A-Za-z0-9_]")) {}

        return chars.emit(Token.Type.IDENTIFIER);
    }

    /**
     * Lexes an INTEGER or DECIMAL token. Unlike the previous project, we now
     * have integers and decimals instead of just numbers, and leading zeros are
     * not allowed (throw an exception if found). Since both start in the same
     * way, we handle this through a single method and change the token type of
     * the emitted token.
     */
    Token lexNumber() throws ParseException {
        boolean decimal = false;
        while (match("[0-9]")) {}

        if (match("[.]")) {
            decimal = true;
            if (match("[0-9]")) {
                while (match("[0-9]"));
            }
            else throw new ParseException("Decimal has no numbers after decimal point", chars.index);
        }

        if (decimal) {
            return chars.emit(Token.Type.DECIMAL);
        }
        else {
            return chars.emit(Token.Type.INTEGER);
        }
    }

    /**
     * Lexes a STRING token. Unlike the previous project, there are limited
     * characters allowed in strings and escape characters are not supported. If
     * the character is invalid a {@link ParseException} should be thrown.
     */
    Token lexString() throws ParseException {
        if (!chars.has(1)) throw new ParseException("Unterminated string", chars.index);
        else chars.advance();

        while (!peek("\"") && !peek("\n") && !peek("\r") && !peek("\b") && !peek("\t")) {
            if (!chars.has(1))
                throw new ParseException("Unterminated string", chars.index);
            chars.advance();
        }

        if (match("\""))
            return chars.emit(Token.Type.STRING);
        else
            throw new plc.interpreter.ParseException("Not a valid String literal", chars.index);
    }

    /**
     * Lexes an OPERATOR token. Unlike the previous project, we have two
     * multi-character operators: {@code ==} and {@code !=}. If the next
     * characters match either of these, you should emit both characters as a
     * <em>single</em> OPERATOR. As before, this is a 'fallback' for any other
     * unknown characters.
     */
    Token lexOperator() throws ParseException {
        if (chars.has(1)) {
            chars.advance();
        }
        if (peek("=")) {
            chars.advance();
        }
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true in the same way as peek, but also advances the CharStream too
     * if the characters matched.
     */
    boolean match(String... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                chars.advance();
            }
        }
        return peek;
    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    public static final class CharStream {

        final String input;
        int index = 0;
        int length = 0;

        CharStream(String input) {
            this.input = input;
        }

        /**
         * Returns true if there is a character at index + offset, as defined by
         * the length of the input.
         */
        public boolean has(int offset) {
            return index + offset < input.length();
        }

        /**
         * Gets the character at index + offset, throwing an exception if the
         * character does not exist.
         */
        public char get(int offset) {
            return input.charAt(index + offset); //throws if out of bounds
        }

        /**
         * Advances to the next character, incrementing the current index and
         * length of the literal being built.
         */
        public void advance() {
            index++;
            length++;
        }

        /**
         * Resets the length to zero, skipping any consumed characters.
         */
        public void skip() {
            length = 0;
        }

        /**
         * Returns a token of the given type with the built literal. The index
         * of the token should be the starting index.
         */
        public Token emit(Token.Type type) {
            int start = index - length;
            skip(); //we've saved the starting point already
            return new Token(type, input.substring(start, index), start);
        }

    }
}
