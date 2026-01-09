package com.mobileforge.compiler;

public class Token {
    public enum Type {
        // Actions
        BUILD, CREATE, PLACE, ADD, MAKE, CHANGE, SET, UPDATE, REMOVE,
        WHEN, IF, ELSE, REPEAT, WHILE, FOR, EACH, IN, FROM, TO,
        IMPORT, USE, CONNECT, LOAD, TRY, ON, END,
        CALL, WITH, RETURN, GO, BACK,

        // Objects
        APP, SCREEN, BUTTON, TEXT, IMAGE, HEADER, FOOTER, INPUT, LIST, MENU,
        FUNCTION, FEATURE, CLASS, VARIABLE, FILE, ACTIVITY,
        PAGE, SECTION, CONTAINER, LAYOUT, COMPONENT, SERVICE, DATABASE, SETTING,
        POPUP, FIELD, BLOCK, ITEM, NUMBER,

        // Properties & Modifiers
        LARGE, SMALL, MEDIUM, TINY, HUGE, BOLD, ITALIC, UNDERLINED,
        COLOR, BACKGROUND, WIDTH, HEIGHT, SIZE,
        TOP, BOTTOM, LEFT, RIGHT, CENTER, ABOVE, BELOW, BESIDE, AT,
        VISIBLE, HIDDEN, ENABLED, DISABLED, ACTIVE, INACTIVE,
        SCROLLABLE, CLICKABLE,

        // Logical
        AND, OR, NOT, IS, EQUALS, GREATER, LESS, THAN,

        // Values
        TRUE, FALSE, YES, NO, ON, OFF,

        // Literals
        STRING, INTEGER, DECIMAL, IDENTIFIER,

        // Operators
        PLUS, MINUS, TIMES, DIVIDED, BY,

        // Structure
        NEWLINE, INDENT, DEDENT, EOF,

        // Special
        LPAREN, RPAREN, COMMA, DOT, COLON,

        // Events
        CLICKED, LOADED, CREATED, ERROR,

        // Requests
        REQUEST, PERMISSION, CAMERA, LOCATION, STORAGE, NOTIFICATIONS,
        INTERNET, ACCESS,

        // Actions on data
        SHOW, HIDE, SAVE, PARSE, CHECK, HIGHLIGHT, SUGGEST, SEARCH,
        SUMMARIZE, DISPLAY, PRINT, MARK, INCREASE, DECREASE,
        OPEN, SHARE, WELCOME,

        // Prepositions/Connectors
        OF, THE, A, AN, AS, INTO, ONTO, WITHIN,

        // Android specific
        ANDROID, RESOURCE, EXTERNAL, PERMISSION_KEYWORD,

        // Conditionals
        EMPTY, COMPLETE, RUNNING, LOADING,

        // Special keywords
        TIMES_KEYWORD, MESSAGE, HINT, PARAGRAPH, HEADING, PRICE,
        CONTENT, CONTEXT, DEFAULT, CURRENT, ABOUT,

        UNKNOWN
    }

    public Type type;
    public String value;
    public int line;
    public int column;

    public Token(Type type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("Token(%s, '%s', %d:%d)", type, value, line, column);
    }
}
