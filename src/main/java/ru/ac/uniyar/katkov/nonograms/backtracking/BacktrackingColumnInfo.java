package ru.ac.uniyar.katkov.nonograms.backtracking;

import ru.ac.uniyar.katkov.nonograms.nonogram.Cell;
import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;
import ru.ac.uniyar.katkov.nonograms.nonogram.Line;

public class BacktrackingColumnInfo {

    private final Line col;

    private int curGroupNum;
    private int curGroupSize;
    private int curEmptyCellsNum;

    public BacktrackingColumnInfo(Line col) {
        this.col = col;
        this.curGroupNum = 0;
        this.curGroupSize = 0;
        this.curEmptyCellsNum = 0;
    }

    public int getCurEmptyCellsNum() {
        return curEmptyCellsNum;
    }

    public int getCurGroupNum() {
        return curGroupNum;
    }

    public int getCurGroupSize() {
        return curGroupSize;
    }

    public Line getCol() {
        return col;
    }

    private void increaseGroupSize() {
        ++curGroupSize;
    }

    private void decreaseGroupSize() {
        --curGroupSize;
    }

    private void increaseGroupNum() {
        ++curGroupNum;
    }

    private void setCurGroupSize(int curGroupSize) {
        this.curGroupSize = curGroupSize;
    }

    private void decreaseGroupNum() {
        --curGroupNum;
    }

    private void increaseEmptyCellsNum() {
        ++curEmptyCellsNum;
    }

    private void decreaseEmptyCellsNum() {
        --curEmptyCellsNum;
    }


    private boolean isThereGroups() {
        return curGroupNum < col.getGroups().size();
    }

    boolean isCorrect(int lastFilled) {
        ConsoleColor color = col.getCells().get(lastFilled).getColor();
        if (color == ConsoleColor.NONE) {
            if (!isThereGroups()) {
                increaseEmptyCellsNum();
                return true;
            }
            int groupSize = col.getGroups().get(curGroupNum).getLength();
            if (curGroupSize == 0) {
                if (curEmptyCellsNum >= col.getEmptyNum()) return false;
                increaseEmptyCellsNum();
                return true;
            }
            if (curGroupSize != groupSize) return false;
            if (!col.hasSimilarColorWithNextGroup(curGroupNum)) {
                if (curEmptyCellsNum >= col.getEmptyNum()) return false;
                increaseEmptyCellsNum();
            }
            increaseGroupNum();
            setCurGroupSize(0);
            return true;
        }
        if (!isThereGroups()) return false;
        if (color == col.getGroups().get(curGroupNum).getColor()) {
            if (curGroupSize >= col.getGroups().get(curGroupNum).getLength()) return false;
            increaseGroupSize();
            return true;
        }
        if (curGroupSize != col.getGroups().get(curGroupNum).getLength()) return false;
        if (curGroupNum + 1 >= col.getGroups().size()) return false;
        if (color != col.getGroups().get(curGroupNum + 1).getColor()) return false;
        increaseGroupNum();
        setCurGroupSize(1);
        return true;
    }

    private void print() {
        if(col.getNum() != 0) return;
        System.out.println(curGroupNum + " " + curGroupSize + " " + curEmptyCellsNum);
    }

    public void turnBackColInfo(int row) {
        Cell cell = col.getCells().get(row);
        if (cell.isUndefined()) return;
        if (cell.getColor() == ConsoleColor.NONE) {
            if (row == 0 || col.getCells().get(row - 1).getColor() == ConsoleColor.NONE) {
                decreaseEmptyCellsNum();
                return;
            }
            if (!col.hasSimilarColorWithNextGroup(curGroupNum - 1)) {
                decreaseEmptyCellsNum();
            }
            decreaseGroupNum();

            setCurGroupSize(col.getGroups().get(curGroupNum).getLength());
            return;
        }
        if (row != 0 && col.getCells().get(row - 1).getColor() != ConsoleColor.NONE && col.getCells().get(row - 1).getColor() != cell.getColor()) {
            decreaseGroupNum();
            setCurGroupSize(col.getGroups().get(curGroupNum).getLength());
            return;
        }
        decreaseGroupSize();
    }
}
