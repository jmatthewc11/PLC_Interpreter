package plc.interpreter;

import java.util.ArrayList;
import java.util.List;
//import java.util.regex.Pattern;

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
 * The  and  functions are
 * helpers, they're not necessary but their use will make the implementation a
 * lot easier. Regex isn't the most performant way to go but it gets the job
 * done, and the focus here is on the concept.
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
    private List<Token> lex() throws ParseException {
        List<Token> tokens = new ArrayList<>();
        while (chars.has(0)) {
            if (chars.get(0) == ' ' || chars.get(0) == '\t' || chars.get(0) == '\r' || chars.get(0) == '\n') {
                chars.advance();
                chars.reset();
                continue;
            }
            tokens.add(lexToken());
            chars.reset();
        }
        return tokens;
    }

    /**
     * Lexes the next token. It may be helpful to have this call other methods,
     * such as {@code lexIdentifier()} or {@code lexNumber()}, based on the next
     * character(s).
     *
     * Additionally, here is an example of lexing a character literal (not used
     * in this assignment) using the peek/match methods below.
     *
     * <pre>
     * {@code
     *     private plc.interpreter.Token lexCharacter() {
     *         if (!match("\'")) {
     *             //Your lexer should prevent this from happening, as it should
     *             // only try to lex a character literal if the next character
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
     *             //lex escape characters...
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
        if (match("[+-]")) {
            if (chars.has(1) && Character.isDigit(chars.get(1))) {
                return lexNumber();
            }
            else {
                return lexIdentifier();
            }
        }
        else if (match("[0-9]")) {
            return lexNumber();
        }
        else if (match("\"")) {
            return lexString();
        }
        else if (match("[a-zA-z")) {
            return lexIdentifier();
        }
        else if (match("[.]")) {
            if (chars.has(1) && isIdentifier(chars.get(1)))
                return lexIdentifier();
            else
                return chars.emit(Token.Type.OPERATOR);
        }
        else if (match("[+-*/.:!?<>=]")) {
            return lexIdentifier();
        }
        else {
            return chars.emit(Token.Type.OPERATOR);
        }
    }

    boolean isIdentifier(char nextChar) {
        return  Character.isAlphabetic(nextChar) ||
                Character.isDigit(nextChar) ||
                nextChar == '_' || nextChar == '+' ||
                nextChar == '-' || nextChar == '*' ||
                nextChar == '/' || nextChar == '.' ||
                nextChar == ':' || nextChar == '!' ||
                nextChar == '?' || nextChar == '<' ||
                nextChar == '>' || nextChar == '=';
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    boolean peek(String... patterns) {
        if (chars.has(1))
            return Pattern.matches(patterns, Character.toString(chars.get(1)));
        else
            return false;
    }

    boolean match(String... patterns) {

    }

    Token lexNumber() throws ParseException {
        String regex = "[+-]?[0-9]+(\\\\.[0-9]+)?";
        while (chars.has(1) && peek(regex)) {
            chars.content = chars.content + chars.get(1);
            chars.advance();
        }

        if (chars.has(1) && chars.get(1) == '.') {
            if (chars.has(2) && Character.isDigit(chars.get(2))) {
                chars.content = chars.content + '.';
                chars.advance();

                while (chars.has(1) && peek(regex)) {
                    chars.content = chars.content + chars.get(1);
                    chars.advance();
                }
            }
        }
        return chars.emit(Token.Type.NUMBER);
    }

    Token lexIdentifier() {
        String regex = "[\\w+\\-*/.:!?<>=]";

        while (chars.has(1) && peek(regex)) {
            chars.content = chars.content + chars.get(1);
            chars.advance();
        }
        return chars.emit(Token.Type.IDENTIFIER);
    }

    Token lexString() throws ParseException {
        if (chars.input.length() != 1) {
            chars.index++;
            if (chars.has(0)) {
                while (chars.get(0) != ('\"') && matchString()) {
                    chars.content = chars.content + chars.get(0);
                    chars.advance();
                    if (!chars.has(0)) {
                        throw new ParseException("Unterminated literal", chars.start);
                    }
                }
            }
            else {
                throw new ParseException("Not a valid String literal", chars.start);
            }
        }
        else {
            throw new ParseException("Not a valid String literal", chars.start);
        }

        String regex = "\"([^\"\\\\]|\\\\[bnrt\'\"\\\\])*\"";
        if (chars.get(0) == ('\"')) {
            chars.content = chars.content + chars.get(0);
            chars.length++;
            if (chars.matchRegex(regex))
                return chars.emit(Token.Type.STRING);
            else
                throw new ParseException("String literal does not match regex", chars.start);
        }
        else {
            throw new ParseException("Unterminated literal", chars.start);
        }
    }

//    /**
//     * For matching String literal edge cases
//     */
//    private boolean matchString() {
//        if (chars.get(0) == '\n'|| chars.get(0) == '\b' ||
//            chars.get(0) == '\r' || chars.get(0) == '\"' ||
//            chars.get(0) == '\t' || chars.get(0) == '\'')
//            return true;
//        else if (chars.get(0) == '\\') {
//            chars.advance();
//            if (chars.get(0) == '\\' || chars.get(0) == '\'' || chars.get(0) == '\"'
//                    || chars.get(0) == 'b' || chars.get(0) == 'n'
//                    || chars.get(0) == 'r' || chars.get(0) == 't') {
//                chars.content = chars.content + chars.get(-1);
//                return true;
//            }
//            else
//                return false;
//        }
//        else {
//            return true;
//        }
//    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    static final class CharStream {
        private int index = 0;      //where we are in the input
        private int length = 0;     //how many chars are part of the built literal
//        private int start = 0;
        private String input;
//        private String content;

        CharStream(String input) {
            this.input = input;
        }

        /**
         * Returns true if there is a character at index + offset.
         */
        boolean has(int offset) {
            return index + offset < input.length();
        }

        /**
         * Gets the character at index + offset.
         */
        char get(int offset) {
            return input.charAt(index + offset);
        }

        /**
         * Advances to the next character, incrementing the current index and
         * length of the literal being built.
         */
        void advance() {
            index++;
            length++;
        }

        /**
         * Resets the length to zero, skipping any consumed characters.
         */
        void reset() {
            length = 0;
        }

        /**
         * Checks for the end of the input.
         */
//        private boolean endOfInput() {
//            return index >= input.length();
//        }

        /**
         * Checks that the entire built token actually matches the intended regex
         */
//        private boolean matchRegex(String pattern) {
//            return Pattern.matches(pattern, chars.content);
//        }

        /**
         * Returns a token of the given type with the built literal. The index
         * of the token should be the <em>starting</em> index.
         */
        Token emit(Token.Type type) {
            if (type == Token.Type.IDENTIFIER) {
                return new Token(Token.Type.IDENTIFIER, input.substring(index - length, length + 1), index - length);
            }
            else if (type == Token.Type.NUMBER) {
                return new Token(Token.Type.NUMBER, input.substring(index - length, length + 1), index - length);
            }
            else if (type == Token.Type.STRING) {
                return new Token(Token.Type.STRING, input.substring(index - length, length + 1), index - length);
            }
            else {
                return new Token(Token.Type.OPERATOR, input.substring(index - length, length + 1), index - length);
            }
        }
    }
}
