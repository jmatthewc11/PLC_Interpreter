package plc.compiler;

import java.io.PrintWriter;

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

        // TODO: go to each method as needed, based on what is next in the AST

        writer.write("}");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Expression ast) {
        // TODO:  Generate Java to handle Expression node.

        return null;
    }

    //FIXME: getValue() will change based on the value, see BOOLEAN example
    // will have to unwrap the literal, most likely
    @Override
    public Void visit(Ast.Statement.Declaration ast) {
        writer.write(ast.getType() + " " + ast.getName());
        if (ast.getValue().isPresent()) {
            writer.write(" = ");
            visit(ast.getValue().get());
        }

//        if (ast.getValue().get() instanceof Ast.Expression.Literal) { //FIXME: go to different visit based on type of expression?
//            visit(ast.getValue().get());
//        }
//            visit(ast.getValue()); //FIXME: visit expression statement instead of trying to parse out here?

//            if (ast.getType().equals("BOOLEAN"))  //FIXME: conversion to boolean?
//                writer.write(String.valueOf(Boolean.parseBoolean(ast.getValue().toString().toLowerCase())));
//            else if (ast.getType().equals("STRING"))
//                writer.write("\"" + ast.getValue() + "\"");
//        }
        writer.write(";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.Assignment ast) {
        writer.write(ast.getName() + " = " + ast.getExpression() + ";");
        return null;
    }

    @Override
    public Void visit(Ast.Statement.If ast) {

        // TODO:  Generate Java to handle If node.

        return null;
    }

    @Override
    public Void visit(Ast.Statement.While ast) {

        // TODO:  Generate Java to handle While node.

        return null;
    }

    @Override
    public Void visit(Ast.Expression.Literal ast) {

        // TODO:  Generate Java to handle Literal node.

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

    //FIXME: might have problem with argument type (literal or not) and number of args (comma separated, no space after comma)
    @Override
    public Void visit(Ast.Expression.Function ast) {
        writer.write(ast.getName() + "(" + ast.getArguments() + ");");
        return null;
    }

}
