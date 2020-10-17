package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

final class InterpreterTests {

    @Test
    void testTerm() {
        test(new Ast.Term("print", Arrays.asList()), Interpreter.VOID, Collections.emptyMap());
    }

    @Test
    void testIdentifier() {
        test(new Ast.Identifier("num"), 10, Collections.singletonMap("num", 10));
    }

    @Test
    void testNumber() {
        test(new Ast.NumberLiteral(BigDecimal.ONE), BigDecimal.ONE, Collections.emptyMap());
    }

    @Test
    void testString() {
        test(new Ast.StringLiteral("string"), "string", Collections.emptyMap());
    }

    @ParameterizedTest
    @MethodSource
    void testAddition(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testAddition() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("+", Arrays.asList()), BigDecimal.valueOf(0)),
                Arguments.of("Multiple Arguments", new Ast.Term("+", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(6))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSubtraction(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testSubtraction() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("-", Arrays.asList()), null),
                Arguments.of("Single Argument", new Ast.Term("-", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE)
                )), BigDecimal.valueOf(-1)),
                Arguments.of("Multiple Arguments", new Ast.Term("-", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(-4))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testMulti(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testMulti() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("*", Arrays.asList()), BigDecimal.valueOf(1)),
                Arguments.of("Multiple Arguments", new Ast.Term("*", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(10)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(-1)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(7))
                )), BigDecimal.valueOf(-70))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDivide(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testDivide() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("/", Arrays.asList()), null),
                Arguments.of("Single Argument", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), BigDecimal.valueOf(0)),
                Arguments.of("Multiple Arguments", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(new BigDecimal("1.000")),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(.167))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testEqual(String test, Ast ast, boolean expected) throws EvalException {
        test(ast, expected, Collections.emptyMap());
    }
    //FIXME: compare bools, identifiers, terms, etc.
    //FIXME: how to throw exceptions?  Everything does what it's supposed to do but test can't pass
    private static Stream<Arguments> testEqual() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("equals?", Arrays.asList()), false),
                Arguments.of("Single Argument", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), false),
                Arguments.of("Two Nums True", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), true),
                Arguments.of("Two Nums False", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), false),
                Arguments.of("One Num and One String", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(10)),
                        new Ast.Identifier("10")
                )), false),
                Arguments.of("Multiple Arguments", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(1))
                )), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNot(String test, Ast ast, boolean expected) throws EvalException {
        test(ast, expected, Collections.emptyMap());
    }

    //FIXME: How to pass in identifiers with true/false values to test this?
    private static Stream<Arguments> testNot() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("not", Arrays.asList()), false),
                Arguments.of("Num Not Bool", new Ast.Term("not", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), false),
                Arguments.of("String Not Bool", new Ast.Term("not", Arrays.asList(
                        new Ast.StringLiteral("string")
                )), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testAnd(String test, Ast ast, boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testAnd() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("and", Arrays.asList()), true, Collections.emptyMap()),
                Arguments.of("One Arg True", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("truth")
                )), true, Collections.singletonMap("truth", true)),
                Arguments.of("String Not Bool", new Ast.Term("and", Arrays.asList(
                        new Ast.StringLiteral("string")
                )), false, Collections.emptyMap())  //FIXME: returns wrong value
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOr(String test, Ast ast, boolean expected) throws EvalException {
        test(ast, expected, Collections.emptyMap());
    }

    //FIXME: How to pass in identifiers with true/false values to test this?
    private static Stream<Arguments> testOr() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("not", Arrays.asList()), false),
                Arguments.of("Num Not Bool", new Ast.Term("not", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), false),
                Arguments.of("String Not Bool", new Ast.Term("not", Arrays.asList(
                        new Ast.StringLiteral("string")
                )), false)
        );
    }

    private static void test(Ast ast, Object expected, Map<String, Object> map) {
        Scope scope = new Scope(null);
        map.forEach(scope::define);
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out), scope);
        if (expected != null) {
            Assertions.assertEquals(expected, interpreter.eval(ast));
        } else {
            Assertions.assertThrows(EvalException.class, () -> interpreter.eval(ast));
        }
    }
}