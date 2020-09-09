package plc.interpreter;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regex's as needed.
 */
public class Regex {
    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            FILE_NAMES = Pattern.compile("(?<name>[\\w-]+)+(\\.java|\\.class)"),
            EVEN_STRINGS = Pattern.compile("(([\\S\\s]){20}|([\\S\\s]){18}|([\\S\\s]){16}|([\\S\\s]){14}|([\\S\\s]){12}|([\\S\\s]){10})"),
            INTEGER_LIST = Pattern.compile("([\\[])(([1-9]+[0-9]*)+(, {0,1}[1-9]+[0-9]*)*)*([\\]])"),
            IDENTIFIER = Pattern.compile("([.][.\\w\\+\\-\\*\\/\\:!\\?<>=]+)|([A-Za-z_\\+\\-\\*/:!\\?<>=]+[\\d.]*)+"),
            NUMBER = Pattern.compile("([\\+]|[\\-]){0,1}([\\d]+)+([.][\\d]+)*"),
            STRING = Pattern.compile("(\")([\\w,!\\-\\.+*/=]*([\\\\][bnrt'\"\\\\])*[\\w,!\\-\\.+*/=]*)*(\")");

}
