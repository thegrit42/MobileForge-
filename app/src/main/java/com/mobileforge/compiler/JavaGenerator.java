package com.mobileforge.compiler;

import java.util.HashMap;
import java.util.Map;

public class JavaGenerator {
    private StringBuilder output;
    private int indentLevel;
    private String packageName;
    private String className;
    private Map<String, String> uiElements;
    private int elementCounter;

    public JavaGenerator() {
        this.output = new StringBuilder();
        this.indentLevel = 0;
        this.packageName = "com.mobileforge.generated";
        this.className = "MainActivity";
        this.uiElements = new HashMap<>();
        this.elementCounter = 0;
    }

    public String generate(ASTNode.Program program) {
        // Generate package and imports
        emit("package " + packageName + ";");
        emitLine();
        emit("import android.app.Activity;");
        emit("import android.os.Bundle;");
        emit("import android.widget.*;");
        emit("import android.view.*;");
        emit("import android.content.Intent;");
        emit("import android.util.Log;");
        emitLine();

        // Generate class
        emit("public class " + className + " extends Activity {");
        indent();

        emitLine();
        emit("private static final String TAG = \"MobileForge\";");
        emitLine();

        // Generate onCreate method
        emit("@Override");
        emit("protected void onCreate(Bundle savedInstanceState) {");
        indent();
        emit("super.onCreate(savedInstanceState);");
        emitLine();

        // Create root layout
        emit("LinearLayout rootLayout = new LinearLayout(this);");
        emit("rootLayout.setOrientation(LinearLayout.VERTICAL);");
        emit("rootLayout.setLayoutParams(new LinearLayout.LayoutParams(");
        emit("    LinearLayout.LayoutParams.MATCH_PARENT,");
        emit("    LinearLayout.LayoutParams.MATCH_PARENT");
        emit("));");
        emitLine();

        // Generate statements
        for (ASTNode.Statement stmt : program.statements) {
            generateStatement(stmt, "rootLayout");
        }

        emitLine();
        emit("setContentView(rootLayout);");

        dedent();
        emit("}");
        emitLine();

        // Generate helper methods
        generateHelperMethods();

        dedent();
        emit("}");

        return output.toString();
    }

    private void generateStatement(ASTNode.Statement stmt, String parentView) {
        if (stmt instanceof ASTNode.BuildStatement) {
            generateBuild((ASTNode.BuildStatement) stmt, parentView);
        } else if (stmt instanceof ASTNode.CreateStatement) {
            generateCreate((ASTNode.CreateStatement) stmt, parentView);
        } else if (stmt instanceof ASTNode.PlaceStatement) {
            generatePlace((ASTNode.PlaceStatement) stmt, parentView);
        } else if (stmt instanceof ASTNode.WhenStatement) {
            generateWhen((ASTNode.WhenStatement) stmt);
        } else if (stmt instanceof ASTNode.IfStatement) {
            generateIf((ASTNode.IfStatement) stmt, parentView);
        } else if (stmt instanceof ASTNode.ForStatement) {
            generateFor((ASTNode.ForStatement) stmt, parentView);
        } else if (stmt instanceof ASTNode.WhileStatement) {
            generateWhile((ASTNode.WhileStatement) stmt, parentView);
        } else if (stmt instanceof ASTNode.RepeatStatement) {
            generateRepeat((ASTNode.RepeatStatement) stmt, parentView);
        } else if (stmt instanceof ASTNode.AssignmentStatement) {
            generateAssignment((ASTNode.AssignmentStatement) stmt);
        } else if (stmt instanceof ASTNode.ActionStatement) {
            generateAction((ASTNode.ActionStatement) stmt);
        } else if (stmt instanceof ASTNode.PropertyStatement) {
            generateProperty((ASTNode.PropertyStatement) stmt);
        }
    }

    private void generateBuild(ASTNode.BuildStatement stmt, String parentView) {
        if (stmt.objectType.equals("app") || stmt.objectType.equals("android_app")) {
            // App-level build, just process body
            for (ASTNode.Statement bodyStmt : stmt.body) {
                generateStatement(bodyStmt, parentView);
            }
        }
    }

    private void generateCreate(ASTNode.CreateStatement stmt, String parentView) {
        String varName = generateElementName(stmt.objectType);
        String javaType = getJavaType(stmt.objectType);

        emit(javaType + " " + varName + " = new " + javaType + "(this);");

        // Set name/text if provided
        if (!stmt.name.isEmpty()) {
            if (stmt.objectType.equals("button") || stmt.objectType.equals("text")) {
                emit(varName + ".setText(\"" + escapeString(stmt.name) + "\");");
            }
        }

        // Process body for properties and nested elements
        for (ASTNode.Statement bodyStmt : stmt.body) {
            if (bodyStmt instanceof ASTNode.PropertyStatement) {
                generatePropertyForElement(varName, (ASTNode.PropertyStatement) bodyStmt);
            } else if (bodyStmt instanceof ASTNode.WhenStatement) {
                generateWhenForElement(varName, (ASTNode.WhenStatement) bodyStmt);
            } else {
                generateStatement(bodyStmt, varName);
            }
        }

        // Add to parent
        if (parentView != null) {
            emit(parentView + ".addView(" + varName + ");");
        }
        emitLine();

        uiElements.put(stmt.name, varName);
    }

    private void generatePlace(ASTNode.PlaceStatement stmt, String parentView) {
        String varName = generateElementName(stmt.objectType);
        String javaType = getJavaType(stmt.objectType);

        emit(javaType + " " + varName + " = new " + javaType + "(this);");

        // Set text if provided
        if (!stmt.name.isEmpty()) {
            if (stmt.objectType.equals("button") || stmt.objectType.equals("text")) {
                emit(varName + ".setText(\"" + escapeString(stmt.name) + "\");");
            } else if (stmt.objectType.equals("input")) {
                emit(varName + ".setHint(\"" + escapeString(stmt.name) + "\");");
            }
        }

        // Process properties
        for (Map.Entry<String, ASTNode.Expression> entry : stmt.properties.entrySet()) {
            applyProperty(varName, entry.getKey(), entry.getValue());
        }

        // Process body
        for (ASTNode.Statement bodyStmt : stmt.body) {
            if (bodyStmt instanceof ASTNode.PropertyStatement) {
                generatePropertyForElement(varName, (ASTNode.PropertyStatement) bodyStmt);
            } else if (bodyStmt instanceof ASTNode.WhenStatement) {
                generateWhenForElement(varName, (ASTNode.WhenStatement) bodyStmt);
            } else {
                generateStatement(bodyStmt, varName);
            }
        }

        // Add to parent with positioning
        if (parentView != null) {
            if (stmt.position != null && stmt.position.contains("center")) {
                emit("LinearLayout.LayoutParams " + varName + "Params = new LinearLayout.LayoutParams(");
                emit("    LinearLayout.LayoutParams.WRAP_CONTENT,");
                emit("    LinearLayout.LayoutParams.WRAP_CONTENT");
                emit(");");
                emit(varName + "Params.gravity = Gravity.CENTER;");
                emit(varName + ".setLayoutParams(" + varName + "Params);");
            }
            emit(parentView + ".addView(" + varName + ");");
        }
        emitLine();

        uiElements.put(stmt.name, varName);
    }

    private void generateWhen(ASTNode.WhenStatement stmt) {
        // This is handled inline when attached to elements
    }

    private void generateWhenForElement(String elementVar, ASTNode.WhenStatement stmt) {
        if (stmt.eventType.contains("clicked") || stmt.eventType.contains("click")) {
            emit(elementVar + ".setOnClickListener(new View.OnClickListener() {");
            indent();
            emit("@Override");
            emit("public void onClick(View v) {");
            indent();

            for (ASTNode.Statement bodyStmt : stmt.body) {
                generateStatement(bodyStmt, null);
            }

            dedent();
            emit("}");
            dedent();
            emit("});");
        }
    }

    private void generateIf(ASTNode.IfStatement stmt, String parentView) {
        emit("if (" + generateExpression(stmt.condition) + ") {");
        indent();

        for (ASTNode.Statement bodyStmt : stmt.thenBody) {
            generateStatement(bodyStmt, parentView);
        }

        dedent();
        if (!stmt.elseBody.isEmpty()) {
            emit("} else {");
            indent();

            for (ASTNode.Statement bodyStmt : stmt.elseBody) {
                generateStatement(bodyStmt, parentView);
            }

            dedent();
        }
        emit("}");
    }

    private void generateFor(ASTNode.ForStatement stmt, String parentView) {
        String iterableCode = generateExpression(stmt.iterable);
        emit("for (Object " + stmt.variable + " : " + iterableCode + ") {");
        indent();

        for (ASTNode.Statement bodyStmt : stmt.body) {
            generateStatement(bodyStmt, parentView);
        }

        dedent();
        emit("}");
    }

    private void generateWhile(ASTNode.WhileStatement stmt, String parentView) {
        emit("while (" + generateExpression(stmt.condition) + ") {");
        indent();

        for (ASTNode.Statement bodyStmt : stmt.body) {
            generateStatement(bodyStmt, parentView);
        }

        dedent();
        emit("}");
    }

    private void generateRepeat(ASTNode.RepeatStatement stmt, String parentView) {
        String countCode = generateExpression(stmt.count);
        emit("for (int i = 0; i < " + countCode + "; i++) {");
        indent();

        for (ASTNode.Statement bodyStmt : stmt.body) {
            generateStatement(bodyStmt, parentView);
        }

        dedent();
        emit("}");
    }

    private void generateAssignment(ASTNode.AssignmentStatement stmt) {
        String valueCode = generateExpression(stmt.value);
        emit(stmt.variable + " = " + valueCode + ";");
    }

    private void generateAction(ASTNode.ActionStatement stmt) {
        if (stmt.action.equals("show")) {
            if (stmt.target != null && stmt.target.equals("message")) {
                String msg = stmt.value != null ? generateExpression(stmt.value) : "\"\"";
                emit("showMessage(" + msg + ");");
            }
        } else if (stmt.action.equals("print")) {
            String value = stmt.value != null ? generateExpression(stmt.value) : "\"\"";
            emit("Log.d(TAG, String.valueOf(" + value + "));");
        }
    }

    private void generateProperty(ASTNode.PropertyStatement stmt) {
        // Standalone properties are typically handled inline
    }

    private void generatePropertyForElement(String elementVar, ASTNode.PropertyStatement stmt) {
        applyProperty(elementVar, stmt.property, stmt.value);
    }

    private void applyProperty(String elementVar, String property, ASTNode.Expression value) {
        String valueCode = value != null ? generateExpression(value) : "true";

        switch (property.toLowerCase()) {
            case "color":
                emit(elementVar + ".setTextColor(android.graphics.Color.parseColor(" + valueCode + "));");
                break;
            case "size":
                if (value instanceof ASTNode.Identifier) {
                    String size = ((ASTNode.Identifier) value).name.toLowerCase();
                    int textSize = getSizeInSp(size);
                    emit(elementVar + ".setTextSize(" + textSize + ");");
                }
                break;
            case "large":
                emit(elementVar + ".setTextSize(24);");
                break;
            case "small":
                emit(elementVar + ".setTextSize(12);");
                break;
            case "bold":
                emit(elementVar + ".setTypeface(null, android.graphics.Typeface.BOLD);");
                break;
            case "hint":
                emit(elementVar + ".setHint(" + valueCode + ");");
                break;
            case "background":
                emit(elementVar + ".setBackgroundColor(android.graphics.Color.parseColor(" + valueCode + "));");
                break;
        }
    }

    private String generateExpression(ASTNode.Expression expr) {
        if (expr instanceof ASTNode.StringLiteral) {
            return "\"" + escapeString(((ASTNode.StringLiteral) expr).value) + "\"";
        } else if (expr instanceof ASTNode.NumberLiteral) {
            double val = ((ASTNode.NumberLiteral) expr).value;
            if (val == (long) val) {
                return String.valueOf((long) val);
            }
            return String.valueOf(val);
        } else if (expr instanceof ASTNode.BooleanLiteral) {
            return String.valueOf(((ASTNode.BooleanLiteral) expr).value);
        } else if (expr instanceof ASTNode.Identifier) {
            String name = ((ASTNode.Identifier) expr).name;
            // Check if it's a UI element reference
            if (uiElements.containsKey(name)) {
                return uiElements.get(name);
            }
            return name;
        } else if (expr instanceof ASTNode.BinaryOp) {
            ASTNode.BinaryOp binOp = (ASTNode.BinaryOp) expr;
            String left = generateExpression(binOp.left);
            String right = generateExpression(binOp.right);
            return "(" + left + " " + binOp.operator + " " + right + ")";
        } else if (expr instanceof ASTNode.Comparison) {
            ASTNode.Comparison comp = (ASTNode.Comparison) expr;
            String left = generateExpression(comp.left);
            String right = generateExpression(comp.right);
            String op = getJavaComparisonOp(comp.operator);
            return "(" + left + " " + op + " " + right + ")";
        } else if (expr instanceof ASTNode.MemberAccess) {
            ASTNode.MemberAccess ma = (ASTNode.MemberAccess) expr;
            return generateExpression(ma.object) + "." + ma.member;
        }

        return "null";
    }

    private void generateHelperMethods() {
        // showMessage helper
        emit("private void showMessage(String message) {");
        indent();
        emit("android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();");
        dedent();
        emit("}");
        emitLine();
    }

    private String getJavaType(String mfnlType) {
        switch (mfnlType.toLowerCase()) {
            case "button": return "Button";
            case "text": return "TextView";
            case "input": return "EditText";
            case "image": return "ImageView";
            case "list": return "ListView";
            case "screen": return "LinearLayout";
            case "header": return "TextView";
            case "footer": return "TextView";
            case "menu": return "Menu";
            default: return "View";
        }
    }

    private String generateElementName(String type) {
        return type.toLowerCase() + (elementCounter++);
    }

    private String getJavaComparisonOp(String mfnlOp) {
        switch (mfnlOp.toLowerCase()) {
            case "is":
            case "equals": return "==";
            case "greater than": return ">";
            case "less than": return "<";
            default: return "==";
        }
    }

    private int getSizeInSp(String size) {
        switch (size) {
            case "tiny": return 10;
            case "small": return 12;
            case "medium": return 16;
            case "large": return 24;
            case "huge": return 32;
            default: return 16;
        }
    }

    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\t", "\\t");
    }

    private void emit(String code) {
        for (int i = 0; i < indentLevel; i++) {
            output.append("    ");
        }
        output.append(code).append("\n");
    }

    private void emitLine() {
        output.append("\n");
    }

    private void indent() {
        indentLevel++;
    }

    private void dedent() {
        if (indentLevel > 0) {
            indentLevel--;
        }
    }
}
