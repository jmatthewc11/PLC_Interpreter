package plc.compiler;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

//FIXME: will probably have general problems with semicolons/indents, use indent variable
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
        print("public final class Main {");
        indent++;
        newline(0);
        newline(indent);
        print("public static void main(String[] args) {");
        indent++;
        newline(indent);

        //get the list of statements in the AST, visit each one
        List<Ast.Statement> statements = ast.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            visit(statements.get(i));
            if (i == statements.size() - 1) {
                indent--;
                newline(indent);
                break;
            }
            newline(indent);
        }
        writer.write("}");
        newline(0);
        newline(0);
        writer.write("}");
        newline(0);
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        visit(ast.getExpression());
        print(";");
        return null;
    }

    //TODO: hopefully get() returns an expression and can visit accordingly
    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        print(ast.getType() + " " + ast.getName());
        if (ast.getValue().isPresent()) {
            print(" = ");
            visit(ast.getValue().get());
        }
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        print(ast.getName() + " = ");
        visit(ast.getExpression());
        print(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {
        List<Ast.Statement> then_statements = ast.getThenStatements();
        List<Ast.Statement> else_statements = ast.getElseStatements();
        print("if (");
        visit(ast.getCondition());
        print(") {");
        if (then_statements.isEmpty())
            print("}");
        else {
            indent++;
            newline(indent);
            for (int i = 0; i < then_statements.size(); i++) {
                visit(then_statements.get(i));
                if (i == then_statements.size() - 1) {
                    indent--;
                    newline(indent);
                    break;
                }
                newline(indent);
            }
            print("}");
        }

        if (!else_statements.isEmpty()) {
            print(" else {");
            indent++;
            newline(indent);
            for (int i = 0; i < else_statements.size(); i++) {
                visit(else_statements.get(i));
                if (i == else_statements.size() - 1) {
                    indent--;
                    newline(indent);
                    break;
                }
                newline(indent);
            }
            print("}");
        }

        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {
        print("while (");
        visit(ast.getCondition());
        print(") {");
        indent++;
        newline(indent);

        List<Ast.Statement> statements = ast.getStatements();
        for (int i = 0; i < statements.size(); i++) {
            visit(statements.get(i));
            if (i == statements.size() - 1) {
                indent--;
                newline(indent);
                break;
            }
            newline(indent);
        }

        print("}");
        return null;
    }

    //FIXME: have to make sure these conversions are correct
    @Override
    public Void visit(Ast.Expression.Literal ast) {
        if (ast.getValue() instanceof BigInteger)           //unwrap integer types
            print(((BigInteger) ast.getValue()).intValue());
        else if (ast.getValue() instanceof BigDecimal)      //unwrap decimal types
            print(((BigDecimal) ast.getValue()).doubleValue());
        else if (ast.getValue() instanceof Boolean)         //unwrap boolean types
            print(Boolean.parseBoolean(ast.getValue().toString()));
        else                                                //write a string
            print("\"" + ast.getValue() + "\"");

        return null;
    }

    //FIXME: how to add semicolon at the end?  How to deal with nested expressions?
    @Override
    public Void visit(Ast.Expression.Group ast) {
        print("(");
        visit(ast.getExpression());
        print(")");

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Binary ast) {
        visit(ast.getLeft());
        print(" " + ast.getOperator() + " ");
        visit(ast.getRight());

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Variable ast) {
        print(ast.getName());
        return null;
    }

    //FIXME: may have problems printing a literal vs. a variable...
    @Override
    public Void visit(Ast.Expression.Function ast) {
        print(ast.getName() + "(");
        List<Ast.Expression> args = ast.getArguments();

        for(int i = 0; i < args.size(); i++) {
            visit(args.get(i));
            if (i < args.size() - 1) {
                print(", ");
            }
        }
        print(")");
        return null;
    }

}
