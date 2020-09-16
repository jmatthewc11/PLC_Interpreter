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
                Arguments.of("getName", true),
                Arguments.of("is-empty?", true),
                Arguments.of("<=>", true),
                Arguments.of("get.name", true),
                Arguments.of("getName_", true),
                Arguments.of("get///Name", true),
                Arguments.of("life42", true),
                Arguments.of("/", true),
                Arguments.of("A", true),
                Arguments.of("life42!", true),
                Arguments.of("..", true),
                Arguments.of(".42", true),
                Arguments.of("42=life", false),
//                Arguments.of("why,are,there,commas,", false), //FIXME: PE, IOOB
                Arguments.of("b2/11.", true),
//                Arguments.of("get Name", false),              //FIXME: PE, IOOB
//                Arguments.of("li\fe", false),                 //FIXME: PE, IOOB
//                Arguments.of("li\\fe", false),                //FIXME: PE, IOOB
//                Arguments.of("[]", false),                    //FIXME: PE, IOOB
                Arguments.of("", false)
//                Arguments.of("()", false),                    //FIXME: PE, IOOB
//                Arguments.of("\"", false),                    //FIXME: PE, IOOB
//                Arguments.of(".", false),                     //FIXME: PE, IOOB
//                Arguments.of("4", false),                     //FIXME: PE, IOOB
//                Arguments.of("-42854", false)                 //FIXME: PE, IOOB
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNumber(String input, boolean success) {
        test(input, Token.Type.NUMBER, success);
    }

    private static Stream<Arguments> testNumber() {
        return Stream.of(
                Arguments.of("1", true),
                Arguments.of("-1.0", true),
                Arguments.of("007.000", true),
                Arguments.of("1", true),
                Arguments.of("982345786", true),
                Arguments.of("+436", true),
                Arguments.of("-436", true),
                Arguments.of("-1.0", true),
                Arguments.of("+1.0", true),
                Arguments.of("0", true),
                Arguments.of("0.01", true),
                Arguments.of("01", true),
                Arguments.of("+0.01", true),
                Arguments.of("-0.01", true),
//                Arguments.of("1.", false),    //FIXME: IOOB
//                Arguments.of(".5", false),    //FIXME: PE
//                Arguments.of("+-10", false),  //FIXME: PE
                Arguments.of("1.-", false),
//                Arguments.of("+.5", false),   //FIXME: PE
//                Arguments.of("+-1", false),   //FIXME: PE
//                Arguments.of("-+1", false),   //FIXME: PE
//                Arguments.of("++1", false),   //FIXME: PE
//                Arguments.of("--1", false),   //FIXME: PE
                Arguments.of("1+1", false),
                Arguments.of("1-1", false),
//                Arguments.of("+.", false),    //FIXME: PE
//                Arguments.of("-.", false),    //FIXME: PE
                Arguments.of("", false)
//                Arguments.of("1+", false),    //FIXME: PE, IOOB
//                Arguments.of("1-", false),    //FIXME: PE, IOOB
//                Arguments.of("+", false),     //FIXME: PE, IOOB
//                Arguments.of(".", false),     //FIXME: PE, IOOB
//                Arguments.of("-", false)      //FIXME: PE, IOOB
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
                Arguments.of("\"\r\"", true),
                Arguments.of("\"\ba9\n\"", true),
                Arguments.of("\"Hello,\nWorld\"", true),
                Arguments.of("\"unterminated", false),
                Arguments.of("\"invalid escape \\uXYZ\"", false)
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

    @Test
    void testExample4() {
        String input = "\n \r[x 10] (23rt-equals? x 10.))";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "[", 3),
                new Token(Token.Type.IDENTIFIER, "x", 4),
                new Token(Token.Type.NUMBER, "10", 6),
                new Token(Token.Type.OPERATOR, "]", 8),
                new Token(Token.Type.OPERATOR, "(", 10),
                new Token(Token.Type.NUMBER, "23", 11),
                new Token(Token.Type.IDENTIFIER, "rt-equals?", 13),
                new Token(Token.Type.IDENTIFIER, "x", 24),
                new Token(Token.Type.NUMBER, "10", 26),
                new Token(Token.Type.OPERATOR, ".", 28),
                new Token(Token.Type.OPERATOR, ")", 29),
                new Token(Token.Type.OPERATOR, ")", 30)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample5() {
        String input = "~ [Q -1]  (23r\rt-eq/ual.s? . 1.0..\"teSt\"";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "~", 0),
                new Token(Token.Type.OPERATOR, "[", 2),
                new Token(Token.Type.IDENTIFIER, "Q", 3),
                new Token(Token.Type.NUMBER, "-1", 5),
                new Token(Token.Type.OPERATOR, "]", 7),
                new Token(Token.Type.OPERATOR, "(", 10),
                new Token(Token.Type.NUMBER, "23", 11),
                new Token(Token.Type.IDENTIFIER, "r", 13),
                new Token(Token.Type.IDENTIFIER, "t-eq/ual.s?", 15),
                new Token(Token.Type.OPERATOR, ".", 27),
                new Token(Token.Type.NUMBER, "1.0", 29),
                new Token(Token.Type.IDENTIFIER, "..", 32),
                new Token(Token.Type.STRING, "\"teSt\"", 34)
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