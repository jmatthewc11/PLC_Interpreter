package plc.interpreter;

import java.util.*;
import java.util.regex.Pattern;
/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - , which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException}.
 *
 * The {@link #(Pattern)} and  functions are
 * helpers, they're not necessary but their use will make the implementation a
 * lot easier. Regex isn't the most performant way to go but it gets the job
 * done, and the focus here is on the concept.
 */
public class Lexer {    //TODO: ParseExceptions, probably need to take out leading char right before switch

    private final String input;
    private final CharStream chars = new CharStream();
    List<Token> tokens = new ArrayList<>();

    private Lexer(String input) {
        this.input = input;
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
        while (!chars.endOfInput()) {
            chars.start = chars.index;
            if (chars.get(0) == ' ' || chars.get(0) == '\t' || chars.get(0) == '\r') {
                chars.advance();
                chars.reset();
                continue;
            }
            tokens.add(lexToken());
            chars.reset();
            chars.content = "";
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
     *             //your lexer should prevent this from happening, as it should
     *             //only try to lex a character literal if the next character
     *             //begins a character literal.
     *             throw new RuntimeException("Next character does not begin a character literal.");
     *         }
     *         if (!chars.has(0) || match("\'")) {
     *             throw new RuntimeException("Empty character literal.");
     *         } else if (match("\\")) {
     *             //lex escape characters...basically calling the RegEx stuff here to get a match
     *         } else {
     *             chars.advance();
     *         }
     *         if (!match("\'")) {
     *             throw new RuntimeException("Unterminated character literal.");
     *         }
     *         return new plc.interpreter.Token(plc.interpreter.Token.Type.CHARACTER, chars.emit());
     *     }
     * }
     * </pre>
     */
    private Token lexToken() throws ParseException {

        char c = input.charAt(chars.index);
        chars.content = Character.toString(c);
        chars.length++;
        if (c == '+' | c == '-') {
            if (Character.isDigit(chars.get(1))) {
                return lexNumber();
            }
            else {
                return lexIdentifier();
            }
        }
        else if (Character.isDigit(c)) {
            return lexNumber();
        }
        else if (c == '\"') {
            return lexLiteral(c);
        }
        else if (Character.isAlphabetic(c)) {
            return lexIdentifier();
        }
        else {
            switch (c) {
                case '.':   //need to check if something follows it
                    if (chars.has(1))
                        return lexIdentifier();
                    else
                        return lexOperator();
                case '*':
                case '/':
                case ':':
                case '!':
                case '?':
                case '<':
                case '>':
                case '=':
                        return lexIdentifier();
                default:
                    return lexOperator();
            }
        }
    }
    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    private boolean peek(String patterns) {
        if (chars.has(1))
            return Pattern.matches(patterns, Character.toString(chars.get(1)));
        else
            return false;
    }

    /**
     * Returns true in the same way as peek, but also advances the CharStream to
     * if the characters matched.
     */
    private boolean match(String patterns) {
        if (chars.endOfInput()) return false;
        if (!Pattern.matches(String.valueOf(patterns), Character.toString(chars.get(0)))) return false;

        chars.content = chars.content + chars.get(0);
        chars.index++;
        return true;
    }

    private Token lexNumber() throws ParseException {
        String regex = "([\\+]|[\\-]){0,1}[\\d]+([.][\\d]+)*";
        while (chars.has(1) && peek(regex)) {
            chars.content = chars.content + chars.get(1);
            chars.advance();
        }

        if (chars.has(1) && chars.get(1) == '.' && chars.has(2) && Character.isDigit(chars.get(2))) {
            chars.content = chars.content + '.';
            chars.advance();

            while (chars.has(1) && peek(regex)) {
                chars.content = chars.content + chars.get(1);
                chars.advance();
            }
        }
        if (chars.content.charAt(chars.length - 1) == '.') {
            throw new ParseException("Number ends in a period", 187);
        }
        return chars.emit(Token.Type.NUMBER);
    }

    private Token lexIdentifier() {
        String regex = "[\\w+\\-*/.:!?<>=]";
        chars.content = "";

        while (match(regex)) {}
        return chars.emit(Token.Type.IDENTIFIER);
    }

    private Token lexOperator() {
        return chars.emit(Token.Type.OPERATOR);
    }

    private Token lexLiteral(char c) {  //TODO
//        while (isAlphaNumeric(peek())) advance();
        chars.content = Character.toString(c);
        return chars.emit(Token.Type.STRING);
    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    private final class CharStream {
        private int index = 0;
        private int length = 0;
        private int start = 0;
        private String content;

        /**
         * Returns true if there is a character at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < input.length();
        }

        /**
         * Gets the character at index + offset.
         */
        public char get(int offset) {
            return input.charAt(index + offset);
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
        public void reset() {
            length = 0;
        }

        private boolean endOfInput() {
            return index >= input.length();
        }

        /**
         * Returns a token of the given type with the built literal. The index
         * of the token should be the <em>starting</em> index.
         */
        public Token emit(Token.Type type) {
            chars.index++;
            if (type == Token.Type.IDENTIFIER) {
                return new Token(Token.Type.IDENTIFIER, content, chars.start);
            }
            else if (type == Token.Type.NUMBER) {
                return new Token(Token.Type.NUMBER, content, chars.start);
            }
            else if (type == Token.Type.STRING) {
                return new Token(Token.Type.STRING, content, chars.start);
            }
            else {
                return new Token(Token.Type.OPERATOR, content, chars.start);
            }
        }
    }
}
