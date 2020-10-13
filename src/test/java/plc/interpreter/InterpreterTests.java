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
                )), BigDecimal.valueOf(.5)),
                Arguments.of("Multiple Arguments", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(1))
                )), BigDecimal.valueOf(.5))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testEqual(String test, Ast ast, boolean expected) throws EvalException {
        test(ast, expected, Collections.emptyMap());
    }
    //FIXME: compare bools, identifiers, etc.
    //FIXME: how to throw exceptions?  Everything does what it's supposed to do but test can't pass
    private static Stream<Arguments> testEqual() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("equals?", Arrays.asList()), null),
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

    //FIXME: don't know how pass in identifiers to test this
    private static Stream<Arguments> testNot() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("not", Arrays.asList()), false),
                Arguments.of("Num Not Bool", new Ast.Term("not", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), false),
                Arguments.of("String Not Bool", new Ast.Term("not", Arrays.asList(
                        new Ast.StringLiteral("string")
                )), false)
//                Arguments.of("Term Not Bool", new Ast.Term("not", Arrays.asList(
//                        new Ast.StringLiteral("string")
//                )), false)
//                Arguments.of("False Argument", new Ast.Term("not", Arrays.asList(
//                        new Ast.Identifier("not_false"), false, Collections.singletonMap("not_false", false)
//                )), true)
//                Arguments.of("Two Nums True", new Ast.Term("not", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
//                )), true),
//                Arguments.of("Two Nums False", new Ast.Term("not", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
//                )), false),
//                Arguments.of("One Num and One String", new Ast.Term("not", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.valueOf(10)),
//                        new Ast.Identifier("10")
//                )), false),
//                Arguments.of("Multiple Arguments", new Ast.Term("not", Arrays.asList(
//                        new Ast.NumberLiteral(BigDecimal.ONE),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
//                        new Ast.NumberLiteral(BigDecimal.valueOf(1))
//                )), false)
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