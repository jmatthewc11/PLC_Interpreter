package plc.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/*
* Generator tests should be pretty simple if you have any questions message me on teams :> -Gus
* */

public class GeneratorTests {

    @Test
    void testEmptySource(){
        Ast input = Parser.parse(Lexer.lex(
                ""));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                "" ,
                "    public static void main(String[] args) {}" ,
                "" ,
                "}"
        ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testSimplePrint(){
        Ast input = new Ast.Expression.Function("print", Arrays.asList(
                new Ast.Expression.Literal("Hello, World!")
        ));
        String expected = "print(\"Hello, World!\")";

        test(input, expected);
    }

    @Test
    void testBiggerPrint(){
        Ast input = Parser.parse(Lexer.lex(
                "cry(\"Hello, World!\");"));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                        "" ,
                        "    public static void main(String[] args) {" ,
                        "        cry(\"Hello, World!\");" ,
                        "    }" ,
                        "" ,
                        "}"
        ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testExpressionAST() {
        Ast.Expression ast = new Ast.Expression.Binary("-",
                new Ast.Expression.Literal(new BigDecimal("3.14")),
                new Ast.Expression.Group(new Ast.Expression.Binary("*",
                        new Ast.Expression.Variable("rr"),
                        new Ast.Expression.Variable("r")
                ))
        );
        String expected = "3.14 - (rr * r)";
        test(ast, expected);
    }

    //TODO: Issue in parser, AST version passes
    @Test
    void testExpression(){
        Ast input = Parser.parse(Lexer.lex(
                  "LET pi : DECIMAL = 3.14;\n" +
                        "LET r : INTEGER = 10;\n" +
                        "area = pi * (r * r);\n" +
                        "diameter = r * r / r;"));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "" ,
                "    public static void main(String[] args) {" ,
                "        DECIMAL pi = 3.14;" ,
                "        INTEGER r = 10;" ,
                "        area = pi * (r * r);" ,
                "        diameter = r * r / r;" ,
                "    }" ,
                "" ,
                "}"
                ) + System. lineSeparator();
        test(input, expected);
    }

    @Test
    void testDeclarationSmallAST() {
        Ast.Statement ast = new Ast.Statement.Declaration("str", "STRING",
                Optional.of(new Ast.Expression.Literal("HELL_O")));
        String expected = "STRING str = \"HELL_O\";";
        test(ast, expected);
    }

    @Test
    void testDeclarationsmall(){
        Ast input = Parser.parse(Lexer.lex(
                  "LET str : STRING = \"hello\";\n" +
                        "PRINT(str, str, str, str);"));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                        "",
                        "    public static void main(String[] args) {" ,
                        "        STRING str = \"hello\";" ,
                        "        PRINT(str, str, str, str);" ,
                        "    }",
                        "",
                        "}"
        ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testDeclarationBigAST() {
        Ast.Source ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("str", "STRING", Optional.of(new Ast.Expression.Literal("hello"))),
                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                        new Ast.Expression.Variable("str")
                ))),
                new Ast.Statement.Declaration("i", "INTEGER", Optional.empty()),
                new Ast.Statement.Assignment("i", new Ast.Expression.Literal(BigInteger.valueOf(5))),
                new Ast.Statement.Assignment("x", new Ast.Expression.Literal(BigInteger.TEN)),
                new Ast.Statement.Assignment("y", new Ast.Expression.Literal("why?")),
                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                        new Ast.Expression.Variable("x"),
                        new Ast.Expression.Variable("y")
                ))),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Variable("i"),
                                new Ast.Expression.Variable("x")
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Variable("i")
                                )))
                        ),
                        Arrays.asList()
                )
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        STRING str = \"hello\";",
                "        PRINT(str);",
                "        INTEGER i;",
                "        i = 5;",
                "        x = 10;",
                "        y = \"why?\";",
                "        PRINT(x, y);",
                "        if (i != x) {",
                "            PRINT(i);",
                "        }",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testDeclarationBig(){
        Ast input = Parser.parse(Lexer.lex(
                "LET str : STRING = \"hello\";\n" +
                        "PRINT(str);\n" +
                        "LET i : INTEGER;\n" +
                        "i = 5;\n" +
                        "x = 10;\n" +
                        "y= \"why?\";\n" +
                        "PRINT(x,y);\n" +
                        "IF i==x THEN\n" +
                        "END\n"));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                        "" ,
                        "    public static void main(String[] args) {" ,
                        "        STRING str = \"hello\";" ,
                        "        PRINT(str);" ,
                        "        INTEGER i;" ,
                        "        i = 5;" ,
                        "        x = 10;" ,
                        "        y = \"why?\";" ,
                        "        PRINT(x, y);" ,
                        "        if (i == x) {}" ,
                        "    }" ,
                        "" ,
                        "}"
                ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testIfSmallAST() {
        Ast.Statement ast = new Ast.Statement.If(
                new Ast.Expression.Binary("==",
                        new Ast.Expression.Variable("score"),
                        new Ast.Expression.Literal(BigInteger.valueOf(5))
                ),
                Arrays.asList(
                        new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                new Ast.Expression.Literal("f\"ive")
                        )))
                ),
                Arrays.asList()
        );
        String expected = String.join(System.lineSeparator(),
                "if (score == 5) {",
                "    PRINT(\"f\"ive\");",
                "}"
        );
        test(ast, expected);

    }

    @Test
    void testIfsmall(){

        Ast input = Parser.parse(Lexer.lex(
                    "LET score : DECIMAL;\n" +
                          "score = score / 10;\n" +
                          "IF score == 5 THEN\n    " +
                            "PRINT(\"Gus\");\n" +
                          "END"));

        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                        "" ,
                        "    public static void main(String[] args) {" ,
                        "        DECIMAL score;" ,
                        "        score = score / 10;" ,
                        "        if (score == 5) {" ,
                        "            PRINT(\"Gus\");" ,
                        "        }" ,
                        "    }" ,
                        "" ,
                        "}"
                ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testIfBigAST() {
        /*Ast input = Parser.parse(Lexer.lex("LET score : INTEGER;\nscore = score / 10;\nIF score == 10 THEN\n    " +
                "PRINT(\"A\");\nEND\nIF score == 9 THEN\n    " +
                "PRINT(\"A\");\nEND\nIF score == 8 THEN\n    " +
                "PRINT(\"B\");\nEND\nIF score == 7 THEN\n    " +
                "PRINT(\"C\");\nEND\nIF SCORE == 6 THEN\n    " +
                "PRINT(\"D\");END\nIF score != 10 THEN\n    " +
                "IF score != 9 THEN\n        " +
                "IF score != 8 THEN\n            " +
                "IF score != 7 THEN\n                " +
                "IF score != 6 THEN\n    " +
                "PRINT(\"E\");\n                " +
                "END\n            " +
                "END\n        " +
                "END\n    " +
                "END\nEND"));*/
        Ast.Source ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("score", "INTEGER", Optional.empty()),
                new Ast.Statement.Assignment("score", new Ast.Expression.Binary("/",
                        new Ast.Expression.Variable("score"),
                        new Ast.Expression.Literal(BigInteger.TEN)
                )),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("A")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.valueOf(9))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("A")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.valueOf(8))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("B")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.valueOf(7))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("C")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("==",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.valueOf(6))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("D")
                                )))
                        ),
                        Arrays.asList()
                ),
                new Ast.Statement.If(
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Variable("score"),
                                new Ast.Expression.Literal(BigInteger.TEN)
                        ),
                        Arrays.asList(
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("score"),
                                                new Ast.Expression.Literal(BigInteger.valueOf(9))
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.If(
                                                        new Ast.Expression.Binary("!=",
                                                                new Ast.Expression.Variable("score"),
                                                                new Ast.Expression.Literal(BigInteger.valueOf(8))
                                                        ),
                                                        Arrays.asList(
                                                                new Ast.Statement.If(
                                                                        new Ast.Expression.Binary("!=",
                                                                                new Ast.Expression.Variable("score"),
                                                                                new Ast.Expression.Literal(BigInteger.valueOf(7))
                                                                        ),
                                                                        Arrays.asList(
                                                                                new Ast.Statement.If(
                                                                                        new Ast.Expression.Binary("!=",
                                                                                                new Ast.Expression.Variable("score"),
                                                                                                new Ast.Expression.Literal(BigInteger.valueOf(6))
                                                                                        ),
                                                                                        Arrays.asList(
                                                                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                                                                        new Ast.Expression.Literal("E")
                                                                                                )))
                                                                                        ),
                                                                                        Arrays.asList()
                                                                                )
                                                                        ),
                                                                        Arrays.asList()
                                                                )
                                                        ),
                                                        Arrays.asList()
                                                )
                                        ),
                                        Arrays.asList()
                                )
                        ),
                        Arrays.asList()
                )
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        INTEGER score;",
                "        score = score / 10;",
                "        if (score == 10) {",
                "            PRINT(\"A\");",
                "        }",
                "        if (score == 9) {",
                "            PRINT(\"A\");",
                "        }",
                "        if (score == 8) {",
                "            PRINT(\"B\");",
                "        }",
                "        if (score == 7) {",
                "            PRINT(\"C\");",
                "        }",
                "        if (score == 6) {",
                "            PRINT(\"D\");",
                "        }",
                "        if (score != 10) {",
                "            if (score != 9) {",
                "                if (score != 8) {",
                "                    if (score != 7) {",
                "                        if (score != 6) {",
                "                            PRINT(\"E\");",
                "                        }",
                "                    }",
                "                }",
                "            }",
                "        }",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testIfBig(){
        Ast input = Parser.parse(Lexer.lex(
                    "LET score : INTEGER;\n" +
                        "score = score / 10;\n" +
                            "IF score == 10 THEN\n    " +
                                "PRINT(\"A\");\nEND\n" +
                            "IF score == 9 THEN\n    " +
                                "PRINT(\"A\");\nEND\n" +
                            "IF score == 8 THEN\n    " +
                                "PRINT(\"B\");" +
                            "\nELSE\n" +
                                "PRINT(score);" +
                            "\nEND\n" +
                            "IF score == 7 THEN\n    " +
                                "PRINT(\"C\");\n" +
                            "END\n" +
                            "IF SCORE == 6 THEN\n" +
                            "END\n" +
                            "IF score != 10 THEN\n    " +
                                 "IF score != 9 THEN\n        " +
                                    "IF score != 8 THEN\n            " +
                                        "IF score != 7 THEN\n                " +
                                            "IF score != 6 THEN\n    " +
                                                "PRINT(\"E\");\n                " +
                                            "END\n            " +
                                        "END\n        " +
                                    "END\n    " +
                                "END\n" +
                            "END"));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                        "" ,
                        "    public static void main(String[] args) {" ,
                        "        INTEGER score;" ,
                        "        score = score / 10;" ,
                        "        if (score == 10) {" ,
                        "            PRINT(\"A\");" ,
                        "        }" ,
                        "        if (score == 9) {" ,
                        "            PRINT(\"A\");" ,
                        "        }" ,
                        "        if (score == 8) {" ,
                        "            PRINT(\"B\");" ,
                        "        } else {" ,
                        "            PRINT(score);" ,
                        "        }" ,
                        "        if (score == 7) {" ,
                        "            PRINT(\"C\");" ,
                        "        }" ,
                        "        if (SCORE == 6) {}" ,
                        "        if (score != 10) {" ,
                        "            if (score != 9) {" ,
                        "                if (score != 8) {" ,
                        "                    if (score != 7) {" ,
                        "                        if (score != 6) {" ,
                        "                            PRINT(\"E\");" ,
                        "                        }" ,
                        "                    }" ,
                        "                }" ,
                        "            }" ,
                        "        }" ,
                        "    }" ,
                        "" ,
                        "}"
        ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testWhileSmallAST() {
        Ast ast = new Ast.Statement.While(
                new Ast.Expression.Binary("!=",
                        new Ast.Expression.Variable("i"),
                        new Ast.Expression.Literal(BigInteger.ZERO)
                ),
                Arrays.asList(
                        new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                new Ast.Expression.Literal("bean")
                        ))),
                        new Ast.Statement.Assignment("i", new Ast.Expression.Binary("-",
                                new Ast.Expression.Variable("i"),
                                new Ast.Expression.Literal(BigInteger.ONE)
                        ))
                )
        );
        String expected = String.join(System.lineSeparator(),
                "while (i != 0) {",
                "    PRINT(\"bean\");",
                "    i = i - 1;",
                "}"
        );
        test(ast, expected);
    }

    @Test
    void testWhileSmall(){
        Ast input = Parser.parse(Lexer.lex(
                    "LET i : INTEGER = 2;\n" +
                          "WHILE PRINT(i) DO\n    " +
                          "END\n" +
                          "IF i != 7.251 THEN\n    " +
                          "PRINT(\"B\");" +
                          "\nELSE\n" +
                          "PRINT(i);" +
                          "\nEND"
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                        "" ,
                        "    public static void main(String[] args) {" ,
                        "        INTEGER i = 2;" ,
                        "        while (PRINT(i)) {}" ,
                        "        if (i != 7.251) {" ,
                        "            PRINT(\"B\");" ,
                        "        } else {" ,
                        "            PRINT(i);" ,
                        "        }" ,
                        "    }" ,
                        "" ,
                        "}"
                ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testWhileBigAST() {
        Ast ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("i", "INTEGER", Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(2)))),
                new Ast.Statement.Declaration("n", "INTEGER", Optional.of(new Ast.Expression.Literal(BigInteger.valueOf(5)))),
                new Ast.Statement.Declaration("zero", "INTEGER", Optional.empty()),
                new Ast.Statement.While(
                        new Ast.Expression.Binary("!=",
                                new Ast.Expression.Variable("n"),
                                new Ast.Expression.Literal(BigInteger.ZERO)
                        ),
                        Arrays.asList(
                                new Ast.Statement.Assignment("zero", new Ast.Expression.Binary("*",
                                        new Ast.Expression.Binary("-",
                                                new Ast.Expression.Variable("n"),
                                                new Ast.Expression.Variable("i")
                                        ),
                                        new Ast.Expression.Group(new Ast.Expression.Binary("/",
                                                new Ast.Expression.Variable("n"),
                                                new Ast.Expression.Variable("i")
                                        ))
                                )),
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("zero"),
                                                new Ast.Expression.Literal(BigInteger.ONE)
                                        ),
                                        Arrays.asList(),
                                        Arrays.asList()
                                ),
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("zero"),
                                                new Ast.Expression.Literal(BigInteger.ONE)
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                        new Ast.Expression.Literal("even")
                                                )))
                                        ),
                                        Arrays.asList()
                                ),
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("zero"),
                                                new Ast.Expression.Literal(BigInteger.ONE)
                                        ),
                                        Arrays.asList(),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                        new Ast.Expression.Literal("odd")
                                                )))
                                        )
                                ),
                                new Ast.Statement.If(
                                        new Ast.Expression.Binary("!=",
                                                new Ast.Expression.Variable("zero"),
                                                new Ast.Expression.Literal(BigInteger.ONE)
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                        new Ast.Expression.Literal("even")
                                                )))
                                        ),
                                        Arrays.asList(
                                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                                        new Ast.Expression.Literal("odd")
                                                )))
                                        )
                                ),
                                new Ast.Statement.Assignment("n", new Ast.Expression.Binary("-",
                                        new Ast.Expression.Variable("n"),
                                        new Ast.Expression.Literal(BigInteger.ONE)
                                ))
                        )
                )
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        INTEGER i = 2;",
                "        INTEGER n = 5;",
                "        INTEGER zero;",
                "        while (n != 0) {",
                "            zero = n - i * (n / i);",
                "            if (zero != 1) {}",
                "            if (zero != 1) {",
                "                PRINT(\"even\");",
                "            }",
                "            if (zero != 1) {} else {",
                "                PRINT(\"odd\");",
                "            }",
                "            if (zero != 1) {",
                "                PRINT(\"even\");",
                "            } else {",
                "                PRINT(\"odd\");",
                "            }",
                "            n = n - 1;",
                "        }",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testWhileBig(){
        Ast input = Parser.parse(Lexer.lex("LET i : INTEGER = 2;" +
                "\nLET n : INTEGER = 5;" +
                "\n LET zero: STRING;" +
                "\nWHILE n != 0 DO\n    " +
                    "zero = n - i * ( n / i );" +
                "\n    IF zero != 1 THEN\n    ELSE\n    END\n    IF zero != 1 THEN\n        PRINT(\"even\");" +
                "\n    ELSE\n    END\n    IF zero != 1 THEN\n    ELSE\n        PRINT(\"odd\");" +
                "\n    END\n    " +
                "IF zero != 1 THEN\n        " +
                    "PRINT(\"even\");" +
                "\n    " +
                "ELSE\n        " +
                    "PRINT(\"odd\");" +
                "\n    END\n    n = n - 1;\n" +
                "PRINT(n);\nEND"));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                        "" ,
                        "    public static void main(String[] args) {" ,
                        "        INTEGER i = 2;" ,
                        "        INTEGER n = 5;" ,
                        "        STRING zero;" ,
                        "        while (n != 0) {" ,
                        "            zero = n - i * (n / i);" ,
                        "            if (zero != 1) {}" ,
                        "            if (zero != 1) {" ,
                        "                PRINT(\"even\");" ,
                        "            }" ,
                        "            if (zero != 1) {} else {" ,
                        "                PRINT(\"odd\");" ,
                        "            }" ,
                        "            if (zero != 1) {" ,
                        "                PRINT(\"even\");" ,
                        "            } else {" ,
                        "                PRINT(\"odd\");" ,
                        "            }" ,
                        "            n = n - 1;" ,
                        "            PRINT(n);" ,
                        "        }" ,
                        "    }" ,
                        "" ,
                        "}"
                ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testLiteral(){
        Ast input = Parser.parse(Lexer.lex(
                  "LET test : BOOLEAN = FALSE;\n" +
                        "IF test THEN\n    " +
                            "PRINT(\"success!\");\n" +
                        "ELSE\n    " +
                            "PRINT(\"failure?\");\n" +
                        "END"));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                        "" ,
                        "    public static void main(String[] args) {" ,
                        "        BOOLEAN test = false;" ,
                        "        if (test) {" ,
                        "            PRINT(\"success!\");" ,
                        "        } else {" ,
                        "            PRINT(\"failure?\");" ,
                        "        }" ,
                        "    }" ,
                        "" ,
                        "}"
                ) + System.lineSeparator();
        test(input, expected);
    }

    @Test
    void testLiteralSmallAST() {
        Ast.Expression ast = new Ast.Expression.Literal("string");
        String expected = "\"string\"";
        test(ast, expected);
    }

    @Test
    void testLiteralBigAST() {
        Ast.Source ast = new Ast.Source(Arrays.asList(
                new Ast.Statement.Declaration("test", "BOOLEAN", Optional.of(new Ast.Expression.Literal(true))),
                new Ast.Statement.If(
                        new Ast.Expression.Variable("test"),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("success!")
                                )))
                        ),
                        Arrays.asList(
                                new Ast.Statement.Expression(new Ast.Expression.Function("PRINT", Arrays.asList(
                                        new Ast.Expression.Literal("failure?")
                                )))
                        )
                )
        ));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {",
                "",
                "    public static void main(String[] args) {",
                "        BOOLEAN test = true;",
                "        if (test) {",
                "            PRINT(\"success!\");",
                "        } else {",
                "            PRINT(\"failure?\");",
                "        }",
                "    }",
                "",
                "}",
                ""
        );
        test(ast, expected);
    }

    @Test
    void testGroupAST() {
        Ast.Expression ast = new Ast.Expression.Group(new Ast.Expression.Binary("+",
                new Ast.Expression.Binary("*",
                        new Ast.Expression.Literal(BigInteger.valueOf(2)),
                        new Ast.Expression.Group(new Ast.Expression.Binary("-",
                                new Ast.Expression.Literal(BigInteger.valueOf(10)),
                                new Ast.Expression.Literal(BigInteger.valueOf(3))
                        ))),
                new Ast.Expression.Group(new Ast.Expression.Binary("/",
                        new Ast.Expression.Literal(BigInteger.valueOf(5)),
                        new Ast.Expression.Literal(BigInteger.valueOf(2))
                ))
        ));
        String expected = "(2 * (10 - 3) + (5 / 2))";
        test(ast, expected);
    }

    @Test
    void testGroup(){
        Ast input = Parser.parse(Lexer.lex(
                "LET value : INTEGER = (2 * (10 - 3) + (5 / 2)) == 10;"));
        String expected = String.join(System.lineSeparator(),
                "public final class Main {" ,
                        "" ,
                        "    public static void main(String[] args) {" ,
                        "        INTEGER value = (2 * (10 - 3) + (5 / 2)) == 10;" ,
                        "    }" ,
                        "" ,
                        "}"
                ) + System.lineSeparator();
        test(input, expected);
    }

    private static <T extends Ast> void test(Ast input,String expected) {
        StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);

        Generator generator = new Generator(writer);
        if (expected != null) {
            generator.visit(input);
            Assertions.assertEquals(expected, out.toString());
        } else {
            System.out.println("failed");
        }
    }


}
