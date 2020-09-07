package plc.interpreter;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regex's as needed.
 */
public class Regex {

    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            //Must be (1+ number of letters/numbers/./_/-) (@) (0+ of letters/numbers/- but only that can be repeated), then below
            //First [] CANNOT be blank
            //Second [] CANNOT have symbols other than -, CAN be ommitted
            // After . CANNOT be uppercase/numbers/symbols, CANNOT be less than 2 or more than 3 chars
            FILE_NAMES = Pattern.compile(""),
            EVEN_STRINGS = Pattern.compile(""),
            INTEGER_LIST = Pattern.compile(""),
            IDENTIFIER = Pattern.compile(""),
            NUMBER = Pattern.compile(""),
            STRING = Pattern.compile("");

}
