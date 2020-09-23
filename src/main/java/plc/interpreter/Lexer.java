package plc.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    List<Token> lex() throws ParseException {
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
        if (peek("[+-]")) {
            chars.advance();
            if (peek("[0-9]")) {
                chars.advance();
                return lexNumber();
            }
            else {
                return lexIdentifier();
            }
        }
        else if (peek("[0-9]")) {
            chars.advance();
            return lexNumber();
        }
        else if (peek("\"")) {
            chars.advance();
            return lexString();
        }
        else if (peek("[a-zA-Z]")) {
            return lexIdentifier();
        }
        else if (peek("[.]", "[A-Za-z0-9_+*./:!?<>=-]")) {
            return lexIdentifier();
        }
        else if (peek("[+*/:!?<>=-]")) {
            return lexIdentifier();
        }
        else {
            chars.advance();
            return chars.emit(Token.Type.OPERATOR);
        }
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    boolean peek(String... patterns) {
        int i = 0;
        int count = 0;
        int offset = 0;
        for (i = 0; i < patterns.length; i++) {
            if (chars.has(offset)) {
                if (Pattern.compile(patterns[i]).matcher(Character.toString(chars.get(offset))).matches()) {
                    count++;
                    offset++;
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        return (count == 0 || i == patterns.length);
    }

    /**
     * Returns true in the same way as peek, but also advances the CharStream too
     * if the characters matched.
     */
    boolean match(String... patterns) {
        int i = 0;
        int count = 0;
        int offset = 0;
        for (i = 0; i < patterns.length; i++) {   //only 1 char at a time, they move together
            if (chars.has(offset)) {
                if (Pattern.compile(patterns[i]).matcher(Character.toString(chars.get(offset))).matches()) {
                    count++;
                    offset++;
                    while (chars.has(offset) && Pattern.compile(patterns[i]).matcher(Character.toString(chars.get(offset))).matches()) {
                        count++;
                        offset++;
                    }
                }
                else {
                    return false;
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
            chars.advance();
            count--;
        }
        return true;
    }

    Token lexNumber() throws ParseException {
        while (match("[0-9]")) {}
        while (match("[.]", "[0-9]+")) {}

        return chars.emit(Token.Type.NUMBER);
    }

    Token lexIdentifier() {
        while (match("[A-Za-z0-9_+*/.:!?<>=-]")) {}

        return chars.emit(Token.Type.IDENTIFIER);
    }

    Token lexString() throws ParseException {
        while (!peek("\"")) {
            if (peek("\n") || peek("\r") || peek("\b") || peek("\t")) {
                chars.advance();
            }
            else if (peek("\\\\")) {
                chars.advance();
                if (peek("\"") || peek("\'") || peek("r") || peek("b") ||
                    peek("t") || peek("n") || peek("\\\\")) {
                    chars.advance();
                }
                else
                    throw new ParseException("Not a valid String literal", chars.index);
            }
            else
                if (!match("[^\\\"\\\\]"))
                    break;
        }

        if (match("\""))
                return chars.emit(Token.Type.STRING);
        else
            throw new ParseException("Not a valid String literal", chars.index);
    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    static final class CharStream {
        final String input;
        int index = 0;      //where we are in the input
        int length = 0;     //how many chars are part of the built literal

        CharStream(String input) {
            this.input = input;
        }

        /**
         * Returns true if there is a character at index + offset.
         */
        boolean has(int offset) { return index + offset < input.length(); }

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
         * Returns a token of the given type with the built literal. The index
         * of the token should be the <em>starting</em> index.
         */
        Token emit(Token.Type type) {
            if (type == Token.Type.IDENTIFIER) {
                return new Token(Token.Type.IDENTIFIER, input.substring(index - length, index), index - length);
            }
            else if (type == Token.Type.NUMBER) {
                return new Token(Token.Type.NUMBER, input.substring(index - length, index), index - length);
            }
            else if (type == Token.Type.STRING) {
                return new Token(Token.Type.STRING, input.substring(index - length, index), index - length);
            }
            else {
                return new Token(Token.Type.OPERATOR, input.substring(index - length, index), index - length);
            }
        }
    }
}
