package ru.ac.uniyar.katkov.nonograms.nonogram;

import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

public class Group {
    private final int length;
    private final ConsoleColor color;
    private int lowerBound, upperBound;
    private int curPlacement;
    private final int sequenceNumber;
    private boolean equalColorLeft = false;
    private boolean equalColorRight = false;


    public Group(int sequenceNumber, int length, ConsoleColor color) {
        this.sequenceNumber = sequenceNumber;
        this.length = length;
        this.color = color;
    }

    protected void setEqualColorLeft(boolean equalColorLeft){
        this.equalColorLeft = equalColorLeft;
    }

    protected void setEqualColorRight(boolean equalColorRight) {
        this.equalColorRight = equalColorRight;
    }

    public boolean isEqualColorLeft() {
        return equalColorLeft;
    }

    public boolean isEqualColorRight() {
        return equalColorRight;
    }

    public ConsoleColor getColor() {
        return color;
    }

    public int getLength() {
        return length;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public boolean setLowerBound(int lowerBound) {
        boolean res = this.lowerBound != lowerBound;
        this.lowerBound = lowerBound;
        return res;
    }

    public boolean setUpperBound(int upperBound) {
        boolean res = this.upperBound != upperBound;
        this.upperBound = upperBound;
        return res;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void clear(){
        curPlacement = -1;
    }

    public void setCurPlacement(int curPlacement) {
        this.curPlacement = curPlacement;
    }

    public int getCurPlacement() {
        return curPlacement;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sequenceNumber).append(". ").append("(").append(length).append(" ").append(color.getLetter()).append(")");
        sb.append("{").append(lowerBound).append(" - ").append(upperBound).append("}");
        return sb.toString();
    }
}
