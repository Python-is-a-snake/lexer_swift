package com.lexer.swift_lexer;

import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class Lexer {

    private final Swift swift;
    private final List<Token> tokens;
    private String buffer;
    private Queue<Character> input;

    public Lexer(String filename) {
        swift = new Swift("lang/keywords.txt", "lang/punctuation.txt",
                "lang/directives.txt");
        tokens = new ArrayList<>();

        String inputString = readFileContents(filename);
        input = new ArrayBlockingQueue<>(inputString.length());

        char[] input = inputString.toCharArray();
        for (char character : input) {
            if (character != 0) this.input.add(character);
        }

        tokenize();
    }


    private Token getDirToken() {
        while (!input.isEmpty()) {
            char character = input.peek();
            if (swift.isDirective(buffer)) break;
            updateLex(character);
        }

        return new Token(Type.DIRECTIVE, buffer);
    }

    private Token getNumToken() {
        while (!input.isEmpty()) {
            char character = input.peek();
            if ((character > '9'
                    || character < '0')
                    && character != '.'
                    && character != 'p' && character != 'P'
                    && character != 'e' && character != 'E'
                    && character != '+' && character != '-') break;
            updateLex(character);
        }

        if (getPrevToken().getType() == Type.IDENTIFIER
                || getPrevToken().getType() == Type.KEYWORD) {
            return new Token(Type.ERROR, buffer);
        }
        return new Token(Type.NUMBER, buffer);
    }

    private void updateLex(char character) {
        buffer = buffer.concat(String.valueOf(character));
        input.poll();
    }

    private Token getPrevToken() {
        return tokens.get(tokens.size() - 1);
    }

    private String readFileContents(String filename) {
        String result = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buff = new char[10];
            while (reader.read(buff) != -1) {
                stringBuilder.append(new String(buff));
                buff = new char[10];
            }
            result = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void tokenize() {
        while (!input.isEmpty()) {
            buffer = "";
            Token token = getToken();
            token.setValue(token.getValue().trim());
            tokens.add(token);
        }
    }

    void getTokenSeq() {
        for (Token token : tokens) {
            System.out.println();
            System.out.println("Type: " + token.getType() + " | Value: " + token.getValue());
        }
    }

    void printByType() {
        List<List<String>> valuesByType = new ArrayList<>();
        int typesCount = Type.values().length;
        for (int i = 0; i < typesCount; i++) {
            valuesByType.add(new ArrayList<>());
        }

        for (Token token : tokens) {
            String value = token.getValue();
            if (!(token.getType() == Type.COMMENT || token.getType() == Type.LITERAL))
                value = token.getValue().trim();
            if (!valuesByType.get(token.getType().ordinal()).contains(value))
                valuesByType.get(token.getType().ordinal()).add(value);
        }

        for (int i = 0; i < valuesByType.size(); i++) {
            System.out.println(Type.values()[i]);
            for (String lexeme : valuesByType.get(i)) {
                System.out.println(lexeme);
            }
            System.out.println();
        }
    }

    private Token getToken() {
        while (true) {
            char character = input.peek();
            updateLex(character);

            switch (character) {
                case '/' ->{
                    if (input.peek() == '/' || input.peek() == '*') return getCommentToken();
                }
                case '`' ->{return getIdToken(2);}
                case '$' -> {return getIdToken(4);}
                case '"' -> {
                    if (input.peek() == '"') return getStrMultLiteral();
                    else return getStrLitToken();}
                case '#' ->{return getDirToken();}
                case '.' ->{if (swift.isOperatorChar(input.peek())) return getOperToken(true);}
                case '-' ->{if (input.peek() >= '0' && input.peek() <= '9') return getNumToken();}
            }

            if (swift.isHeadIdentif(character)) {return getIdToken(1);}
            if (swift.isOperatorHead(character)) {return getOperToken(false);}
            if (swift.isPunctuationMark(String.valueOf(character))) {return getPunctToken(character);}
            if (character >= '0' && character <= '9') {return getNumToken();}
            if (input.peek() == null) {return new Token(Type.EOF, "eof");}
        }
    }

    private Token getCommentToken() {
        int state = 1;
        while (true) {
            int nextCommentState = state;
            char character = input.peek();

            switch (state) {
                default -> throw new RuntimeException();
                case 1 -> {
                    switch (character) {
                        case '/' -> nextCommentState = 2;
                        case '*' -> nextCommentState = 3;
                        default -> {
                            return new Token(Type.ERROR, buffer);
                        }
                    }
                }
                case 2 -> nextCommentState = swift.isNewLine(character) ? 5 : 2;
                case 3 -> nextCommentState = character == '*' ? 4 : 3;
                case 4 -> nextCommentState = character == '/' ? 5 : 3;
                case 5 -> {
                    return new Token(Type.COMMENT, buffer);
                }
            }
            updateLex(character);
            if (input.peek() == null) return new Token(Type.COMMENT, buffer);
            state = nextCommentState;
        }
    }

    private Token getIdToken(int state) {
        while (true) {
            int nextIdState = state;
            char character = input.peek();

            switch (state) {
                default -> throw new RuntimeException();
                case 1 -> nextIdState = swift.isCharIdentif(character) ? 1 : 5;
                case 2 -> {
                    if (swift.isHeadIdentif(character)) nextIdState = 3;
                    else return new Token(Type.ERROR, buffer);
                }
                case 3 -> {
                    if (swift.isCharIdentif(character)) nextIdState = 3;
                    else if (character == '`') nextIdState = 5;
                    else return new Token(Type.ERROR, buffer);
                }
                case 4 -> {
                    if (character >= '0' && character <= '9') nextIdState = 4;
                    else nextIdState = 5;
                }
                case 5 -> {
                    if (swift.isKeyword(buffer)) return new Token(Type.KEYWORD, buffer);
                    else return new Token(Type.IDENTIFIER, buffer);
                }
            }

            if (swift.isCharIdentif(character) || character == '`') updateLex(character);

            if (input.peek() == null) return new Token(Type.IDENTIFIER, buffer);

            state = nextIdState;
        }
    }

    private Token getPunctToken(char character) {
        if (character == '-' && (input.peek() == '>')) {
            updateLex(character);
            return new Token(Type.PUNCTUATION, buffer);
        }
        return new Token(Type.PUNCTUATION, buffer);
    }

    private Token getOperToken(boolean canContainDot) {
        while (!input.isEmpty()) {
            char character = input.peek();
            if (!(swift.isOperatorChar(character) || (character == '.' && canContainDot))) {
                break;
            }
            updateLex(character);
        }
        return new Token(Type.OPERATOR, buffer);
    }

    private Token getStrLitToken() {
        if (getPrevToken().getType() == Type.LITERAL
                || getPrevToken().getType() == Type.IDENTIFIER
                || getPrevToken().getType() == Type.KEYWORD) {
            return new Token(Type.ERROR, buffer);
        }
        char character = input.peek();
        while (!input.isEmpty()) {
            character = input.peek();
            if (character == '"') break;
            updateLex(character);
        }

        if (character == '"') updateLex(character);
        else return new Token(Type.ERROR, buffer);

        return new Token(Type.LITERAL, StringEscapeUtils.unescapeJava(
                buffer.replaceAll("\"", "")));
    }

    private Token getStrMultLiteral() {
        updateLex(input.peek());

        if (input.isEmpty() || input.peek() != '"') {
            return new Token(Type.LITERAL, StringEscapeUtils.unescapeJava(
                    buffer.replaceAll("\"", "")));
        }
        updateLex(input.peek());
        int state = 1;
        int nextState = state;
        while (true) {
            char character = input.peek();
            switch (state) {
                default -> throw new RuntimeException();
                case 1 -> nextState = character == '"' ? 2 : 1;
                case 2 -> nextState = character =='"' ? 3 : 1;
                case 3 -> nextState = character == '"' ? 4 : 1;
                case 4 ->{
                    return new Token(Type.LITERAL, StringEscapeUtils.unescapeJava(
                            buffer.replaceAll("\"\"\"", "")));
                }
            }
            updateLex(character);
            if (input.peek() == null) return new Token(Type.LITERAL,
                    StringEscapeUtils.unescapeJava(buffer.replaceAll("\"\"\"", "")));
            state = nextState;
        }
    }
}