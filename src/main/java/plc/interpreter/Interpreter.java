package plc.interpreter;

import com.sun.org.apache.xpath.internal.operations.Operation;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Interpreter {

    /**
     * The VOID constant represents a value that has no useful information. It
     * is used as the return value for functions which only perform side
     * effects, such as print, similar to Java.
     */
    public static final Object VOID = new Function<List<Ast>, Object>() {

        @Override
        public Object apply(List<Ast> args) {
            return VOID;
        }

    };

    public final PrintWriter out;
    public Scope scope;

    public Interpreter(PrintWriter out, Scope scope) {
        this.out = out;
        this.scope = scope;
        init(scope);
    }

    /**
     * Delegates evaluation to the method for the specific instance of AST. This
     * is another approach to implementing the visitor pattern.
     */
    public Object eval(Ast ast) {
        if (ast instanceof Ast.Term) {
            return eval((Ast.Term) ast);
        } else if (ast instanceof Ast.Identifier) {
            return eval((Ast.Identifier) ast);
        } else if (ast instanceof Ast.NumberLiteral) {
            return eval((Ast.NumberLiteral) ast);
        } else if (ast instanceof Ast.StringLiteral) {
            return eval((Ast.StringLiteral) ast);
        } else {
            throw new AssertionError(ast.getClass());
        }
    }

    /**
     * Evaluations the Term ast, which returns the value resulting by calling
     * the function stored under the term's name in the current scope. You will
     * need to check that the type of the value is a {@link Function}, and cast
     * to the type {@code Function<List<Ast>, Object>}.
     */
    private Object eval(Ast.Term ast) {     //FIXME: did I use requireType correctly?
        Object object = scope.lookup(ast.getName());    //should returns the mapped function
        object = requireType(Function.class, object);   //check that returned function is actually a function
        Function<List<Ast>, Object> func = (Function<List<Ast>, Object>) object;

        return func.apply(ast.getArgs());
    }

    /**
     * Evaluates the Identifier ast, which returns the value stored under the
     * identifier's name in the current scope.
     */
    private Object eval(Ast.Identifier ast) {
        return scope.lookup(ast.getName());
    }

    /**
     * Evaluates the NumberLiteral ast, which returns the stored number value.
     */
    private BigDecimal eval(Ast.NumberLiteral ast) {
        return ast.getValue();
    }

    /**
     * Evaluates the StringLiteral ast, which returns the stored string value.
     */
    private String eval(Ast.StringLiteral ast) {
        return ast.getValue();
    }

    /**
     * Initializes the given scope with fields and functions in the standard
     * library.
     */
    private void init(Scope scope) {            //TODO: Add standard library functions from specs
        scope.define("print", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            evaluated.forEach(out::print);
            out.println();
            return VOID;
        });
        scope.define("+", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            BigDecimal result = BigDecimal.ZERO;            //return 0 if no args
            for (int i = 0; i < evaluated.size(); i++) {
                result = result.add((BigDecimal) evaluated.get(i));
            }
            return result;
        });
        scope.define("-", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() == 0) {    //no args, throw error
                throw new EvalException("Subtraction must have at least one argument");
            }

            BigDecimal result = (BigDecimal) evaluated.get(0);
            if (evaluated.size() == 1) {    //add result to zero and negate it
                return result.negate();
            }

            for (int i = 1; i < evaluated.size(); i++) {
                result = result.subtract((BigDecimal) evaluated.get(i));
            }
            return result;
        });
        scope.define("*", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            BigDecimal result = BigDecimal.ONE;         //returns 1 if no args
            for (int i = 0; i < evaluated.size(); i++) {
                result = result.multiply((BigDecimal) evaluated.get(i));
            }
            return result;
        });
        scope.define("/", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() == 0) {    //no args, throw error
                throw new EvalException("Division must have at least one argument");
            }

            BigDecimal result = (BigDecimal) evaluated.get(0);
            if (evaluated.size() == 1) {    //raise to power of -1 to get inverse
                return result.pow(-1, MathContext.DECIMAL128);
            }

            for (int i = 1; i < evaluated.size(); i++) {
                result = result.divide((BigDecimal) evaluated.get(i));
            }
            return result;
        });
        scope.define("true", (Function<List<Ast>, Object>) args -> {        //FIXME: booleans?
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() == 0) return false;
            if (evaluated.get(0) == Boolean.TRUE) {
                return true;
            }
            return false;
        });
        scope.define("false", (Function<List<Ast>, Object>) args -> {       //FIXME: booleans part 2?
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() == 0) return true;
            if (evaluated.get(0) == Boolean.FALSE) {
                return false;
            }
            return true;
        });
        scope.define("equals?", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() != 2)
                throw new EvalException("equals? requires two arguments for comparison");

            if (Objects.deepEquals(evaluated.get(0), evaluated.get(1))) {
                return true;
            }
            return false;
        });
        scope.define("not", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() != 1)
                throw new EvalException(("not only takes a single argument"));
            if (!(evaluated.get(0) instanceof Boolean))
                throw new EvalException(("not requires a boolean argument"));
            if (evaluated.get(0) == Boolean.FALSE) {
                return true;
            }
            return false;
        });
    }

    /**
     * A helper function for type checking, taking in a type and an object and
     * throws an exception if the object does not have the required type.
     *
     * This function does a poor job of actually identifying where the issue
     * occurs - in a real interpreter, we would have a stacktrace to provide
     * that implementation. For now, this is the simple-but-not-ideal solution.
     */
    private static <T> T requireType(Class<T> type, Object value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            throw new EvalException("Expected " + value + " to have type " + type.getSimpleName() + ".");
        }
    }

}
