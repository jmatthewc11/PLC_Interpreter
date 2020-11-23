package plc.compiler;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

//FIXME: will probably have general problems with semicolons/indents
// watch for reducing indent for ending }

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        writer.write("public final class Main {");
        newline(1);
        writer.write("public static void main(String[] args {");

        //get the list of statements in the AST, visit each one
        List<Ast.Statement> statements = ast.getStatements();
        for (Ast.Statement statement : statements)
            visit(statement);

        writer.write("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        return null;
    }

    //TODO: hopefully get() returns an expression and can visit accordingly
    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        writer.write(ast.getType() + " " + ast.getName());
        if (ast.getValue().isPresent()) {
            writer.write(" = ");
            visit(ast.getValue().get());
        }
        writer.write(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        writer.write(ast.getName() + " = ");
        visit(ast.getExpression());
        writer.write(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {

        // TODO:  Generate Java to handle If node.

        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        writer.write("WHILE ( " + ast.getCondition() + ") {");

        List<Ast.Statement> statements = ast.getStatements();
        newline(1);
        for (Ast.Statement statement : statements)
            visit(statement);

        writer.write("}");
        return null;
    }

    //FIXME: have to make sure these conversions are correct
    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if (ast.getValue() instanceof BigInteger)           //unwrap integer types
            writer.write(((BigInteger) ast.getValue()).intValue());
        else if (ast.getValue() instanceof BigDecimal)      //unwrap decimal types
            writer.print(((BigDecimal) ast.getValue()).doubleValue());
        else if (ast.getValue() instanceof Boolean)         //unwrap boolean types
            writer.print(Boolean.parseBoolean(ast.getValue().toString()));
        else                                                //write a string
            writer.write("\"" + ast.getValue() + "\"");

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Group ast) {

        // TODO:  Generate Java to handle Group node.

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {

        // TODO:  Generate Java to handle Binary node.

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Variable ast) {
        writer.write(ast.getName());
        return null;
    }

    //FIXME: may have problems printing a literal vs. a variable...
    @Override
    public Void visit(Ast.Expression.Function ast) {
        writer.write(ast.getName() + "(");
        List<Ast.Expression> args = ast.getArguments();

        for(int i = 0; i < args.size(); i++) {
            visit(args.get(i));
            args.remove(i);
            if (args.size() > 0) {
                writer.write(",");
            }
        }
        writer.write(")");
        return null;
    }

}
