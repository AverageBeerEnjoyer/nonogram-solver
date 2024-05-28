package ru.ac.uniyar.katkov.nonograms.nonogram;

import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionService;

public class Cell {
    private final int x, y;
    private ConsoleColor color;
    private boolean undefined;
    private boolean fixed = false;
    private Set<ConsoleColor> rowColors;
    private Set<ConsoleColor> columnColors;

    public Cell(int x, int y) {
        color = ConsoleColor.DEFAULT;
        this.x = x;
        this.y = y;
        this.undefined=true;
        rowColors = new HashSet<>();
        columnColors = new HashSet<>();
    }

    @Override
    public String toString() {
        return color.apply("   ");
    }

    public boolean setColor(ConsoleColor color) {
        if(color!=ConsoleColor.NONE && !canBePaintedBy(color)) return false;
        if(fixed) return this.color == color;

        boolean res = undefined;
        this.color = color;
        undefined = false;
        return res;
    }

    public Set<ConsoleColor> getRowColors(){
        return rowColors;
    }

    public Set<ConsoleColor> getColumnColors() {
        return columnColors;
    }

    public void addRowColor(ConsoleColor color){
        rowColors.add(color);
    }
    public void addColumnColor(ConsoleColor color){
        columnColors.add(color);
    }

    public boolean canBePaintedBy(ConsoleColor color){
        return rowColors.contains(color) && columnColors.contains(color);
    }

    public void clear() {
        if(fixed) return;
        color = ConsoleColor.DEFAULT;
        undefined = true;
    }

    protected void reset(){
        fixed = false;
        clear();
        rowColors.clear();
        columnColors.clear();
    }

    public void fix(){
        fixed = true;
    }

    public ConsoleColor getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isUndefined() {
        return undefined;
    }
}
