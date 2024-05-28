package ru.ac.uniyar.katkov.nonograms.utils;

public class ConsoleColor {
    private static String reset = "\033[0m";
    private String color;
    private String letter;

    public ConsoleColor(String color, String letter) {
        this.color = color;
        this.letter = letter;
    }

    public String apply(String text) {
        return color + text + reset;
    }

    public static final ConsoleColor DEFAULT = new ConsoleColor(reset, "-"); // WHITE
    public static final ConsoleColor NONE = new ConsoleColor("\033[47m", "-"); // WHITE

    public String getLetter() {
        return letter;
    }

    @Override
    public String toString() {
        return letter;
    }
}