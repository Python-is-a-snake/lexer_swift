package com.lexer.swift_lexer;

public class Token {

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }
    private final Type type;
    private String  value;

    public Type getType() {
        return type;
    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}