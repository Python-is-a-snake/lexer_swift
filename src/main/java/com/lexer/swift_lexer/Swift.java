package com.lexer.swift_lexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Swift {
    private List<String> keywords;
    private List<String> punctuations;
    private List<String> directives;
    public Swift(String keywordsFile, String punctuationFile, String dirFile) {
        keywords = new ArrayList<>();
        punctuations = new ArrayList<>();
        directives = new ArrayList<>();

        readLines(keywordsFile, keywords);
        readLines(punctuationFile, punctuations);
        readLines(dirFile, directives);
    }
    public static boolean isNewLine(char character) {
        return character == 10 || character == 13;
    }

    public boolean isKeyword(String lexeme) {
        return keywords.contains(lexeme.trim());
    }

    public boolean isHeadIdentif(char character) {
        return ((character >= 'a' && character <= 'z')
                || (character >= 'A' && character <= 'Z')
                || (character == '_'));
    }
    public  boolean isDirective(String lexeme) {
        return directives.contains(lexeme.trim());
    }

    public boolean isPunctuationMark(String lexeme) {
        return punctuations.contains(lexeme) || lexeme.equals("-");
    }

    public boolean isOperatorHead(char character) {
        char[] operatorHeads = {'/', '=', '-', '+', '!', '*', '%', '<', '>', '&', '|', '^', '~', '?'};
        for (char head : operatorHeads) {
            if (character == head)
                return true;
        }
        return false;
    }

    public boolean isCharIdentif(char character) {
        return (isHeadIdentif(character) || (character >= '0' && character <= '9'));
    }

    private void readLines(String filename, List<String> output) {
        try(Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNext()) {
                output.add(scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isOperatorChar(char character) {
        return (isOperatorHead(character)
                || (character >= 'Ĭ' && character <= 'ͯ')
                || (character >= '᷀' && character <= '᷿')
                || (character >= '⃐' && character <= '⃟'));
    }
}