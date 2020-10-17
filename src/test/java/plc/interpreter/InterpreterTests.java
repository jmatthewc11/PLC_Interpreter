package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
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
    void testEqual(String test, Ast ast, boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }
    //TODO: compare bools, identifiers, terms, etc.
    private static Stream<Arguments> testEqual() {  //NOTE: errors return correctly (1, 2, 6)
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("equals?", Arrays.asList()), false, Collections.emptyMap()),
                Arguments.of("Single Argument", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), false, Collections.emptyMap()),
                Arguments.of("Two Nums True", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), true, Collections.emptyMap()),
                Arguments.of("Two Nums False", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), false, Collections.emptyMap()),
                Arguments.of("One Num and One String", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(10)),
                        new Ast.Identifier("10")
                )), false, Collections.singletonMap("10", "10")),
                Arguments.of("Multiple Arguments", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(1))
                )), false, Collections.emptyMap())
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNot(String test, Ast ast, boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testNot() {    //NOTE: errors return correctly (1, 2, 3)
        return Stream.of(
                    Arguments.of("Zero Arguments", new Ast.Term("not", Arrays.asList()), false, Collections.emptyMap()),
                    Arguments.of("Num Not Bool", new Ast.Term("not", Arrays.asList(
                            new Ast.NumberLiteral(BigDecimal.valueOf(2))
                    )), false, Collections.emptyMap()),
                    Arguments.of("String Not Bool", new Ast.Term("not", Arrays.asList(
                            new Ast.StringLiteral("string")
                    )), false, Collections.emptyMap()),
                    Arguments.of("Switch to True", new Ast.Term("not", Arrays.asList(
                            new Ast.Identifier("truth")
                    )), false, Collections.singletonMap("truth", true)),
                    Arguments.of("Switch to False", new Ast.Term("not", Arrays.asList(
                            new Ast.Identifier("falsey")
                    )), true, Collections.singletonMap("falsey", false))
                );
    }

    @ParameterizedTest
    @MethodSource
    void testAnd(String test, Ast ast, boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testAnd() {    //NOTE: errors return correctly (5)
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("and", Arrays.asList()), true, Collections.emptyMap()),
                Arguments.of("One Arg True", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("truth")
                )), true, Collections.singletonMap("truth", true)),
                Arguments.of("One Arg False", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("truth"),
                        new Ast.Identifier("falsey")
                        )), false, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("truth", true);
                            put("falsey", false);
                        }})),
                Arguments.of("Two Args True", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("truth"),
                        new Ast.Identifier("truth2")
                        )), true, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("truth", true);
                            put("truth2", true);
                        }})),
                Arguments.of("String Not Bool", new Ast.Term("and", Arrays.asList(
                        new Ast.StringLiteral("string")
                )), false, Collections.emptyMap()),
                Arguments.of("First Arg False", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("falsey"),
                        new Ast.Identifier("truth")
                )), false, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("falsey", false);
                    put("truth", true);
                }}))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOr(String test, Ast ast, boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testOr() {     //NOTE: errors return correctly (1, 2, 3)
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("not", Arrays.asList()), false, Collections.emptyMap()),
                Arguments.of("Num Not Bool", new Ast.Term("not", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), false, Collections.emptyMap()),
                Arguments.of("String Not Bool", new Ast.Term("not", Arrays.asList(
                        new Ast.StringLiteral("string")
                )), false, Collections.emptyMap()),
                Arguments.of("One Arg True", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("truth")
                )), true, Collections.singletonMap("truth", true)),
                Arguments.of("One Arg False", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("truth"),
                        new Ast.Identifier("falsey")
                )), false, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("truth", true);
                    put("falsey", false);
                }})),
                Arguments.of("Two Args True", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("truth"),
                        new Ast.Identifier("truth2")
                )), true, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("truth", true);
                    put("truth2", true);
                }})),
                Arguments.of("First Arg False", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("falsey"),
                        new Ast.Identifier("truth")
                )), false, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("falsey", false);
                    put("truth", true);
                }}))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLL(String test, Ast ast, LinkedList<Object> expected, Map<String, Object> map) {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testLL() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("list", Arrays.asList()), new LinkedList<>(), Collections.emptyMap()),
                Arguments.of("Single Argument", new Ast.Term("list", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), new LinkedList<Object>(Arrays.asList(BigDecimal.valueOf(2))), Collections.emptyMap()),
                Arguments.of("Multiple Arguments", new Ast.Term("list", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(8)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), new LinkedList<Object>(Arrays.asList(BigDecimal.valueOf(2), BigDecimal.valueOf(8), BigDecimal.valueOf(2))), Collections.emptyMap()),
                Arguments.of("Multiple Data Types", new Ast.Term("list", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.StringLiteral("string"),
                        new Ast.Identifier("truth")
                )), new LinkedList<Object>(Arrays.asList(BigDecimal.valueOf(2), "string", true)), Collections.singletonMap("truth", true))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testRange(String test, Ast ast, LinkedList<Object> expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testRange() {  //NOTE: errors return correctly (1, 2, 4)
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("range", Arrays.asList()), new LinkedList<>()),
                Arguments.of("Single Argument", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), new LinkedList<>()),
                Arguments.of("Multiple Arguments", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(8))
                )), new LinkedList<Object>(Arrays.asList(
                        BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4),
                        BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7)))),
                Arguments.of("Non Int", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(10.5))
                )), new LinkedList<Object>())
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