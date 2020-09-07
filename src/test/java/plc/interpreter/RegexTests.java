package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. Tests declarations for steps 1 & 2
 * are provided, you must add your own for step 3.
 *
 * To run tests, either click the run icon on the left margin or execute the
 * gradle test task, which can be done by clicking the Gradle tab in the right
 * sidebar and navigating to Tasks > verification > test Regex(double click to run).
 */
public class RegexTests {
    //5 success and 5 failure test cases
    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests.
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("Period Before @", "the.legend27@gmail.com", true),
                Arguments.of("Underscore Before @", "_thelegend27@gmail.com", true),
                Arguments.of("Hyphen Before @", "thelegend-@gmail.com", true),
                Arguments.of("All Caps Before @", "THELEGEND@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Missing After @", "otherdomain@.edu", true),
                Arguments.of("Number After @", "thelegend27@gmail27.com", true),
                Arguments.of("Hyphen After @", "thelegend27@gmail-27.com", true),
                Arguments.of("Caps After @", "thelegend27@GMAIL.com", true),
                Arguments.of("Org Domain", "otherdomain@something.org", true),
                Arguments.of("Hyphen After @", "otherdomain@some-thing.org", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Two Domain Dots", "missingdot@gmail..om", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),
                Arguments.of("Space Before @", "the legend@gmail.com", false),
                Arguments.of("Blank Before @", "@gmail.com", false),
                Arguments.of("Symbol After @", "thelegend27@gma*l.com", false),
                Arguments.of("Two @", "thelegend@@gmail.com", false),
                Arguments.of("Extension Too Long", "thelegend27@gmail.comm", false),
                Arguments.of("Extension Too Short", "thelegend27@gmail.c", false),
                Arguments.of("Extension With Number", "thelegend27@gmail.co1", false),
                Arguments.of("Extension With Uppercase", "thelegend27@gmail.Com", false),
                Arguments.of("Extension With Symbol", "thelegend27@gmail.c*m", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testFileNamesRegex(String test, String input, boolean success) {
        //this one is different as we're also testing the file name capture
        Matcher matcher = test(input, Regex.FILE_NAMES, success);
        if (success) {
            Assertions.assertEquals(input.substring(0, input.indexOf(".")), matcher.group("name"));
        }
    }

    //FIXME: What are the allowed characters for file names?
    public static Stream<Arguments> testFileNamesRegex() {
        return Stream.of(
                Arguments.of("Java File", "Regex.tar.java", true),
                Arguments.of("Java Class", "RegexTests.class", true),
                Arguments.of("Java File With Class", "Regex.java.class", true),
                Arguments.of("Java File With Class", "Regex.class.java", true),
                Arguments.of("File With Underscore", "Re_gex.class", true),
                Arguments.of("File With Hyphen", "Re-gex.class", true),
                Arguments.of("File With Caps", "RE-GEX.class", true),
                Arguments.of("Directory", "directory", false),
                Arguments.of("Python File", "scrippy.py", false),
                Arguments.of("Java Ext in Caps", "Regex.tar.JAVA", false),
                Arguments.of("Class Ext in Caps", "Regex.tar.CLASS", false),
                Arguments.of("File Name With Invalid Char", "Regex*Tests.class", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    //Strings between 10 and 20 characters (inclusive) which have even lengths
    //FIXME: Are uppercase letters/other symbols considered a char?  Is 10 < L < 20, inclusive also?
    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                Arguments.of("14 Characters", "thishas14chars", true),
                Arguments.of("10 Characters", "i<3pancakes!", true),    //FIXME: Length is 12, but 10 characters?
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("15 Characters", "i<3pancakes!!", false)   //FIXME: Length is 13, but 15 chars?
//                Arguments.of("14 Characters", "thishas14chars", true),                   //L = 14, C = 14
//                Arguments.of("10 Characters", "i<3pancakes!", true),                     //L = 12, C = 10
//                Arguments.of("10 Characters, 10 Length", "tenchars10", true),            //L = 10, C = 10
//                Arguments.of("16 Characters, 20 Length", "pancakesssssssss!!!!", true),  //L = 20, C = 16
//                Arguments.of("Caps", "THISHAS14CHARS", true),                   //L = 14, C = 14, TODO
//                Arguments.of("15 Characters", "i<3pancakes!!", false),          //TODO
//                Arguments.of("20 Characters, 22 Length", "aaaaaaaaaaaaaaaaaaaa!!", false), //L = 22, C = 20, too long
//                Arguments.of("6 Characters", "6chars", false)                             //L = 6,  C = 6, not enough chars
//                Arguments.of("11 Chars, 15 Length", "6chars6char!!!", false),              //L = 15, C = 11, odd length
//                Arguments.of("9 Characters, 13 Length", "i<3pancake!!!", false)      //L = 13, C = 9, odd length and not enough chars
        );
    }

//
//    @ParameterizedTest
//    @MethodSource
//    public void testIntegerListRegex(String test, String input, boolean success) {
//        test(input, Regex.INTEGER_LIST, success);
//    }
//
//    public static Stream<Arguments> testIntegerListRegex() {
//        return Stream.of(
//                Arguments.of("Empty List", "[]", true),
//                Arguments.of("Single Element", "[1]", true),
//                Arguments.of("Multiple Elements", "[1,2,3]", true),
//                Arguments.of("Missing Brackets", "1,2,3", false),
//                Arguments.of("Missing Commas", "[1 2 3]", false),
//                Arguments.of("Trailing Comma", "[1,2,3,]", false)
//        );
//    }

    //FIXME: add Part 3 test methods here

    /**
     * Asserts that the input matches the given pattern and returns the matcher
     * for additional assertions.
     */
    private static Matcher test(String input, Pattern pattern, boolean success) {
        Matcher matcher = pattern.matcher(input);
        Assertions.assertEquals(success, matcher.matches());
        return matcher;
    }

}
