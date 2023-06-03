package com.lexer.swift_lexer;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer("resource/input.txt");
        lexer.getTokenSeq();
    }
}
