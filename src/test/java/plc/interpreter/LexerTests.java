package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from part 1 for
 * more information.
 */
final class LexerTests {

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String input, boolean success) {
        test(input, Token.Type.IDENTIFIER, success);
    }

    private static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("Alphanumeric", "getName", true),
                Arguments.of("Symbols and Alpha", "is-empty?", true),
                Arguments.of("Only Symbols", "<=>", true),
                Arguments.of("Period In Word", "get.name", true),
                Arguments.of("Underscore", "getName_", true),
                Arguments.of("Forward Slash", "get///Name", true),
                Arguments.of("Numbers", "life42", true),
                Arguments.of("Operation", "/", true),
                Arguments.of("Exclamation", "life42!", true),
                Arguments.of("Two Periods", "..", true),
                Arguments.of("Starts With Decimal", ".42", true),
                Arguments.of("Starts With Negative Sign", "-42854", true),
                Arguments.of("Single Letter", "A", true),
                Arguments.of("Ends With Period", "b2/11.", true),
                Arguments.of("Starts With Digit", "42=life", false),
                Arguments.of("Single Digit", "4", false),
                Arguments.of("No Commas Allowed", "why,are,there,commas,", false),
                Arguments.of("No Space Allowed", "get Name", false),
                Arguments.of("Backslash", "li\fe", false),
                Arguments.of("Single Period", ".", false),
                Arguments.of("2 Backslashes", "li\\fe", false),
                Arguments.of("Brackets", "[]", false),
                Arguments.of("Quote", "\"", false),
                Arguments.of("Empty", "", false),
                Arguments.of("Parentheses", "()", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNumber(String input, boolean success) {
        test(input, Token.Type.NUMBER, success);
    }

    private static Stream<Arguments> testNumber() {
        return Stream.of(
            Arguments.of("Integer", "1", true),
            Arguments.of("Large Integer", "982345786", true),
            Arguments.of("Postive Integer", "+436", true),
            Arguments.of("Negative Integer", "-436", true),
            Arguments.of("Negative Decimal", "-1.0", true),
            Arguments.of("Positive Decimal", "+1.0", true),
            Arguments.of("Decimal", "007.000", true),
            Arguments.of("Zero", "0", true),
            Arguments.of("Leading Zero Decimal", "0.01", true),
            Arguments.of("Leading Zero Number", "01", true),
            Arguments.of("Positive Leading Zero Decimal", "+0.01", true),
            Arguments.of("Negative Leading Zero Decimal", "-0.01", true),
            Arguments.of("Nothing After Decimal", "1.", false),
            Arguments.of("Nothing Before Decimal", ".5", false),
            Arguments.of("Negative And Nothing After Decimal", "1.-", false),
            Arguments.of("Positive And Nothing Before Decimal", "+.5", false),
            Arguments.of("Positive After", "1+", false),
            Arguments.of("Negative After", "1-", false),
            Arguments.of("Both Signs", "+-1", false),
            Arguments.of("Both Signs Again", "-+1", false),
            Arguments.of("Two Positive", "++1", false),
            Arguments.of("Two Negative", "--1", false),
            Arguments.of("Middle Positive", "1+1", false),
            Arguments.of("Middle Negative", "1-1", false),
            Arguments.of("Positive Only Decimal", "+.", false),
            Arguments.of("Negative Only Decimal", "-.", false),
            Arguments.of("Empty", "", false),
            Arguments.of("Positive Only", "+", false),
            Arguments.of("Negative Only", ".", false),
            Arguments.of("Decimal Only", "-", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String input, boolean success) {
        test(input, Token.Type.STRING, success);
    }

    private static Stream<Arguments> testString() {
        return Stream.of(
                Arguments.of("Empty", "\"\"", true),
                Arguments.of("ABC", "\"abc\"", true),
                Arguments.of("Escape A Letter", "\"Hello,\\nWorld!\"", true),
                Arguments.of("Random Chars", "\"dsi'b38^_.&(*n_ne\"", true),
                Arguments.of("Whitespace", "\"so tired\"", true),
                Arguments.of("Another Escape", "\"Hello,\\bWorld!\"", true),
                Arguments.of("Escape A Char With A Letter", "\"\\r\"", true),
                Arguments.of("Quote In Middle", "\"Hell\"o_World\"", true),
                Arguments.of("Another Escape", "\"\r\"", true),
                Arguments.of("No End Quote", "\"unterminated", false),
                Arguments.of("No Begin Quote", "unterminated\"", false),
                Arguments.of("No Quotes", "unterminated", false),
                Arguments.of("Escape With Invalid Letter", "\"\\d\"", false),
                Arguments.of("Invalid Escape Again", "\"invalid\\escape\"", false),
                Arguments.of("Still Invalid Escape", "\"Hello,\\\\\\World!\"", false),
                Arguments.of("Wrong Escape Char Case", "\"Hello,\\NWorld!\"", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOperator(String input, boolean success) {
        test(input, Token.Type.OPERATOR, success);
    }

    private static Stream<Arguments> testOperator() {
        return Stream.of(
                Arguments.of("(", true),
                Arguments.of("#", true),
                Arguments.of("~", true),
                Arguments.of("]", true),
                Arguments.of(" ", false),
                Arguments.of("\r", false),
                Arguments.of("\t", false)
        );
    }

    @Test
    void testExample1() {
        String input = "(+ 1 -2.0)";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "+", 1),
                new Token(Token.Type.NUMBER, "1", 3),
                new Token(Token.Type.NUMBER, "-2.0", 5),
                new Token(Token.Type.OPERATOR, ")", 9)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample2() {
        String input = "(print \"Hello, World!\")";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "print", 1),
                new Token(Token.Type.STRING, "\"Hello, World!\"", 7),
                new Token(Token.Type.OPERATOR, ")", 22)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample3() {
        String input = "(let [x 10] (assert-equals? x 10))";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "let", 1),
                new Token(Token.Type.OPERATOR, "[", 5),
                new Token(Token.Type.IDENTIFIER, "x", 6),
                new Token(Token.Type.NUMBER, "10", 8),
                new Token(Token.Type.OPERATOR, "]", 10),
                new Token(Token.Type.OPERATOR, "(", 12),
                new Token(Token.Type.IDENTIFIER, "assert-equals?", 13),
                new Token(Token.Type.IDENTIFIER, "x", 28),
                new Token(Token.Type.NUMBER, "10", 30),
                new Token(Token.Type.OPERATOR, ")", 32),
                new Token(Token.Type.OPERATOR, ")", 33)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    /**
     * Tests that the input lexes to the (single) expected token if successful,
     * else throws a {@link ParseException} otherwise.
     */
    private static void test(String input, Token.Type expected, boolean success) {
        if (success) {
            Assertions.assertIterableEquals(Arrays.asList(new Token(expected, input, 0)), Lexer.lex(input));
        } else {
            Assertions.assertThrows(ParseException.class, () -> {
                List<Token> tokens = Lexer.lex(input);
                if (tokens.size() != 1) {
                    throw new ParseException("Expected a single token.", 0);
                }
            });
        }
    }

}
