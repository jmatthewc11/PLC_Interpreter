package plc.compiler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Ast> {

    public Scope scope;

    public Analyzer(Scope scope) {
        this.scope = scope;
    }

    @Override
    public Ast visit(Ast.Source ast) throws AnalysisException {
        if (ast == null) {
            throw new AnalysisException("Source doesn't contain any statements");
        }
        return ast;
    }

    /**
     * Statically validates that visiting a statement returns a statement.
     */
    private Ast.Statement visit(Ast.Statement ast) throws AnalysisException {
        return (Ast.Statement) visit((Ast) ast);
    }

    @Override
    public Ast.Statement.Expression visit(Ast.Statement.Expression ast) throws AnalysisException {
        if (!(ast.getExpression() instanceof Ast.Expression.Function)) {
            throw new AnalysisException("Expression statement must be a function");
        }
        Ast.Expression expression = visit(ast.getExpression());
        return new Ast.Statement.Expression(expression);
    }

    @Override
    public Ast.Statement.Declaration visit(Ast.Statement.Declaration ast) throws AnalysisException {
        if (ast.getType().equals("VOID")) {
            throw new AnalysisException("Cannot declare variable of type VOID");
        }
        String name = ast.getName();
        Stdlib.Type type = Stdlib.getType(ast.getType());
        boolean isLiteral = false;
        Ast.Expression.Literal literal = null;

        if (ast.getValue().isPresent()) {
            if (ast.getValue().get() instanceof Ast.Expression.Literal) {
                isLiteral = true;
                literal = (Ast.Expression.Literal) visit(ast.getValue().get());
                checkAssignable(literal.getType(), type);
                if (literal.getType() == Stdlib.Type.INTEGER && type == Stdlib.Type.DECIMAL) {
                    literal = new Ast.Expression.Literal(type, new Double((int)literal.getValue()));
                }
            }
            else {
                checkAssignable(ast.getValue().get().getType(), type);
            }
        }

        try {
            scope.define(name, type);
        }
        catch (AnalysisException e) {
            throw new AnalysisException("Variable is already defined with the given name");
        }

        if (isLiteral) {
            return new Ast.Statement.Declaration(name, type.getJvmName(), Optional.of(literal));
        }
        return new Ast.Statement.Declaration(name, type.getJvmName(), ast.getValue());
    }

    @Override
    public Ast.Statement.Assignment visit(Ast.Statement.Assignment ast) throws AnalysisException {
        Stdlib.Type var_type;
        try {
            var_type = scope.lookup(ast.getName());
        }
        catch (AnalysisException e) {
            throw new AnalysisException("Variable has not been defined yet, so it cannot be assigned");
        }

        Ast.Expression eval_expression = visit(ast.getExpression());
        checkAssignable(eval_expression.getType(), var_type);

        return new Ast.Statement.Assignment(ast.getName(), eval_expression);
    }

    @Override
    public Ast.Statement.If visit(Ast.Statement.If ast) throws AnalysisException {
        Ast.Expression condition = ast.getCondition();
        List<Ast.Statement> thenStatements = new ArrayList<>();
        thenStatements = ast.getThenStatements();
        List<Ast.Statement> elseStatements = new ArrayList<>();
        elseStatements = ast.getElseStatements();

        if (thenStatements.isEmpty()) {
            throw new AnalysisException("THEN statements list is empty");
        }

        Ast.Expression.Literal eval_condition = (Ast.Expression.Literal) visit(condition);
        if (!eval_condition.getType().getName().equals("BOOLEAN")) {
            throw new AnalysisException("If condition must evaluate to boolean");
        }

        scope = new Scope(scope);
        List<Ast.Statement> new_thenStatements = new ArrayList<>();
        for (int i = 0; i < thenStatements.size(); i++) {
            new_thenStatements.add(visit(thenStatements.get(i)));
        }
        scope = scope.getParent();

        if (!elseStatements.isEmpty()) {
            scope = new Scope(scope);
            List<Ast.Statement> new_elseStatements = new ArrayList<>();
            for (int i = 0; i < elseStatements.size(); i++) {
                new_elseStatements.add(visit(elseStatements.get(i)));
            }
            scope = scope.getParent();
            return new Ast.Statement.If(eval_condition, new_thenStatements, new_elseStatements);
        }
        else {
            return new Ast.Statement.If(eval_condition, new_thenStatements, elseStatements);
        }
    }

    @Override
    public Ast.Statement.While visit(Ast.Statement.While ast) throws AnalysisException {
        Ast.Expression condition = ast.getCondition();
        Ast.Expression.Literal eval_condition = (Ast.Expression.Literal) visit(condition);
        if (!eval_condition.getType().getName().equals("BOOLEAN")) {
            throw new AnalysisException("While condition must evaluate to boolean");
        }

        scope = new Scope(scope);
        List<Ast.Statement> statements = new ArrayList<>();
        statements = ast.getStatements();
        List<Ast.Statement> new_statements = new ArrayList<>();

        for (int i = 0; i < statements.size(); i++) {
            new_statements.add(visit(statements.get(i)));
        }
        scope = scope.getParent();

        return new Ast.Statement.While(eval_condition, new_statements);
    }

    /**
     * Statically validates that visiting an expression returns an expression.
     */
    private Ast.Expression visit(Ast.Expression ast) throws AnalysisException {
        return (Ast.Expression) visit((Ast) ast);
    }

    @Override
    public Ast.Expression.Literal visit(Ast.Expression.Literal ast) throws AnalysisException {
        if (ast.getValue() instanceof Boolean) {
            return new Ast.Expression.Literal(Stdlib.Type.BOOLEAN, ast.getValue());
        }
        else if (ast.getValue() instanceof BigInteger) {
            String max_val = String.valueOf(Integer.MAX_VALUE);
            BigInteger max = new BigInteger(max_val);

            String min_val = String.valueOf(Integer.MIN_VALUE);
            BigInteger min = new BigInteger(min_val);

            if (((BigInteger) ast.getValue()).compareTo(max) > 0 || ((BigInteger) ast.getValue()).compareTo(min) < 0)
                throw new AnalysisException("Integer value is out of range");

            return new Ast.Expression.Literal(Stdlib.Type.INTEGER, ((BigInteger) ast.getValue()).intValue());
        }
        else if (ast.getValue() instanceof BigDecimal) {
            double double_val = ((BigDecimal) ast.getValue()).doubleValue();

            if (double_val == Double.POSITIVE_INFINITY || double_val == Double.NEGATIVE_INFINITY)
                throw new AnalysisException("Double value is out of range, positive or negative infinity");
            if (double_val >= Double.MAX_VALUE || double_val <= Double.MIN_VALUE)
                throw new AnalysisException("Double value is out of range");
            if (Double.compare(-0.0f, double_val) == 0 || Double.compare(+0.0f, double_val) == 0)
                throw new AnalysisException("Double value is out of range, positive or negative zero");

            return new Ast.Expression.Literal(Stdlib.Type.DECIMAL, double_val);
        }
        else if (ast.getValue() instanceof String) {
            if (((String) ast.getValue()).matches("[A-Za-z0-9_!?.+-/* ]*"));
                return new Ast.Expression.Literal(Stdlib.Type.STRING, ast.getValue());
        }
        throw new AnalysisException("Literal expression is incorrect");
    }

    @Override
    public Ast.Expression.Group visit(Ast.Expression.Group ast) throws AnalysisException {
        Ast.Expression expression = visit(ast.getExpression());
        return new Ast.Expression.Group(expression.getType(), expression);
    }

    @Override
    public Ast.Expression.Binary visit(Ast.Expression.Binary ast) throws AnalysisException {
        Ast.Expression.Literal left = (Ast.Expression.Literal) visit(ast.getLeft());
        Ast.Expression.Literal right = (Ast.Expression.Literal) visit(ast.getRight());
        if (left.getType().getName().equals("VOID") || right.getType().getName().equals("VOID"))
            throw new AnalysisException("No void types allowed for equality expressions");

        switch (ast.getOperator()) {
            case "==":
            case "!=":
                return new Ast.Expression.Binary(Stdlib.Type.BOOLEAN, ast.getOperator(), left, right);
            case "+":
                if (left.getType().getName().equals("STRING") || right.getType().getName().equals("STRING"))
                    return new Ast.Expression.Binary(Stdlib.Type.STRING, ast.getOperator(), left, right);
            case "-":
            case "*":
            case "/":
                if (!left.getType().getName().equals("INTEGER") && !left.getType().getName().equals("DECIMAL"))
                    throw new AnalysisException("Need decimal or integer types for arithmetic operations");
                else if (!right.getType().getName().equals("INTEGER") && !right.getType().getName().equals("DECIMAL"))
                    throw new AnalysisException("Need decimal or integer types for arithmetic operations");

                if (left.getType().getName().equals("INTEGER") && right.getType().getName().equals("INTEGER"))
                    return new Ast.Expression.Binary(Stdlib.Type.INTEGER, ast.getOperator(), left, right);
                else
                    return new Ast.Expression.Binary(Stdlib.Type.DECIMAL, ast.getOperator(), left, right);
        }
        throw new AnalysisException("Issue parsing binary expression");
    }

    @Override
    public Ast.Expression.Variable visit(Ast.Expression.Variable ast) throws AnalysisException {
        //FIXME: general structure of what needs to be done, no tests given
        // how to actually test?
        Stdlib.Type var_type;
        try {
            var_type = scope.lookup(ast.getName());
        }
        catch (AnalysisException e) {
            throw new AnalysisException("Variable is not defined");
        }
        return new Ast.Expression.Variable(var_type, ast.getName());
    }

    @Override
    public Ast.Expression.Function visit(Ast.Expression.Function ast) throws AnalysisException {
        //FIXME: need to make my own function to test type checking, use multiple types to test loop
        Stdlib.Function lib_function = Stdlib.getFunction(ast.getName(), ast.getArguments().size());
        List<Stdlib.Type> param_types = lib_function.getParameterTypes();

        int j = 0;
        ArrayList<Ast.Expression> args = new ArrayList<>();
        for (int i = 0; i < ast.getArguments().size(); i++) {
            try {
                Ast.Expression input_arg = visit(ast.getArguments().get(i));
                checkAssignable(input_arg.getType(), param_types.get(j));
                args.add(input_arg);
            }
            catch (AnalysisException e) {
                if (param_types.size() > j + 1) {
                    j++;
                    continue;
                }
                else {
                    throw new AnalysisException("Incorrect parameter types passed in for function");
                }
            }
        }
        return new Ast.Expression.Function(lib_function.getReturnType(), lib_function.getJvmName(), args);
    }

    /**
     * Throws an AnalysisException if the first type is NOT assignable to the target type. * A type is assignable if and only if one of the following is true:
     *  - The types are equal, as according to Object#equals
     *  - The first type is an INTEGER and the target type is DECIMAL
     *  - The first type is not VOID and the target type is ANY
     */
    public static void checkAssignable(Stdlib.Type type, Stdlib.Type target) throws AnalysisException {
        if (type.equals(target)) {
            return;
        }
        if (type.getName().equals("INTEGER") && target.getName().equals("DECIMAL")) {
            return;
        }
        if (!type.getName().equals("VOID") && target.getName().equals("ANY")) {
            return;
        }

        throw new AnalysisException("checkAssignable failed");
    }

}
