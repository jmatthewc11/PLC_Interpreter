package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
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
            Arguments.of("42=life", false),
            Arguments.of("why,are,there,commas,", false),   //5
            Arguments.of("is-empty?", true),
            Arguments.of("<=>", true),
            Arguments.of("++", true),
            Arguments.of(">=", true),                       //10
            Arguments.of("get.name", true),
            Arguments.of("getName_", true),
            Arguments.of("get///Name", true),
            Arguments.of("life42", true),
            Arguments.of("/", true),                        //15
            Arguments.of("A", true),
            Arguments.of("life42!", true),
            Arguments.of("..", true),
            Arguments.of(".42", true),
            Arguments.of("b2/11.", true),                   //20
            Arguments.of("get Name", false),
            Arguments.of("li\fe", false),
            Arguments.of("li\\fe", false),
            Arguments.of("[]", false),
            Arguments.of("", false),                        //25
            Arguments.of("()", false),
            Arguments.of("\"", false),
            Arguments.of(".", false),
            Arguments.of("4", false),
            Arguments.of("-42854", false)                   //30
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNumber(String input, boolean success) {
        test(input, Token.Type.NUMBER, success);
    }

    private static Stream<Arguments> testNumber() {
        return Stream.of(
//                Arguments.of("1", true),
                Arguments.of("-1.0", true),
                Arguments.of("007.000", true),
                Arguments.of("1.", false),
                Arguments.of(".5", false),          //5
                Arguments.of("1", true),
                Arguments.of("982345786", true),
                Arguments.of("+436", true),
                Arguments.of("-436", true),
                Arguments.of("-1.0", true),         //10
                Arguments.of("+1.0", true),
                Arguments.of("0", true),
                Arguments.of("0.01", true),
                Arguments.of("01", true),
                Arguments.of("+0.01", true),        //15
                Arguments.of("-0.01", true),
                Arguments.of("+-10", false),
                Arguments.of("1.-", false),
                Arguments.of("+.5", false),
                Arguments.of("+-1", false),         //20
                Arguments.of("-+1", false),
                Arguments.of("++1", false),
                Arguments.of("--1", false),
                Arguments.of("1+1", false),
                Arguments.of("1-1", false),         //25
                Arguments.of("+.", false),
                Arguments.of("-.", false),
                Arguments.of("", false),
                Arguments.of("1+", false),
                Arguments.of("1-", false),          //30
                Arguments.of("+", false),
                Arguments.of(".", false),
                Arguments.of("-", false)
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
                Arguments.of("\"Hello,\\\\\nWorld\"", true),
                Arguments.of("\"Hello,\\nWorld\"", true),
                Arguments.of("\"Hello,\nWorld\"", true),        //5
                Arguments.of("\"unterminated", false),
                Arguments.of("\"invalid\\escape\"", false),
                Arguments.of("\"\r\"", true),
                Arguments.of("\"\ba9\n\"", true),
                Arguments.of("\"dsi'b38^_.&(*n_ne\"", true),    //10
                Arguments.of("\"so tired\"", true),
                Arguments.of("\"Hello,\\bWorld\"", true),
                Arguments.of("\"\\r\"", true),
                Arguments.of("\"\r\"", true),
                Arguments.of("\" string \\\"abc 123\"", true),  //15
                Arguments.of("\"Hello \\\" World\"", true),
                Arguments.of("\"invalid escape \\uXYZ\"", false),
                Arguments.of("\" string \\\"", false),
                Arguments.of("\"unterminated", false),
                Arguments.of("unterminated\"", false),          //20
                Arguments.of("unterminated", false),
                Arguments.of("\"\\d\"", false),
                Arguments.of("\"invalid\\escape\"", false),
                Arguments.of("\"Hello,\\\\\\World!\"", false),
                Arguments.of("\"Hello,\\NWorld!\"", false)      //25
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
                Arguments.of("\t", false),
                Arguments.of("|", true),
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

    @Test
    void testExample6() {
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
    void testExample7() {
        String input = "\"(print \\\"\tHello, W\norld!\\\" \"let ++ 'b";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.STRING, "\"(print \\\"\tHello, W\norld!\\\" \"", 0),
                new Token(Token.Type.IDENTIFIER, "let", 29),
                new Token(Token.Type.IDENTIFIER, "++", 33),
                new Token(Token.Type.OPERATOR, "'", 36),
                new Token(Token.Type.IDENTIFIER, "b", 37)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample8() {
        String input = "58.01.29.3";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.NUMBER, "58.01", 0),
                new Token(Token.Type.IDENTIFIER, ".29.3", 5)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @ParameterizedTest
    @MethodSource("plc.interpreter.LexerTests#testPeekAndMatch")
    void testPeek(String test, String input, String[] patterns, boolean matches) {
        Lexer lexer = new Lexer(input);
        Assertions.assertEquals(matches, lexer.peek(patterns));
        Assertions.assertEquals(0, lexer.chars.index);
    }

    @ParameterizedTest
    @MethodSource("plc.interpreter.LexerTests#testPeekAndMatch")
    void testMatch(String test, String input, String[] patterns, boolean matches) {
        Lexer lexer = new Lexer(input);
        Assertions.assertEquals(matches, lexer.match(patterns));
        Assertions.assertEquals(matches ? patterns.length : 0, lexer.chars.index);
    }

    private static Stream<Arguments> testPeekAndMatch() {
        return Stream.of(
                Arguments.of("Single Char Input, Single Char Pattern", "a", new String[] {"a"}, true),
                Arguments.of("Multiple Char Input, Single Char Pattern", "abc", new String[] {"a"}, true),
                Arguments.of("Single Char Input, Multiple Char Pattern", "a", new String[] {"a", "b", "c"}, false),
                Arguments.of("Multiple Char Input, Multiple Char Pattern", "abc", new String[] {"a"}, true),
                Arguments.of("Single Char Input, Char Class Pattern Success", "a", new String[] {"[a-z]"}, true),   //5
                Arguments.of("Single Char Input, Char Class Pattern Failure", "@", new String[] {"[a-z]"}, false),
                Arguments.of("Multiple Char Input, Mixed Pattern Success", "cat", new String[] {"c", "[aeiou]", "t"}, true),
                Arguments.of("Multiple Char Input, Mixed Pattern Failure 1", "cyt", new String[] {"c", "[aeiou]", "t"}, false),
                Arguments.of("Multiple Char Input, Mixed Pattern Failure 2", "cow", new String[] {"c", "[aeiou]", "t"}, false),
                Arguments.of("Multiple Char Input, Mixed Pattern Failure 3", "tot", new String[] {"c", "[aeiou]", "t"}, false),
                Arguments.of("End of Input", "eo", new String[] {"e", "o", "[fi]"}, false)
        );
    }

    @Test
    void testCharStream() {
        Lexer lexer = new Lexer("abc 123");
        lexer.chars.advance();
        lexer.chars.advance();
        lexer.chars.advance();
        Assertions.assertEquals(new Token(Token.Type.IDENTIFIER, "abc", 0), lexer.chars.emit(Token.Type.IDENTIFIER));
        lexer.chars.advance();
        lexer.chars.reset();
        Assertions.assertEquals(0, lexer.chars.length);
        lexer.chars.advance();
        lexer.chars.advance();
        lexer.chars.advance();
        Assertions.assertEquals(new Token(Token.Type.NUMBER, "123", 4), lexer.chars.emit(Token.Type.NUMBER));
        Assertions.assertFalse(lexer.chars.has(0));
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