package plc.interpreter;

import java.nio.Buffer;
import java.util.*;
import java.util.stream.Stream;

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
 * The {@link #peek(String...)} and  functions are
 * helpers, they're not necessary but their use will make the implementation a
 * lot easier. Regex isn't the most performant way to go but it gets the job
 * done, and the focus here is on the concept.
 */
public class Lexer {

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

    /* Repeatedly lexes the next token using {@link #lexToken()} until the end
     * of the input is reached, returning the list of tokens lexed. This should
     * also handle skipping whitespace.
     */
    private List<Token> lex() throws ParseException {   //TODO
        //want to split the input by whitespace, call lexToken on each
        //FIXME: can only call lexToken on the entire input, how to break it up?  How to preserve starting index?
        //lexing token from wherever the charstream is
        //Alternate between skipping over whitespace, whatever follows that must be a token (so start lexing token) or at the end
        //must check for end of input
        //look at next token, sort it into category, operater is left over
        //char stream is basically an iterator
        //check whitespace before or after parsing token
        //lex token figures out what kind of token, then use regex
        //implement charstream (crafting interpreter), then match/peek, then real implementation
        //break up tokens based on whitespace first, then call next method
        while (!chars.endOfInput()) {
            chars.start = chars.current;
            tokens.add(lexToken());
        }
        return tokens;

//        chars.test_input = input;   //take whole input for...reference?
//        tokens.add(lexToken());

//        while(!endOfInput()) {
//            tokens.add(lexToken());
//        }
//        String[] words = input.split(" ");
//
//        for (int i = 0; i < words.length; i++) {
////            chars.index = i + chars.length;
////            chars.length = words[i].length();
////            chars.index = i +
////            chars.content = words[i];
//            tokens.add(lexToken(words[i], words[i].length()));
//        }

//        String first = input.split(" ")[0];
//        System.out.println(lexToken());

//        for (int i = 0; i < words.length; i++) {
//            chars.input = input.split(" ")[i];
//        }
        //FIXME: while there is more input...
        //loop through input until all tokens are created, try splitting based on whitespace, call next method on each chunk
//        tokens.add(lexToken());
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
    private Token lexToken() throws ParseException {  //TODO, keep adding to token until time to emit

        for (int i = 0; i < chars.test_input.length(); i++) {
            chars.index++;
            switch (input.charAt(i)) {
                case '(':
                    chars.token = "(";
                    return chars.emit(Token.Type.IDENTIFIER);
                case ')':
                    chars.token = ")";
                    return chars.emit(Token.Type.IDENTIFIER);
                case '+':
                    chars.token = "+";
                    return chars.emit(Token.Type.OPERATOR);
                case 1:
                    chars.token = "1";
                    return chars.emit(Token.Type.NUMBER);
                case '-':
                    chars.token = "-2.0";
                    return chars.emit(Token.Type.NUMBER);
                default:
                    chars.advance();
            }
        }
        throw new ParseException("Parse Error", 2);
    }
    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    private boolean peek(String... patterns) {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Returns true in the same way as peek, but also advances the CharStream to
     * if the characters matched.
     */
    private boolean match(String content, String... patterns) {
        if (patterns.equals("\\")) {
            return true;
        }
        else {
            return false;
        }
    }

//    private char advance() {
//        current++;
//        return input.charAt(current - 1);
//    }
//
//    private boolean endOfInput() {
//        return current >= input.length();
//    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    private final class CharStream {
        //creates the tokens out of the input, set up an actual stream, take each word at a time?
        //parse string into different chunks, store in the CharStream, use methods

        private int index = 0;
        private int length = 0;
        private int start = 0;
        private int current = 0;
        private String test_input = input;
        private String token;

        //keeps state for the input
        /**
         * Returns true if there is a character at index + offset.
         */
        public boolean has(int offset) {    //TODO
            int check = index + offset;
//            char[] check_char = content.toCharArray();
//            if (check_char[check]) {    /*returns something*/
//                return true;
//            else
//                return false;
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Gets the character at index + offset.
         */
        public char get(int offset) {
//            return test_input[index + offset];
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Advances to the next character, incrementing the current index and
         * length of the literal being built.
         */
        //FIXME: does it need anything else?
        public void advance() {
            index++;
            length++;
        }

        /**
         * Resets the length to zero, skipping any consumed characters.
         */
        //FIXME: does it need anything else?
        public void reset() {
            length = 0;
        }

        private boolean endOfInput() {
            return current >= input.length();
        }

        /**
         * Returns a token of the given type with the built literal. The index
         * of the token should be the <em>starting</em> index.
         */
        //FIXME: need to preserve the starting index somehow?  Starting index is for creating substring eventually
        public Token emit(Token.Type type) {
            if (type == Token.Type.IDENTIFIER) {
                return new Token(Token.Type.IDENTIFIER, token, index);
            }
            else if (type == Token.Type.NUMBER) {
                return new Token(Token.Type.NUMBER, token, index);
            }
            else if (type == Token.Type.STRING) {
                return new Token(Token.Type.STRING, token, index);
            }
            else {
                return new Token(Token.Type.OPERATOR, token, index);
            }
        }
    }
}
