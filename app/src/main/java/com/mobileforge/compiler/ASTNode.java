package com.mobileforge.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ASTNode {
    public int line;
    public int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    // Program root
    public static class Program extends ASTNode {
        public List<Statement> statements;

        public Program() {
            super(0, 0);
            this.statements = new ArrayList<>();
        }
    }

    // Base statement
    public static abstract class Statement extends ASTNode {
        public Statement(int line, int column) {
            super(line, column);
        }
    }

    // Base expression
    public static abstract class Expression extends ASTNode {
        public Expression(int line, int column) {
            super(line, column);
        }
    }

    // Build statement: build app "Name"
    public static class BuildStatement extends Statement {
        public String objectType; // "app", "feature", etc.
        public String name;
        public List<Statement> body;

        public BuildStatement(String objectType, String name, int line, int column) {
            super(line, column);
            this.objectType = objectType;
            this.name = name;
            this.body = new ArrayList<>();
        }
    }

    // Create statement: create button "Name"
    public static class CreateStatement extends Statement {
        public String objectType;
        public String name;
        public List<Statement> body;
        public Map<String, Expression> properties;

        public CreateStatement(String objectType, String name, int line, int column) {
            super(line, column);
            this.objectType = objectType;
            this.name = name;
            this.body = new ArrayList<>();
            this.properties = new HashMap<>();
        }
    }

    // Place statement: place button "Name"
    public static class PlaceStatement extends Statement {
        public String objectType;
        public String name;
        public List<Statement> body;
        public Map<String, Expression> properties;
        public String position; // "top", "bottom", etc.

        public PlaceStatement(String objectType, String name, int line, int column) {
            super(line, column);
            this.objectType = objectType;
            this.name = name;
            this.body = new ArrayList<>();
            this.properties = new HashMap<>();
        }
    }

    // When statement: when button clicked
    public static class WhenStatement extends Statement {
        public String eventType; // "clicked", "loaded", etc.
        public String target; // What triggers the event
        public List<Statement> body;

        public WhenStatement(String eventType, int line, int column) {
            super(line, column);
            this.eventType = eventType;
            this.body = new ArrayList<>();
        }
    }

    // If statement
    public static class IfStatement extends Statement {
        public Expression condition;
        public List<Statement> thenBody;
        public List<Statement> elseBody;

        public IfStatement(Expression condition, int line, int column) {
            super(line, column);
            this.condition = condition;
            this.thenBody = new ArrayList<>();
            this.elseBody = new ArrayList<>();
        }
    }

    // For statement: for each item in list
    public static class ForStatement extends Statement {
        public String variable;
        public Expression iterable;
        public List<Statement> body;

        public ForStatement(String variable, Expression iterable, int line, int column) {
            super(line, column);
            this.variable = variable;
            this.iterable = iterable;
            this.body = new ArrayList<>();
        }
    }

    // While statement
    public static class WhileStatement extends Statement {
        public Expression condition;
        public List<Statement> body;

        public WhileStatement(Expression condition, int line, int column) {
            super(line, column);
            this.condition = condition;
            this.body = new ArrayList<>();
        }
    }

    // Repeat statement: repeat 5 times
    public static class RepeatStatement extends Statement {
        public Expression count;
        public List<Statement> body;

        public RepeatStatement(Expression count, int line, int column) {
            super(line, column);
            this.count = count;
            this.body = new ArrayList<>();
        }
    }

    // Function definition
    public static class FunctionStatement extends Statement {
        public String name;
        public List<String> parameters;
        public List<Statement> body;

        public FunctionStatement(String name, int line, int column) {
            super(line, column);
            this.name = name;
            this.parameters = new ArrayList<>();
            this.body = new ArrayList<>();
        }
    }

    // Import statement
    public static class ImportStatement extends Statement {
        public List<String> imports;

        public ImportStatement(int line, int column) {
            super(line, column);
            this.imports = new ArrayList<>();
        }
    }

    // Set/Change statement: set variable to value
    public static class AssignmentStatement extends Statement {
        public String variable;
        public Expression value;
        public String action; // "set", "change", etc.

        public AssignmentStatement(String variable, Expression value, int line, int column) {
            super(line, column);
            this.variable = variable;
            this.value = value;
        }
    }

    // Action statement: show message "Hi"
    public static class ActionStatement extends Statement {
        public String action; // "show", "hide", "print", etc.
        public String target;
        public Expression value;

        public ActionStatement(String action, int line, int column) {
            super(line, column);
            this.action = action;
        }
    }

    // Call statement: call function with args
    public static class CallStatement extends Statement {
        public String functionName;
        public List<Expression> arguments;

        public CallStatement(String functionName, int line, int column) {
            super(line, column);
            this.functionName = functionName;
            this.arguments = new ArrayList<>();
        }
    }

    // Return statement
    public static class ReturnStatement extends Statement {
        public Expression value;

        public ReturnStatement(Expression value, int line, int column) {
            super(line, column);
            this.value = value;
        }
    }

    // Property statement: color red
    public static class PropertyStatement extends Statement {
        public String property;
        public Expression value;

        public PropertyStatement(String property, Expression value, int line, int column) {
            super(line, column);
            this.property = property;
            this.value = value;
        }
    }

    // String literal
    public static class StringLiteral extends Expression {
        public String value;

        public StringLiteral(String value, int line, int column) {
            super(line, column);
            this.value = value;
        }
    }

    // Number literal
    public static class NumberLiteral extends Expression {
        public double value;

        public NumberLiteral(double value, int line, int column) {
            super(line, column);
            this.value = value;
        }
    }

    // Boolean literal
    public static class BooleanLiteral extends Expression {
        public boolean value;

        public BooleanLiteral(boolean value, int line, int column) {
            super(line, column);
            this.value = value;
        }
    }

    // Identifier (variable reference)
    public static class Identifier extends Expression {
        public String name;

        public Identifier(String name, int line, int column) {
            super(line, column);
            this.name = name;
        }
    }

    // Binary operation: a + b
    public static class BinaryOp extends Expression {
        public Expression left;
        public String operator; // "+", "-", "*", "/", etc.
        public Expression right;

        public BinaryOp(Expression left, String operator, Expression right, int line, int column) {
            super(line, column);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    // Comparison: a is greater than b
    public static class Comparison extends Expression {
        public Expression left;
        public String operator; // "is", "equals", "greater than", etc.
        public Expression right;

        public Comparison(Expression left, String operator, Expression right, int line, int column) {
            super(line, column);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    // Member access: object.property
    public static class MemberAccess extends Expression {
        public Expression object;
        public String member;

        public MemberAccess(Expression object, String member, int line, int column) {
            super(line, column);
            this.object = object;
            this.member = member;
        }
    }
}
