package plc.interpreter;

import com.sun.org.apache.xpath.internal.operations.Operation;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.lang.Comparable;

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
    private Object eval(Ast.Term ast) {
        Object object = scope.lookup(ast.getName());    //should returns the mapped function

        object = requireType(Function.class, object);   //check that returned function is actually a function
        Function<List<Ast>, Object> func = (Function<List<Ast>, Object>) object;
        return func.apply(ast.getArgs());
    }

    /**
     * Evaluates the Identifier ast, which returns the value stored under the
     * identifier's name in the current scope.
     */
    private Object eval(Ast.Identifier ast) {   //evaluate whatever is in the AST
        return scope.lookup(ast.getName());     //separate terms evaluated with scope vs without
    }                                           //TODO: watch lecture on scopes

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
    private void init(Scope scope) {
        scope.define("print", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            evaluated.forEach(out::print);
            out.println();
            return VOID;
        });
        scope.define("+", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());
            BigDecimal result = BigDecimal.ZERO;            //return 0 if no args
            for (int i = 0; i < evaluated.size(); i++) {
                result = result.add(evaluated.get(1));
            }
            return result;
        });
        scope.define("-", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());
            if (evaluated.isEmpty()) {
                throw new EvalException("Subtraction must have at least one argument");
            }

            BigDecimal result = evaluated.get(0);
            if (evaluated.size() == 1) {    //add result to zero and negate it
                return result.negate();
            }

            for (int i = 1; i < evaluated.size(); i++) {
                result = result.subtract(evaluated.get(i));
            }
            return result;
        });
        scope.define("*", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());
            BigDecimal result = BigDecimal.ONE;         //returns 1 if no args
            for (int i = 0; i < evaluated.size(); i++) {
                result = result.multiply(evaluated.get(i));
            }
            return result;
        });
        scope.define("/", (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());
            if (evaluated.isEmpty()) {
                throw new EvalException("Division must have at least one argument");
            }

            BigDecimal result = evaluated.get(0);
            if (evaluated.size() == 1) {    //raise to power of -1 to get inverse
                result = BigDecimal.ONE.divide(result, RoundingMode.HALF_EVEN);
            }

            for (int i = 1; i < evaluated.size(); i++) {
                result = result.divide(evaluated.get(i), RoundingMode.HALF_EVEN);
            }

            return result;
        });
        scope.define("true", true);
        scope.define("false", false);
        scope.define("equals?", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() != 2)
                throw new EvalException("equals? requires two arguments for comparison");

            return Objects.deepEquals(evaluated.get(0), evaluated.get(1));
        });
        scope.define("not", (Function<List<Ast>, Object>) args -> {
            List<Boolean> evaluated = args.stream().map(a -> requireType(Boolean.class, eval(a))).collect(Collectors.toList());
            if (evaluated.size() != 1)
                throw new EvalException(("not only takes a single argument"));

            return !evaluated.get(0);
        });
        scope.define("and", (Function<List<Ast>, Object>) args -> {
            for (Ast arg : args) {
                if (!requireType(Boolean.class, eval(arg)))
                    return false;
            }
            return true;
        });
        scope.define("or", (Function<List<Ast>, Object>) args -> {
            for (Ast arg : args) {
                if (requireType(Boolean.class, eval(arg)))
                    return true;
            }
            return false;
        });
        scope.define("<", (Function<List<Ast>, Object>) args -> {
            List<Comparable> evaluated = args.stream().map(a -> requireType(Comparable.class, eval(a))).collect(Collectors.toList());
            if (evaluated.isEmpty()) return true;
            if (evaluated.size() == 1) throw new EvalException("Needs two arguments to compare greater than");

            Comparable compare_1 = evaluated.get(0);
            for(int i = 1; i < evaluated.size(); i++) {
                Comparable compare_2 = evaluated.get(i);

                if (compare_1.compareTo(compare_2) != -1)
                    return false;

                compare_1 = compare_2;
            }

            return true;
        });
        scope.define("<=", (Function<List<Ast>, Object>) args -> {
            List<Comparable> evaluated = args.stream().map(a -> requireType(Comparable.class, eval(a))).collect(Collectors.toList());
            if (evaluated.isEmpty()) return true;
            if (evaluated.size() == 1) throw new EvalException("Needs two arguments to compare greater than or equal to");

            Comparable compare_1 = evaluated.get(0);
            for(int i = 1; i < evaluated.size(); i++) {
                Comparable compare_2 = evaluated.get(i);

                if (compare_1.compareTo(compare_2) == 1)
                    return false;

                compare_1 = compare_2;
            }

            return true;
        });
        scope.define(">", (Function<List<Ast>, Object>) args -> {
            List<Comparable> evaluated = args.stream().map(a -> requireType(Comparable.class, eval(a))).collect(Collectors.toList());
            if (evaluated.isEmpty()) return true;
            if (evaluated.size() == 1) throw new EvalException("Needs two arguments to compare less than");

            Comparable compare_1 = evaluated.get(0);
            for(int i = 1; i < evaluated.size(); i++) {
                Comparable compare_2 = evaluated.get(i);

                if (compare_1.compareTo(compare_2) != 1)
                    return false;

                compare_1 = compare_2;
            }

            return true;
        });
        scope.define(">=", (Function<List<Ast>, Object>) args -> {
            List<Comparable> evaluated = args.stream().map(a -> requireType(Comparable.class, eval(a))).collect(Collectors.toList());
            if (evaluated.isEmpty()) return true;
            if (evaluated.size() == 1) throw new EvalException("Needs two arguments to compare less than or equal to");

            Comparable compare_1 = evaluated.get(0);
            for(int i = 1; i < evaluated.size(); i++) {
                Comparable compare_2 = evaluated.get(i);

                if (compare_1.compareTo(compare_2) == -1)
                    return false;

                compare_1 = compare_2;
            }

            return true;
        });
        scope.define("list", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            LinkedList<Object> list = new LinkedList<Object>();
            if (evaluated.isEmpty()) return list;

            list.addAll(evaluated);
            return list;
        });
        scope.define("range", (Function<List<Ast>, Object>) args -> {   //FIXME: clean this up
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() != 2) throw new EvalException("Range requires 2 arguments");

            BigDecimal first_arg = requireType(BigDecimal.class, evaluated.get(0));
            BigDecimal second_arg = requireType(BigDecimal.class, evaluated.get(1));

            LinkedList<Object> list = new LinkedList<Object>();
            int res = second_arg.compareTo(first_arg);
            if (res < 0)
                throw new EvalException("Range requires second argument to be greater than the first");
            else if (res == 0)
                return list;

            if (Math.round(first_arg.doubleValue()) != first_arg.doubleValue() ||
                Math.round(second_arg.doubleValue()) != second_arg.doubleValue())
                    throw new EvalException("Range requires integers");

            int arg1 = first_arg.intValue();
            int arg2 = second_arg.intValue();
            for (int i = arg1; i < arg2; i++) {
                list.add(BigDecimal.valueOf(i));
            }
            return list;
        });
        scope.define("set!", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2) throw new EvalException("set! requires two arguments");

            Ast.Identifier var_name = requireType(Ast.Identifier.class, args.get(0));
            Ast var_value = requireType(Ast.class, args.get(1));

            scope.set(var_name.getName(), eval(var_value));

            return VOID;
        });
        scope.define("do", (Function<List<Ast>, Object>) args -> {     //FIXME: evaluate inside a new scope?
            if (args.isEmpty()) return VOID;
            Scope scope2 = new Scope(scope);

            for(int i = 0; i < args.size(); i++) {

            }


            //FIXME: Store scopes in the sequence they've been created in within a data structure
            return scope2.lookup(args.get(args.size() - 1).toString());
        });
        scope.define("while", (Function<List<Ast>, Object>) args -> {
            //start a do then define a variable, make changes to that variable using a loop, and finally access that variable.
            // Then in JUnit you could compare the output value to what you would expect.
            if (args.size() != 2) throw new EvalException("while requires two arguments");

            while (requireType(Boolean.class, eval(args.get(0))))
                eval(args.get(1));

            return VOID;
        });
        scope.define("for", (Function<List<Ast>, Object>) args -> {
            if (args.size() != 3) throw new EvalException("for requires three arguments");

            List list = requireType(List.class, eval(args.get(1)));     //FIXME: try passing in literal list too, not just range/LL functions
            Ast ast = requireType(Ast.class, args.get(2));

            Ast.Identifier identifier = requireType(Ast.Identifier.class, args.get(0));
            Scope scope2 = new Scope(scope);
            scope2.define(identifier.getName(), list.get(0));           //FIXME: identifier = value in list, value needs to increment

//            for (int i = 0; i < list.size(); i++) {
//                eval(ast);
//                scope2.set(identifier.getName(), list.get(i));
//            }

//            for (scope2.lookup(identifier.getName()) : list) {      //FIXME: print things in list, how to make this work?
//                eval(ast);
//                scope2.set(identifier.getName(), list.get());       //how to set this without iterator?
//            }

            return VOID;
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
