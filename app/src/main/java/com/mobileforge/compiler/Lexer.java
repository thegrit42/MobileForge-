package com.mobileforge.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private String source;
    private int pos;
    private int line;
    private int column;
    private List<Token> tokens;
    private int currentIndent;
    private List<Integer> indentStack;

    private static final Map<String, Token.Type> KEYWORDS = new HashMap<>();

    static {
        // Actions
        KEYWORDS.put("build", Token.Type.BUILD);
        KEYWORDS.put("create", Token.Type.CREATE);
        KEYWORDS.put("place", Token.Type.PLACE);
        KEYWORDS.put("add", Token.Type.ADD);
        KEYWORDS.put("make", Token.Type.MAKE);
        KEYWORDS.put("change", Token.Type.CHANGE);
        KEYWORDS.put("set", Token.Type.SET);
        KEYWORDS.put("update", Token.Type.UPDATE);
        KEYWORDS.put("remove", Token.Type.REMOVE);
        KEYWORDS.put("when", Token.Type.WHEN);
        KEYWORDS.put("if", Token.Type.IF);
        KEYWORDS.put("else", Token.Type.ELSE);
        KEYWORDS.put("repeat", Token.Type.REPEAT);
        KEYWORDS.put("while", Token.Type.WHILE);
        KEYWORDS.put("for", Token.Type.FOR);
        KEYWORDS.put("each", Token.Type.EACH);
        KEYWORDS.put("in", Token.Type.IN);
        KEYWORDS.put("from", Token.Type.FROM);
        KEYWORDS.put("to", Token.Type.TO);
        KEYWORDS.put("import", Token.Type.IMPORT);
        KEYWORDS.put("use", Token.Type.USE);
        KEYWORDS.put("connect", Token.Type.CONNECT);
        KEYWORDS.put("load", Token.Type.LOAD);
        KEYWORDS.put("try", Token.Type.TRY);
        KEYWORDS.put("on", Token.Type.ON);
        KEYWORDS.put("end", Token.Type.END);
        KEYWORDS.put("call", Token.Type.CALL);
        KEYWORDS.put("with", Token.Type.WITH);
        KEYWORDS.put("return", Token.Type.RETURN);
        KEYWORDS.put("go", Token.Type.GO);
        KEYWORDS.put("back", Token.Type.BACK);

        // Objects
        KEYWORDS.put("app", Token.Type.APP);
        KEYWORDS.put("screen", Token.Type.SCREEN);
        KEYWORDS.put("button", Token.Type.BUTTON);
        KEYWORDS.put("text", Token.Type.TEXT);
        KEYWORDS.put("image", Token.Type.IMAGE);
        KEYWORDS.put("header", Token.Type.HEADER);
        KEYWORDS.put("footer", Token.Type.FOOTER);
        KEYWORDS.put("input", Token.Type.INPUT);
        KEYWORDS.put("list", Token.Type.LIST);
        KEYWORDS.put("menu", Token.Type.MENU);
        KEYWORDS.put("function", Token.Type.FUNCTION);
        KEYWORDS.put("feature", Token.Type.FEATURE);
        KEYWORDS.put("class", Token.Type.CLASS);
        KEYWORDS.put("variable", Token.Type.VARIABLE);
        KEYWORDS.put("file", Token.Type.FILE);
        KEYWORDS.put("activity", Token.Type.ACTIVITY);
        KEYWORDS.put("page", Token.Type.PAGE);
        KEYWORDS.put("section", Token.Type.SECTION);
        KEYWORDS.put("container", Token.Type.CONTAINER);
        KEYWORDS.put("layout", Token.Type.LAYOUT);
        KEYWORDS.put("component", Token.Type.COMPONENT);
        KEYWORDS.put("service", Token.Type.SERVICE);
        KEYWORDS.put("database", Token.Type.DATABASE);
        KEYWORDS.put("setting", Token.Type.SETTING);
        KEYWORDS.put("popup", Token.Type.POPUP);
        KEYWORDS.put("field", Token.Type.FIELD);
        KEYWORDS.put("block", Token.Type.BLOCK);
        KEYWORDS.put("item", Token.Type.ITEM);
        KEYWORDS.put("number", Token.Type.NUMBER);

        // Properties
        KEYWORDS.put("large", Token.Type.LARGE);
        KEYWORDS.put("small", Token.Type.SMALL);
        KEYWORDS.put("medium", Token.Type.MEDIUM);
        KEYWORDS.put("tiny", Token.Type.TINY);
        KEYWORDS.put("huge", Token.Type.HUGE);
        KEYWORDS.put("bold", Token.Type.BOLD);
        KEYWORDS.put("italic", Token.Type.ITALIC);
        KEYWORDS.put("underlined", Token.Type.UNDERLINED);
        KEYWORDS.put("color", Token.Type.COLOR);
        KEYWORDS.put("background", Token.Type.BACKGROUND);
        KEYWORDS.put("width", Token.Type.WIDTH);
        KEYWORDS.put("height", Token.Type.HEIGHT);
        KEYWORDS.put("size", Token.Type.SIZE);
        KEYWORDS.put("top", Token.Type.TOP);
        KEYWORDS.put("bottom", Token.Type.BOTTOM);
        KEYWORDS.put("left", Token.Type.LEFT);
        KEYWORDS.put("right", Token.Type.RIGHT);
        KEYWORDS.put("center", Token.Type.CENTER);
        KEYWORDS.put("above", Token.Type.ABOVE);
        KEYWORDS.put("below", Token.Type.BELOW);
        KEYWORDS.put("beside", Token.Type.BESIDE);
        KEYWORDS.put("at", Token.Type.AT);
        KEYWORDS.put("visible", Token.Type.VISIBLE);
        KEYWORDS.put("hidden", Token.Type.HIDDEN);
        KEYWORDS.put("enabled", Token.Type.ENABLED);
        KEYWORDS.put("disabled", Token.Type.DISABLED);
        KEYWORDS.put("active", Token.Type.ACTIVE);
        KEYWORDS.put("inactive", Token.Type.INACTIVE);
        KEYWORDS.put("scrollable", Token.Type.SCROLLABLE);
        KEYWORDS.put("clickable", Token.Type.CLICKABLE);

        // Logical
        KEYWORDS.put("and", Token.Type.AND);
        KEYWORDS.put("or", Token.Type.OR);
        KEYWORDS.put("not", Token.Type.NOT);
        KEYWORDS.put("is", Token.Type.IS);
        KEYWORDS.put("equals", Token.Type.EQUALS);
        KEYWORDS.put("greater", Token.Type.GREATER);
        KEYWORDS.put("less", Token.Type.LESS);
        KEYWORDS.put("than", Token.Type.THAN);

        // Values
        KEYWORDS.put("true", Token.Type.TRUE);
        KEYWORDS.put("false", Token.Type.FALSE);
        KEYWORDS.put("yes", Token.Type.YES);
        KEYWORDS.put("no", Token.Type.NO);
        KEYWORDS.put("on", Token.Type.ON);
        KEYWORDS.put("off", Token.Type.OFF);

        // Events
        KEYWORDS.put("clicked", Token.Type.CLICKED);
        KEYWORDS.put("loaded", Token.Type.LOADED);
        KEYWORDS.put("created", Token.Type.CREATED);
        KEYWORDS.put("error", Token.Type.ERROR);

        // Permissions
        KEYWORDS.put("request", Token.Type.REQUEST);
        KEYWORDS.put("permission", Token.Type.PERMISSION);
        KEYWORDS.put("camera", Token.Type.CAMERA);
        KEYWORDS.put("location", Token.Type.LOCATION);
        KEYWORDS.put("storage", Token.Type.STORAGE);
        KEYWORDS.put("notifications", Token.Type.NOTIFICATIONS);
        KEYWORDS.put("internet", Token.Type.INTERNET);
        KEYWORDS.put("access", Token.Type.ACCESS);

        // Actions on data
        KEYWORDS.put("show", Token.Type.SHOW);
        KEYWORDS.put("hide", Token.Type.HIDE);
        KEYWORDS.put("save", Token.Type.SAVE);
        KEYWORDS.put("parse", Token.Type.PARSE);
        KEYWORDS.put("check", Token.Type.CHECK);
        KEYWORDS.put("highlight", Token.Type.HIGHLIGHT);
        KEYWORDS.put("suggest", Token.Type.SUGGEST);
        KEYWORDS.put("search", Token.Type.SEARCH);
        KEYWORDS.put("summarize", Token.Type.SUMMARIZE);
        KEYWORDS.put("display", Token.Type.DISPLAY);
        KEYWORDS.put("print", Token.Type.PRINT);
        KEYWORDS.put("mark", Token.Type.MARK);
        KEYWORDS.put("increase", Token.Type.INCREASE);
        KEYWORDS.put("decrease", Token.Type.DECREASE);
        KEYWORDS.put("open", Token.Type.OPEN);
        KEYWORDS.put("share", Token.Type.SHARE);
        KEYWORDS.put("welcome", Token.Type.WELCOME);

        // Connectors
        KEYWORDS.put("of", Token.Type.OF);
        KEYWORDS.put("the", Token.Type.THE);
        KEYWORDS.put("a", Token.Type.A);
        KEYWORDS.put("an", Token.Type.AN);
        KEYWORDS.put("as", Token.Type.AS);
        KEYWORDS.put("into", Token.Type.INTO);
        KEYWORDS.put("onto", Token.Type.ONTO);
        KEYWORDS.put("within", Token.Type.WITHIN);

        // Android
        KEYWORDS.put("android", Token.Type.ANDROID);
        KEYWORDS.put("resource", Token.Type.RESOURCE);
        KEYWORDS.put("external", Token.Type.EXTERNAL);

        // States
        KEYWORDS.put("empty", Token.Type.EMPTY);
        KEYWORDS.put("complete", Token.Type.COMPLETE);
        KEYWORDS.put("running", Token.Type.RUNNING);
        KEYWORDS.put("loading", Token.Type.LOADING);

        // Special
        KEYWORDS.put("times", Token.Type.TIMES_KEYWORD);
        KEYWORDS.put("message", Token.Type.MESSAGE);
        KEYWORDS.put("hint", Token.Type.HINT);
        KEYWORDS.put("paragraph", Token.Type.PARAGRAPH);
        KEYWORDS.put("heading", Token.Type.HEADING);
        KEYWORDS.put("price", Token.Type.PRICE);
        KEYWORDS.put("content", Token.Type.CONTENT);
        KEYWORDS.put("context", Token.Type.CONTEXT);
        KEYWORDS.put("default", Token.Type.DEFAULT);
        KEYWORDS.put("current", Token.Type.CURRENT);
        KEYWORDS.put("about", Token.Type.ABOUT);
        KEYWORDS.put("by", Token.Type.BY);
    }

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.tokens = new ArrayList<>();
        this.currentIndent = 0;
        this.indentStack = new ArrayList<>();
        this.indentStack.add(0);
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            char c = source.charAt(pos);

            // Skip whitespace (except newlines)
            if (c == ' ' || c == '\t') {
                advance();
                continue;
            }

            // Handle newlines and indentation
            if (c == '\n' || c == '\r') {
                handleNewline();
                continue;
            }

            // Skip comments
            if (c == '/' && peek() == '/') {
                skipLineComment();
                continue;
            }
            if (c == '/' && peek() == '*') {
                skipBlockComment();
                continue;
            }

            // String literals
            if (c == '"' || c == '\'') {
                tokens.add(readString(c));
                continue;
            }

            // Numbers
            if (Character.isDigit(c) || (c == '-' && peek() != ' ' && Character.isDigit(peek()))) {
                tokens.add(readNumber());
                continue;
            }

            // Identifiers and keywords
            if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifier());
                continue;
            }

            // Operators
            if (c == '+') {
                tokens.add(new Token(Token.Type.PLUS, "+", line, column));
                advance();
                continue;
            }
            if (c == '-') {
                tokens.add(new Token(Token.Type.MINUS, "-", line, column));
                advance();
                continue;
            }
            if (c == '*') {
                tokens.add(new Token(Token.Type.TIMES, "*", line, column));
                advance();
                continue;
            }
            if (c == '/') {
                tokens.add(new Token(Token.Type.DIVIDED, "/", line, column));
                advance();
                continue;
            }

            // Punctuation
            if (c == '(') {
                tokens.add(new Token(Token.Type.LPAREN, "(", line, column));
                advance();
                continue;
            }
            if (c == ')') {
                tokens.add(new Token(Token.Type.RPAREN, ")", line, column));
                advance();
                continue;
            }
            if (c == ',') {
                tokens.add(new Token(Token.Type.COMMA, ",", line, column));
                advance();
                continue;
            }
            if (c == '.') {
                tokens.add(new Token(Token.Type.DOT, ".", line, column));
                advance();
                continue;
            }
            if (c == ':') {
                tokens.add(new Token(Token.Type.COLON, ":", line, column));
                advance();
                continue;
            }

            // Unknown character
            advance();
        }

        // Add EOF
        tokens.add(new Token(Token.Type.EOF, "", line, column));
        return tokens;
    }

    private void handleNewline() {
        while (pos < source.length() && (source.charAt(pos) == '\n' || source.charAt(pos) == '\r')) {
            if (source.charAt(pos) == '\n') {
                line++;
                column = 1;
            }
            pos++;
        }

        // Calculate indentation on the new line
        int spaces = 0;
        while (pos < source.length() && (source.charAt(pos) == ' ' || source.charAt(pos) == '\t')) {
            if (source.charAt(pos) == '\t') {
                spaces += 4; // Tab = 4 spaces
            } else {
                spaces++;
            }
            pos++;
            column++;
        }

        // Skip blank lines
        if (pos < source.length() && (source.charAt(pos) == '\n' || source.charAt(pos) == '\r')) {
            return;
        }

        // Skip comment lines
        if (pos < source.length() && source.charAt(pos) == '/' && peek() == '/') {
            return;
        }

        // Add NEWLINE token
        tokens.add(new Token(Token.Type.NEWLINE, "\\n", line, 1));

        // Handle indentation changes
        int prevIndent = indentStack.get(indentStack.size() - 1);
        if (spaces > prevIndent) {
            indentStack.add(spaces);
            tokens.add(new Token(Token.Type.INDENT, "", line, column));
        } else if (spaces < prevIndent) {
            while (indentStack.size() > 1 && indentStack.get(indentStack.size() - 1) > spaces) {
                indentStack.remove(indentStack.size() - 1);
                tokens.add(new Token(Token.Type.DEDENT, "", line, column));
            }
        }
    }

    private Token readString(char quote) {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        advance(); // Skip opening quote

        while (pos < source.length() && source.charAt(pos) != quote) {
            if (source.charAt(pos) == '\\' && pos + 1 < source.length()) {
                advance();
                char escaped = source.charAt(pos);
                if (escaped == 'n') sb.append('\n');
                else if (escaped == 't') sb.append('\t');
                else if (escaped == '\\') sb.append('\\');
                else if (escaped == quote) sb.append(quote);
                else sb.append(escaped);
                advance();
            } else {
                sb.append(source.charAt(pos));
                advance();
            }
        }

        if (pos < source.length()) {
            advance(); // Skip closing quote
        }

        return new Token(Token.Type.STRING, sb.toString(), startLine, startCol);
    }

    private Token readNumber() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        boolean isDecimal = false;

        if (source.charAt(pos) == '-') {
            sb.append('-');
            advance();
        }

        while (pos < source.length() && (Character.isDigit(source.charAt(pos)) || source.charAt(pos) == '.')) {
            if (source.charAt(pos) == '.') {
                if (isDecimal) break; // Second dot, stop
                isDecimal = true;
            }
            sb.append(source.charAt(pos));
            advance();
        }

        return new Token(isDecimal ? Token.Type.DECIMAL : Token.Type.INTEGER, sb.toString(), startLine, startCol);
    }

    private Token readIdentifier() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();

        while (pos < source.length() && (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            sb.append(source.charAt(pos));
            advance();
        }

        String word = sb.toString().toLowerCase();
        Token.Type type = KEYWORDS.getOrDefault(word, Token.Type.IDENTIFIER);
        return new Token(type, sb.toString(), startLine, startCol);
    }

    private void skipLineComment() {
        while (pos < source.length() && source.charAt(pos) != '\n') {
            advance();
        }
    }

    private void skipBlockComment() {
        advance(); // Skip '/'
        advance(); // Skip '*'
        while (pos < source.length() - 1) {
            if (source.charAt(pos) == '*' && source.charAt(pos + 1) == '/') {
                advance();
                advance();
                break;
            }
            advance();
        }
    }

    private void advance() {
        if (pos < source.length()) {
            pos++;
            column++;
        }
    }

    private char peek() {
        if (pos + 1 < source.length()) {
            return source.charAt(pos + 1);
        }
        return '\0';
    }
}
