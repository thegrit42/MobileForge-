package com.mobileforge.compiler;

import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public ASTNode.Program parse() {
        ASTNode.Program program = new ASTNode.Program();

        while (!isAtEnd()) {
            skipNewlines();
            if (isAtEnd()) break;

            ASTNode.Statement stmt = parseStatement();
            if (stmt != null) {
                program.statements.add(stmt);
            }
            skipNewlines();
        }

        return program;
    }

    private ASTNode.Statement parseStatement() {
        Token current = peek();

        switch (current.type) {
            case BUILD:
                return parseBuild();
            case CREATE:
                return parseCreate();
            case PLACE:
                return parsePlace();
            case WHEN:
                return parseWhen();
            case IF:
                return parseIf();
            case FOR:
                return parseFor();
            case WHILE:
                return parseWhile();
            case REPEAT:
                return parseRepeat();
            case FUNCTION:
                return parseFunction();
            case IMPORT:
                return parseImport();
            case SET:
            case CHANGE:
                return parseAssignment();
            case SHOW:
            case HIDE:
            case PRINT:
            case SAVE:
            case DISPLAY:
            case MARK:
            case INCREASE:
            case DECREASE:
            case OPEN:
            case SHARE:
                return parseAction();
            case CALL:
                return parseCall();
            case RETURN:
                return parseReturn();
            case ADD:
                return parseAdd();
            case COLOR:
            case SIZE:
            case BACKGROUND:
            case WIDTH:
            case HEIGHT:
            case HINT:
                return parseProperty();
            default:
                if (isIdentifier()) {
                    return parseProperty();
                }
                advance(); // Skip unknown token
                return null;
        }
    }

    private ASTNode.BuildStatement parseBuild() {
        Token buildToken = advance(); // consume 'build'

        // Next should be object type (app, feature, etc.)
        Token objectType = advance();
        String objectTypeName = objectType.value.toLowerCase();

        // Next might be android (for "build android app")
        if (match(Token.Type.ANDROID)) {
            objectTypeName = "android_" + objectTypeName;
        }

        // Next should be name (string literal)
        String name = "";
        if (match(Token.Type.STRING)) {
            name = previous().value;
        }

        ASTNode.BuildStatement stmt = new ASTNode.BuildStatement(
            objectTypeName, name, buildToken.line, buildToken.column
        );

        skipNewlines();

        // Parse body if indented
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;
                ASTNode.Statement bodyStmt = parseStatement();
                if (bodyStmt != null) {
                    stmt.body.add(bodyStmt);
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        // Consume 'end' if present
        if (match(Token.Type.END)) {
            advance(); // consume object type after end
        }

        return stmt;
    }

    private ASTNode.CreateStatement parseCreate() {
        Token createToken = advance(); // consume 'create'

        Token objectType = advance();
        String objectTypeName = objectType.value.toLowerCase();

        String name = "";
        if (match(Token.Type.STRING)) {
            name = previous().value;
        } else if (isIdentifier()) {
            name = advance().value;
        }

        ASTNode.CreateStatement stmt = new ASTNode.CreateStatement(
            objectTypeName, name, createToken.line, createToken.column
        );

        skipNewlines();

        // Parse body
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;
                ASTNode.Statement bodyStmt = parseStatement();
                if (bodyStmt != null) {
                    stmt.body.add(bodyStmt);
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume object type after end
        }

        return stmt;
    }

    private ASTNode.PlaceStatement parsePlace() {
        Token placeToken = advance(); // consume 'place'

        Token objectType = advance();
        String objectTypeName = objectType.value.toLowerCase();

        String name = "";
        if (match(Token.Type.STRING)) {
            name = previous().value;
        }

        ASTNode.PlaceStatement stmt = new ASTNode.PlaceStatement(
            objectTypeName, name, placeToken.line, placeToken.column
        );

        // Check for position (at top, below, etc.)
        if (match(Token.Type.AT)) {
            stmt.position = advance().value;
            // Handle "at top center" etc
            if (!check(Token.Type.NEWLINE)) {
                stmt.position += " " + advance().value;
            }
        } else if (match(Token.Type.ABOVE, Token.Type.BELOW, Token.Type.BESIDE)) {
            stmt.position = previous().value;
            if (!check(Token.Type.NEWLINE)) {
                stmt.position += " " + advance().value;
            }
        }

        skipNewlines();

        // Parse body
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;
                ASTNode.Statement bodyStmt = parseStatement();
                if (bodyStmt != null) {
                    stmt.body.add(bodyStmt);
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume object type after end
        }

        return stmt;
    }

    private ASTNode.WhenStatement parseWhen() {
        Token whenToken = advance(); // consume 'when'

        // Parse event (button clicked, screen loaded, etc.)
        StringBuilder eventBuilder = new StringBuilder();
        while (!check(Token.Type.NEWLINE) && !isAtEnd()) {
            eventBuilder.append(advance().value).append(" ");
        }
        String event = eventBuilder.toString().trim().toLowerCase();

        ASTNode.WhenStatement stmt = new ASTNode.WhenStatement(event, whenToken.line, whenToken.column);

        skipNewlines();

        // Parse body
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;
                ASTNode.Statement bodyStmt = parseStatement();
                if (bodyStmt != null) {
                    stmt.body.add(bodyStmt);
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume 'when'
        }

        return stmt;
    }

    private ASTNode.IfStatement parseIf() {
        Token ifToken = advance(); // consume 'if'

        ASTNode.Expression condition = parseExpression();

        ASTNode.IfStatement stmt = new ASTNode.IfStatement(condition, ifToken.line, ifToken.column);

        skipNewlines();

        // Parse then body
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !check(Token.Type.ELSE) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT) || check(Token.Type.ELSE)) break;
                ASTNode.Statement bodyStmt = parseStatement();
                if (bodyStmt != null) {
                    stmt.thenBody.add(bodyStmt);
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        // Parse else if present
        if (match(Token.Type.ELSE)) {
            skipNewlines();
            if (match(Token.Type.INDENT)) {
                while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                    skipNewlines();
                    if (check(Token.Type.DEDENT)) break;
                    ASTNode.Statement bodyStmt = parseStatement();
                    if (bodyStmt != null) {
                        stmt.elseBody.add(bodyStmt);
                    }
                    skipNewlines();
                }
                if (match(Token.Type.DEDENT)) {
                    // Consumed dedent
                }
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume 'if'
        }

        return stmt;
    }

    private ASTNode.ForStatement parseFor() {
        Token forToken = advance(); // consume 'for'

        match(Token.Type.EACH); // optional 'each'

        String variable = advance().value;

        match(Token.Type.IN);

        ASTNode.Expression iterable = parseExpression();

        ASTNode.ForStatement stmt = new ASTNode.ForStatement(variable, iterable, forToken.line, forToken.column);

        skipNewlines();

        // Parse body
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;
                ASTNode.Statement bodyStmt = parseStatement();
                if (bodyStmt != null) {
                    stmt.body.add(bodyStmt);
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume 'for'
        }

        return stmt;
    }

    private ASTNode.WhileStatement parseWhile() {
        Token whileToken = advance(); // consume 'while'

        ASTNode.Expression condition = parseExpression();

        ASTNode.WhileStatement stmt = new ASTNode.WhileStatement(condition, whileToken.line, whileToken.column);

        skipNewlines();

        // Parse body
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;
                ASTNode.Statement bodyStmt = parseStatement();
                if (bodyStmt != null) {
                    stmt.body.add(bodyStmt);
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume 'while'
        }

        return stmt;
    }

    private ASTNode.RepeatStatement parseRepeat() {
        Token repeatToken = advance(); // consume 'repeat'

        ASTNode.Expression count = parseExpression();

        match(Token.Type.TIMES_KEYWORD); // optional 'times'

        ASTNode.RepeatStatement stmt = new ASTNode.RepeatStatement(count, repeatToken.line, repeatToken.column);

        skipNewlines();

        // Parse body
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;
                ASTNode.Statement bodyStmt = parseStatement();
                if (bodyStmt != null) {
                    stmt.body.add(bodyStmt);
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume 'repeat'
        }

        return stmt;
    }

    private ASTNode.FunctionStatement parseFunction() {
        advance(); // consume 'create'
        Token funcToken = advance(); // consume 'function'

        String name = "";
        if (match(Token.Type.STRING)) {
            name = previous().value;
        } else if (isIdentifier()) {
            name = advance().value;
        }

        ASTNode.FunctionStatement stmt = new ASTNode.FunctionStatement(name, funcToken.line, funcToken.column);

        skipNewlines();

        // Parse body
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;

                // Check for 'input' parameter
                if (match(Token.Type.INPUT)) {
                    String param = advance().value;
                    stmt.parameters.add(param);
                } else {
                    ASTNode.Statement bodyStmt = parseStatement();
                    if (bodyStmt != null) {
                        stmt.body.add(bodyStmt);
                    }
                }
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume 'function'
        }

        return stmt;
    }

    private ASTNode.ImportStatement parseImport() {
        Token importToken = advance(); // consume 'import'

        ASTNode.ImportStatement stmt = new ASTNode.ImportStatement(importToken.line, importToken.column);

        skipNewlines();

        // Parse imports
        if (match(Token.Type.INDENT)) {
            while (!check(Token.Type.DEDENT) && !isAtEnd()) {
                skipNewlines();
                if (check(Token.Type.DEDENT)) break;

                StringBuilder importName = new StringBuilder();
                while (!check(Token.Type.NEWLINE) && !isAtEnd()) {
                    importName.append(advance().value).append(" ");
                }
                stmt.imports.add(importName.toString().trim());
                skipNewlines();
            }
            if (match(Token.Type.DEDENT)) {
                // Consumed dedent
            }
        }

        if (match(Token.Type.END)) {
            advance(); // consume 'import'
        }

        return stmt;
    }

    private ASTNode.AssignmentStatement parseAssignment() {
        Token actionToken = advance(); // consume 'set' or 'change'

        String variable = advance().value;

        match(Token.Type.TO); // optional 'to'

        ASTNode.Expression value = parseExpression();

        ASTNode.AssignmentStatement stmt = new ASTNode.AssignmentStatement(
            variable, value, actionToken.line, actionToken.column
        );
        stmt.action = actionToken.value.toLowerCase();

        return stmt;
    }

    private ASTNode.ActionStatement parseAction() {
        Token actionToken = advance(); // consume action verb

        ASTNode.ActionStatement stmt = new ASTNode.ActionStatement(
            actionToken.value.toLowerCase(), actionToken.line, actionToken.column
        );

        // Parse target (message, file, etc.)
        if (match(Token.Type.MESSAGE, Token.Type.TEXT, Token.Type.FILE)) {
            stmt.target = previous().value.toLowerCase();
        }

        // Parse value
        if (!check(Token.Type.NEWLINE) && !isAtEnd()) {
            stmt.value = parseExpression();
        }

        return stmt;
    }

    private ASTNode.CallStatement parseCall() {
        Token callToken = advance(); // consume 'call'

        String functionName = "";
        StringBuilder nameBuilder = new StringBuilder();
        while (!check(Token.Type.WITH) && !check(Token.Type.NEWLINE) && !isAtEnd()) {
            nameBuilder.append(advance().value).append(" ");
        }
        functionName = nameBuilder.toString().trim();

        ASTNode.CallStatement stmt = new ASTNode.CallStatement(functionName, callToken.line, callToken.column);

        // Parse arguments if 'with' present
        if (match(Token.Type.WITH)) {
            while (!check(Token.Type.NEWLINE) && !isAtEnd()) {
                stmt.arguments.add(parseExpression());
                match(Token.Type.AND); // optional 'and'
            }
        }

        return stmt;
    }

    private ASTNode.ReturnStatement parseReturn() {
        Token returnToken = advance(); // consume 'return'

        ASTNode.Expression value = null;
        if (!check(Token.Type.NEWLINE) && !isAtEnd()) {
            value = parseExpression();
        }

        return new ASTNode.ReturnStatement(value, returnToken.line, returnToken.column);
    }

    private ASTNode.Statement parseAdd() {
        Token addToken = advance(); // consume 'add'

        // This is context-dependent - could be adding to list, adding UI element, etc.
        ASTNode.ActionStatement stmt = new ASTNode.ActionStatement("add", addToken.line, addToken.column);

        // Parse what's being added
        if (!check(Token.Type.NEWLINE) && !isAtEnd()) {
            stmt.value = parseExpression();
        }

        // Check for 'to' clause
        if (match(Token.Type.TO)) {
            stmt.target = advance().value;
        }

        return stmt;
    }

    private ASTNode.PropertyStatement parseProperty() {
        Token propToken = advance();

        String property = propToken.value.toLowerCase();

        ASTNode.Expression value = null;
        if (!check(Token.Type.NEWLINE) && !isAtEnd()) {
            value = parseExpression();
        }

        return new ASTNode.PropertyStatement(property, value, propToken.line, propToken.column);
    }

    private ASTNode.Expression parseExpression() {
        return parseComparison();
    }

    private ASTNode.Expression parseComparison() {
        ASTNode.Expression left = parseAddition();

        while (match(Token.Type.IS, Token.Type.EQUALS, Token.Type.GREATER, Token.Type.LESS)) {
            Token op = previous();
            String operator = op.value.toLowerCase();

            // Handle "greater than", "less than"
            if (match(Token.Type.THAN)) {
                operator += " than";
            }

            ASTNode.Expression right = parseAddition();
            left = new ASTNode.Comparison(left, operator, right, op.line, op.column);
        }

        return left;
    }

    private ASTNode.Expression parseAddition() {
        ASTNode.Expression left = parseMultiplication();

        while (match(Token.Type.PLUS, Token.Type.MINUS)) {
            Token op = previous();
            ASTNode.Expression right = parseMultiplication();
            left = new ASTNode.BinaryOp(left, op.value, right, op.line, op.column);
        }

        return left;
    }

    private ASTNode.Expression parseMultiplication() {
        ASTNode.Expression left = parsePrimary();

        while (match(Token.Type.TIMES, Token.Type.DIVIDED)) {
            Token op = previous();
            String operator = op.value;
            if (match(Token.Type.BY)) {
                operator += " by";
            }
            ASTNode.Expression right = parsePrimary();
            left = new ASTNode.BinaryOp(left, operator, right, op.line, op.column);
        }

        return left;
    }

    private ASTNode.Expression parsePrimary() {
        Token current = peek();

        if (match(Token.Type.STRING)) {
            return new ASTNode.StringLiteral(previous().value, current.line, current.column);
        }

        if (match(Token.Type.INTEGER, Token.Type.DECIMAL)) {
            double value = Double.parseDouble(previous().value);
            return new ASTNode.NumberLiteral(value, current.line, current.column);
        }

        if (match(Token.Type.TRUE, Token.Type.YES, Token.Type.ON)) {
            return new ASTNode.BooleanLiteral(true, current.line, current.column);
        }

        if (match(Token.Type.FALSE, Token.Type.NO, Token.Type.OFF)) {
            return new ASTNode.BooleanLiteral(false, current.line, current.column);
        }

        if (match(Token.Type.LPAREN)) {
            ASTNode.Expression expr = parseExpression();
            match(Token.Type.RPAREN);
            return expr;
        }

        if (isIdentifier()) {
            Token id = advance();
            ASTNode.Expression expr = new ASTNode.Identifier(id.value, id.line, id.column);

            // Check for member access
            if (match(Token.Type.DOT)) {
                String member = advance().value;
                expr = new ASTNode.MemberAccess(expr, member, id.line, id.column);
            }

            return expr;
        }

        // Return a dummy identifier if nothing else matches
        advance();
        return new ASTNode.Identifier(previous().value, current.line, current.column);
    }

    // Helper methods
    private boolean isAtEnd() {
        return pos >= tokens.size() || peek().type == Token.Type.EOF;
    }

    private Token peek() {
        if (pos < tokens.size()) {
            return tokens.get(pos);
        }
        return tokens.get(tokens.size() - 1); // EOF token
    }

    private Token advance() {
        if (!isAtEnd()) pos++;
        return previous();
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private boolean check(Token.Type type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean check(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) return true;
        }
        return false;
    }

    private boolean match(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private void skipNewlines() {
        while (match(Token.Type.NEWLINE)) {
            // Skip all newlines
        }
    }

    private boolean isIdentifier() {
        return check(Token.Type.IDENTIFIER);
    }
}
