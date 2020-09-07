package plc.interpreter;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regex's as needed.
 */
public class Regex {

    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            FILE_NAMES = Pattern.compile(""),
            //Must have between 10 and 20 alphanumeric characters, and even overall length
            EVEN_STRINGS = Pattern.compile("[A-Za-z0-9]{10,20}"),
            INTEGER_LIST = Pattern.compile(""),
            IDENTIFIER = Pattern.compile(""),
            NUMBER = Pattern.compile(""),
            STRING = Pattern.compile("");

}
