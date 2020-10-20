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
                        new Ast.StringLiteral("10"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), null),
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
            )), BigDecimal.valueOf(-4)),
            Arguments.of("Zero Arguments", new Ast.Term("-", Arrays.asList(
                    new Ast.Term("-", Arrays.asList(
                            new Ast.NumberLiteral(BigDecimal.ONE),
                            new Ast.NumberLiteral(BigDecimal.valueOf(2))
                    ))
            )), BigDecimal.ONE)
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
                Arguments.of("One Argument", new Ast.Term("*", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(8))
                )), BigDecimal.valueOf(8)),
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
    void testTrue(String test, Ast ast, Boolean expected) throws EvalException {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testTrue() {
        return Stream.of(
                Arguments.of("Term", new Ast.Term("true", Arrays.asList()), null),
                Arguments.of("Identifier", new Ast.Identifier("true"), true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFalse(String test, Ast ast, Boolean expected) throws EvalException {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testFalse() {
        return Stream.of(
                Arguments.of("Term", new Ast.Term("false", Arrays.asList()), null),
                Arguments.of("Identifier", new Ast.Identifier("false"), false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testEqual(String test, Ast ast, Boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }
    //TODO: compare bools, identifiers, terms, etc.
    private static Stream<Arguments> testEqual() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("equals?", Arrays.asList()), null, Collections.emptyMap()),
                Arguments.of("Single Argument", new Ast.Term("equals?", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), null, Collections.emptyMap()),
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
                )), null, Collections.emptyMap())
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNot(String test, Ast ast, Boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testNot() {
        return Stream.of(
            Arguments.of("Zero Arguments", new Ast.Term("not", Arrays.asList()), null, Collections.emptyMap()),
            Arguments.of("Num Not Bool", new Ast.Term("not", Arrays.asList(
                    new Ast.NumberLiteral(BigDecimal.valueOf(2))
            )), null, Collections.emptyMap()),
            Arguments.of("String Not Bool", new Ast.Term("not", Arrays.asList(
                    new Ast.StringLiteral("string")
            )), null, Collections.emptyMap()),
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
    void testAnd(String test, Ast ast, Boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testAnd() {
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
                )), null, Collections.emptyMap()),
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
    void testOr(String test, Ast ast, Boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testOr() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("or", Arrays.asList()), false, Collections.emptyMap()),
                Arguments.of("Num Not Bool", new Ast.Term("or", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), null, Collections.emptyMap()),
                Arguments.of("String Not Bool", new Ast.Term("or", Arrays.asList(
                        new Ast.StringLiteral("string")
                )), null, Collections.emptyMap()),
                Arguments.of("One Arg True", new Ast.Term("or", Arrays.asList(
                        new Ast.Identifier("truth")
                )), true, Collections.singletonMap("truth", true)),
                Arguments.of("First Arg True", new Ast.Term("or", Arrays.asList(
                        new Ast.Identifier("truth"),
                        new Ast.Identifier("falsey")
                )), true, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("truth", true);
                    put("falsey", false);
                }})),
                Arguments.of("Two Args True", new Ast.Term("or", Arrays.asList(
                        new Ast.Identifier("truth"),
                        new Ast.Identifier("truth2")
                )), true, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("truth", true);
                    put("truth2", true);
                }})),
                Arguments.of("First Arg False", new Ast.Term("or", Arrays.asList(
                        new Ast.Identifier("falsey"),
                        new Ast.Identifier("truth")
                )), true, Collections.unmodifiableMap(new HashMap<String, Object>() {{
                    put("falsey", false);
                    put("truth", true);
                }}))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGreater(String test, Ast ast, Boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }
    //TODO: compare bools, identifiers, terms, etc.
    private static Stream<Arguments> testGreater() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("<", Arrays.asList()), true, Collections.emptyMap()),
                Arguments.of("Single Argument", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), null, Collections.emptyMap()),
                Arguments.of("Two Nums Equal", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), false, Collections.emptyMap()),
                Arguments.of("Two Nums Greater", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), true, Collections.emptyMap()),
                Arguments.of("Multiple Arguments, false", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(1))
                )), false, Collections.emptyMap())
        );
    }

    @ParameterizedTest
    @MethodSource
    void testGreaterEqual(String test, Ast ast, Boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }
    //TODO: compare bools, identifiers, terms, etc.
    private static Stream<Arguments> testGreaterEqual() {  //NOTE: errors return correctly (2)
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("<=", Arrays.asList()), true, Collections.emptyMap()),
                Arguments.of("Single Argument", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), null, Collections.emptyMap()),
                Arguments.of("Two Nums Equal", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), true, Collections.emptyMap()),
                Arguments.of("Two Nums Greater", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), true, Collections.emptyMap()),
                Arguments.of("Multiple Arguments, false", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), true, Collections.emptyMap())
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLess(String test, Ast ast, Boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }
    //TODO: compare bools, identifiers, terms, etc.
    private static Stream<Arguments> testLess() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term(">", Arrays.asList()), true, Collections.emptyMap()),
                Arguments.of("Single Argument", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), null, Collections.emptyMap()),
                Arguments.of("Two Nums Equal", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), false, Collections.emptyMap()),
                Arguments.of("Two Nums Less", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(1))
                )), true, Collections.emptyMap()),
                Arguments.of("Two Nums Greater", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), false, Collections.emptyMap()),
                Arguments.of("Multiple Arguments, true", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(-1))
                )), true, Collections.emptyMap()),
                Arguments.of("Multiple Arguments, false", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(1))
                )), false, Collections.emptyMap())
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLessEqual(String test, Ast ast, Boolean expected, Map<String, Object> map) throws EvalException {
        test(ast, expected, map);
    }
    //TODO: compare bools, identifiers, terms, etc.
    private static Stream<Arguments> testLessEqual() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term(">=", Arrays.asList()), true, Collections.emptyMap()),
                Arguments.of("Single Argument", new Ast.Term(">=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), null, Collections.emptyMap()),
                Arguments.of("Two Nums Equal", new Ast.Term(">=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), true, Collections.emptyMap()),
                Arguments.of("Two Nums Greater", new Ast.Term(">=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(3)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), true, Collections.emptyMap()),
                Arguments.of("Multiple Arguments, false", new Ast.Term(">=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), false, Collections.emptyMap())
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

    private static Stream<Arguments> testRange() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("range", Arrays.asList()), null),
                Arguments.of("Single Argument", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), null),
                Arguments.of("Multiple Arguments", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(8)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(7))
                )), null),
                Arguments.of("Correct Case", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(8))
                )), new LinkedList<Object>(Arrays.asList(
                        BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4),
                        BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7)))),
                Arguments.of("Non Int", new Ast.Term("range", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(10.5))
                )), null)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSet(String test, Ast ast, Object expected, Map<String, Object> setup) throws EvalException {
        test(ast, expected, setup);     //FIXME: have to return scope.lookup(var_name.getName()) for testing
    }

    private static Stream<Arguments> testSet() {  //NOTE: errors return correctly (1, 2)
        return Stream.of(
            Arguments.of("Zero Arguments", new Ast.Term("set!", Arrays.asList()), null, Collections.emptyMap()),
            Arguments.of("No Identifier", new Ast.Term("set!", Arrays.asList(
                    new Ast.StringLiteral("no"),
                    new Ast.NumberLiteral(BigDecimal.valueOf(2))
            )), null, Collections.emptyMap()),
            Arguments.of("Identifier and AST", new Ast.Term("set!", Arrays.asList(
                    new Ast.Identifier("x"),
                    new Ast.NumberLiteral(BigDecimal.valueOf(10))
            )), BigDecimal.valueOf(10), Collections.singletonMap("x", 4))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testFor(String test, Ast ast, LinkedList<Object> expected, Map<String, Object> map) {
        test(ast, expected, map);
    }

    private static Stream<Arguments> testFor() {
        return Stream.of(
//            Arguments.of("Zero Arguments", new Ast.Term("for", Arrays.asList()), null, Collections.emptyMap()),
//            Arguments.of("Single Argument", new Ast.Term("for", Arrays.asList(
//                    new Ast.NumberLiteral(BigDecimal.valueOf(2))
//            )), null, Collections.emptyMap()),
            Arguments.of("Correct Case", new Ast.Term("for", Arrays.asList(
                    new Ast.Identifier("i"),
                    new Ast.Term("range", Arrays.asList(
                            new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                            new Ast.NumberLiteral(BigDecimal.valueOf(10))
                    )),
                    new Ast.Term("print", Arrays.asList(
                            new Ast.Identifier("i")
            )))), null, Collections.singletonMap("i", -8))
//            Arguments.of("Correct Case", new Ast.Term("for", Arrays.asList(
//                    new Ast.NumberLiteral(BigDecimal.valueOf(2)),
//                    new Ast.NumberLiteral(BigDecimal.valueOf(8))
//            )), new LinkedList<Object>(Arrays.asList(
//                    BigDecimal.valueOf(2), BigDecimal.valueOf(3), BigDecimal.valueOf(4),
//                    BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7))))
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