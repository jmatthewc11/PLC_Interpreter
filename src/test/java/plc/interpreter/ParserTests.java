package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * You know the drill...
 */
final class ParserTests {

    @Test
    void testExample1() {
        String input = "(+ 1 -2.0)";
        Ast expected = new Ast.Term("+", Arrays.asList(
                new Ast.NumberLiteral(BigDecimal.ONE),
                new Ast.NumberLiteral(BigDecimal.valueOf(-2.0))
        ));
        test(input, expected);
    }

    @Test
    void testExample2() {
        String input = "(print \"Hello, World!\")";
        Ast expected = new Ast.Term("print", Arrays.asList(
                new Ast.StringLiteral("Hello, World!")
        ));
        test(input, expected);
    }

    @Test
    void testExample3() {
        String input = "(let [x 10] (assert-equals? x 10))";
        Ast expected = new Ast.Term("let",
                          Arrays.asList(
                              new Ast.Term("x",
                                  Arrays.asList(new Ast.NumberLiteral(BigDecimal.TEN)
                )),
                new Ast.Term("assert-equals?",
                      Arrays.asList(
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                ))
        ));
        test(input, expected);
    }

    @Test
    void testExample4() {
        String input = "(<=> [yuv 10] (print \"pls give A\"))";
        Ast expected = new Ast.Term("<=>",
                Arrays.asList(
                        new Ast.Term("yuv",
                                Arrays.asList(new Ast.NumberLiteral(BigDecimal.TEN)
                                )),
                        new Ast.Term("print",
                                Arrays.asList(
                                        new Ast.StringLiteral("pls give A")
                                ))
                ));
        test(input, expected);
    }

    void test(String input, Ast expected) {
        Ast ast = new Ast.Term("source", Arrays.asList(expected));
        Assertions.assertEquals(ast, Parser.parse(input));
    }

}
