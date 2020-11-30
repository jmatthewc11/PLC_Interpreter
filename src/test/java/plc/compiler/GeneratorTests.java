package plc.compiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

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
    void testWhileSmall(){
        Ast input = Parser.parse(Lexer.lex(
                    "LET i : INTEGER = 2;\n" +
                          "WHILE PRINT(i) DO\n    " +
                              "PRINT(i);\n    " +
                              "i = i + 1;\n" +
                          "END\n" +
                          "IF i != 7.2 THEN\n    " +
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
                        "        while (PRINT(i)) {" ,
                        "            PRINT(i);" ,
                        "            i = i + 1;" ,
                        "        }" ,
                        "        if (i != 7.2) {" ,
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
