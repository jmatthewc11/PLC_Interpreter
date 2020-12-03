package plc.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Ast.Statement.Declaration visit(Ast.Statement.Declaration ast) throws AnalysisException {
        if (ast.getType().equals("VOID")) {
            throw new AnalysisException("Cannot declare variable of type VOID");
        }
        String name = ast.getName();
        Stdlib.Type type = Stdlib.getType(ast.getType());

        if (ast.getValue().isPresent()) {
            if (ast.getValue().get() instanceof Ast.Expression.Literal) {
                Ast.Expression.Literal literal = (Ast.Expression.Literal) visit(ast.getValue().get());
                checkAssignable(literal.type, type);
            }
            checkAssignable(ast.getValue().get().getType(), type);
        }

        try {
            scope.define(name, type);
        }
        catch (AnalysisException e) {
            throw new AnalysisException("Variable is already defined with the given name");
        }

        return new Ast.Statement.Declaration(name, type.getJvmName(), ast.getValue());
    }

    @Override
    public Ast.Statement.Assignment visit(Ast.Statement.Assignment ast) throws AnalysisException {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Ast.Statement.If visit(Ast.Statement.If ast) throws AnalysisException {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Ast.Statement.While visit(Ast.Statement.While ast) throws AnalysisException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Statically validates that visiting an expression returns an expression.
     */
    private Ast.Expression visit(Ast.Expression ast) throws AnalysisException {
        return (Ast.Expression) visit((Ast) ast);
    }

    @Override
    public Ast.Expression.Literal visit(Ast.Expression.Literal ast) throws AnalysisException {
        throw new UnsupportedOperationException();
        //TODO: validate literals according to specs and return literal with correct value and type
    }

    @Override
    public Ast.Expression.Group visit(Ast.Expression.Group ast) throws AnalysisException {
        return new Ast.Expression.Group(visit(ast.getExpression()));
    }

    @Override
    public Ast.Expression.Binary visit(Ast.Expression.Binary ast) throws AnalysisException {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Ast.Expression.Variable visit(Ast.Expression.Variable ast) throws AnalysisException {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Ast.Expression.Function visit(Ast.Expression.Function ast) throws AnalysisException {
        throw new UnsupportedOperationException(); //TODO
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
