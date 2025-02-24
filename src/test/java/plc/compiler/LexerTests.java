package plc.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from
 * the interpreter part 1 for more information.
 */
final class LexerTests {

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String input, boolean success) {
        test(input, Token.Type.IDENTIFIER, success);
    }

    private static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("getName", true),
                Arguments.of("is-empty?", false),
                Arguments.of("<=>", false),
                Arguments.of("42=life", false),
                Arguments.of("why,are,there,commas,", false),   //5
                Arguments.of("_f", true),
                Arguments.of("getName_", true),
                Arguments.of("life42", true),
                Arguments.of("A", true),
                Arguments.of("42life", false),                  //10
                Arguments.of("get Name", false),
                Arguments.of("li\fe", false),
                Arguments.of("life42!", false),
                Arguments.of("b2/11.", false),
                Arguments.of("li\\fe", false),                  //15
                Arguments.of("get.name", false),
                Arguments.of("..", false),
                Arguments.of("[]", false),
                Arguments.of("/", false),
                Arguments.of("++", false),                      //20
                Arguments.of("get///Name", false),
                Arguments.of("", false),
                Arguments.of("()", false),
                Arguments.of("\"", false),
                Arguments.of(".42", false),                     //25
                Arguments.of(".", false),
                Arguments.of("4", false),
                Arguments.of("-42854", false)                   //28
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInteger(String input, boolean success) {
        test(input, Token.Type.INTEGER, success);
    }

    private static Stream<Arguments> testInteger() {
        return Stream.of(
                Arguments.of("1", true),
                Arguments.of("-1.0", false),
                Arguments.of("007.000", false),
                Arguments.of("1.", false),
                Arguments.of(".5", false),
                Arguments.of("982345786", true),
                Arguments.of("01", true),
                Arguments.of("0", true),
                Arguments.of("+436", false),
                Arguments.of("-436", false),
                Arguments.of("0.01", false),
                Arguments.of("", false),
                Arguments.of("1+", false),
                Arguments.of("1-", false),
                Arguments.of(".", false),           //30
                Arguments.of("1.0.0", false),
                Arguments.of("1..0", false),
                Arguments.of("1+1", false),
                Arguments.of("1-1", false)            //35
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDecimal(String input, boolean success) {
        test(input, Token.Type.DECIMAL, success);
    }

    private static Stream<Arguments> testDecimal() {
        return Stream.of(
                Arguments.of("1", false),
                Arguments.of("-1.0", false),
                Arguments.of("007.000", true),
                Arguments.of("1.", false),
                Arguments.of(".5", false),
                Arguments.of("0.01", true),
                Arguments.of("09.01", true),
                Arguments.of("982345786", false),
                Arguments.of("+436", false),
                Arguments.of("-436", false),
                Arguments.of("-1.0", false),         //10
                Arguments.of("+1.0", false),
                Arguments.of("0", false),
                Arguments.of("01", false),
                Arguments.of("1.-", false),
                Arguments.of("+.5", false),
                Arguments.of("+.", false),
                Arguments.of("", false),
                Arguments.of(".", false),           //30
                Arguments.of("1.0.0", false),
                Arguments.of("1..0", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String input, boolean success) {
        test(input, Token.Type.STRING, success);
    }

    private static Stream<Arguments> testString() {
        return Stream.of(
                Arguments.of("\"\"", true),
                Arguments.of("\"abc\"", true),
                Arguments.of("\"Hello,\\nWorld\"", true),
                Arguments.of("\"unterminated", false),
                Arguments.of("\"invalid\\escape\"", true),
                Arguments.of("\"escaped\\\"quote\"", false),
//                Arguments.of("\"Hello,\\\\\nWorld\"", false), //FIXME: all throw exceptions for escape chars
//                Arguments.of("\"\r\"", false),
//                Arguments.of("\"\ba9\n\"", false),
                Arguments.of("\"dsi'b38^_.&(*n_ne\"", true),    //10
                Arguments.of("\"Hello,\\bWorld\"", true),
                Arguments.of("\"\\r\"", true),
                Arguments.of("\" string \\\"abc 123\"", false),  //15
                Arguments.of("\"Hello \\\" World\"", false),
                Arguments.of("\"invalid escape \\uXYZ\"", true),
                Arguments.of("\" string \\\"", true),
                Arguments.of("\"unterminated", false),
                Arguments.of("unterminated\"", false),          //20
                Arguments.of("unterminated", false),
                Arguments.of("\"\\d\"", true),
                Arguments.of("\"invalid\\escape\"", true),
                Arguments.of("\"Hello,\\\\\\World!\"", true),
                Arguments.of("\"Hello,\\NWorld!\"", true)      //25
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
                Arguments.of(" ", false),
                Arguments.of(" *", false),
                Arguments.of("\t", false),
                Arguments.of("|", true),
                Arguments.of("!=", true),
                Arguments.of("==", true),
                Arguments.of("!=_", false),
                Arguments.of("_", false),
                Arguments.of("&", true),
                Arguments.of("\r", false),
                Arguments.of("\t", false)
        );
    }

    @Test
    void testExample1() {
        String input = "(+ 1 -2.0)";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.OPERATOR, "+", 1),
                new Token(Token.Type.INTEGER, "1", 3),
                new Token(Token.Type.OPERATOR, "-", 5),
                new Token(Token.Type.DECIMAL, "2.0", 6),
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
                new Token(Token.Type.INTEGER, "10", 8),
                new Token(Token.Type.OPERATOR, "]", 10),
                new Token(Token.Type.OPERATOR, "(", 12),
                new Token(Token.Type.IDENTIFIER, "assert", 13),
                new Token(Token.Type.OPERATOR, "-", 19),
                new Token(Token.Type.IDENTIFIER, "equals", 20),
                new Token(Token.Type.OPERATOR, "?", 26),
                new Token(Token.Type.IDENTIFIER, "x", 28),
                new Token(Token.Type.INTEGER, "10", 30),
                new Token(Token.Type.OPERATOR, ")", 32),
                new Token(Token.Type.OPERATOR, ")", 33)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample4() {
        String input = "\n \r[x 10] (23rt x 10.5))";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "[", 3),
                new Token(Token.Type.IDENTIFIER, "x", 4),
                new Token(Token.Type.INTEGER, "10", 6),
                new Token(Token.Type.OPERATOR, "]", 8),
                new Token(Token.Type.OPERATOR, "(", 10),
                new Token(Token.Type.INTEGER, "23", 11),
                new Token(Token.Type.IDENTIFIER, "rt", 13),
                new Token(Token.Type.IDENTIFIER, "x", 16),
                new Token(Token.Type.DECIMAL, "10.5", 18),
                new Token(Token.Type.OPERATOR, ")", 22),
                new Token(Token.Type.OPERATOR, ")", 23)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample5() {
        String input = "(print \"Hello, World!\" let x 'b''";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "print", 1),
                new Token(Token.Type.STRING, "\"Hello, World!\"", 7),
                new Token(Token.Type.IDENTIFIER, "let", 23),
                new Token(Token.Type.IDENTIFIER, "x", 27),
                new Token(Token.Type.OPERATOR, "'", 29),
                new Token(Token.Type.IDENTIFIER, "b", 30),
                new Token(Token.Type.OPERATOR, "'", 31),
                new Token(Token.Type.OPERATOR, "'", 32)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample6() {
        String input = "58.01.29.3";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.DECIMAL, "58.01", 0),
                new Token(Token.Type.OPERATOR, ".", 5),
                new Token(Token.Type.DECIMAL, "29.3", 6)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    /**
     * Tests that the input lexes to the (single) expected token if successful,
     * else throws a {@link ParseException} otherwise.
     */
    private static void test(String input, Token.Type expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(Arrays.asList(new Token(expected, input, 0)), Lexer.lex(input));
            } else {
                Assertions.assertNotEquals(Arrays.asList(new Token(expected, input, 0)), Lexer.lex(input));
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

}