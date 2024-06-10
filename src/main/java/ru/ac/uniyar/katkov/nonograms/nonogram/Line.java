package ru.ac.uniyar.katkov.nonograms.nonogram;

import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.util.*;

public class Line {
    private final int length;
    private final int num;
    private final ArrayList<Group> groups;
    private final int emptyNum;
    private final ArrayList<Cell> cells;
    private final boolean isRow;

    public Line(int length, int num, ArrayList<Group> groups, boolean isRow) {
        this.length = length;
        this.groups = groups;
        this.num = num;
        setGroupsColorIndicators();
        this.emptyNum = countAcceptableEmptyCells();
        countBounds();
        cells = new ArrayList<>(length);
        this.isRow = isRow;
    }

    private void setGroupsColorIndicators(){
        for(int i=0;i<groups.size()-1;++i){
            Group cur = groups.get(i);
            Group next = groups.get(i+1);
            if(cur.getColor() == next.getColor()){
                cur.setEqualColorRight(true);
                next.setEqualColorLeft(true);
            }
        }
    }

    public boolean isRow() {
        return isRow;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    private int countAcceptableEmptyCells() {
        int res = length;
        for (int i = 0; i < groups.size(); ++i) {
            res -= groups.get(i).getLength();
            try {
                if (groups.get(i).getColor() == groups.get(i - 1).getColor()) {
                    --res;
                }
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
        return res;
    }

    public int rightRangeBound(int groupNum) {
        if (groupNum < 0) return 0;
        if (groupNum > groups.size() - 1) return length;
        return groups.get(groupNum).getCurPlacement() + groups.get(groupNum).getLength();
    }

    public int getNum() {
        return num;
    }

    private void countBounds() {
        int cur = 0;
        for (int i = 0; i < groups.size(); ++i) {
            groups.get(i).setLowerBound(cur);
            groups.get(i).setUpperBound(cur + groups.get(i).getLength() + emptyNum);
            cur += groups.get(i).getLength();
            try {
                if (groups.get(i).getColor() == groups.get(i + 1).getColor()) {
                    ++cur;
                }
            } catch (IndexOutOfBoundsException ignored) {
            }
        }
    }

    public int getLength() {
        return length;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public int getEmptyNum() {
        return emptyNum;
    }

    public String withoutGroups() {
        StringBuilder sb = new StringBuilder();
        for (Cell cell : cells) {
            if (cell.isUndefined()) sb.append("_");
            else sb.append(cell.getColor());
        }
        return sb.toString();
    }

    protected void reset(){
        countBounds();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Group group : groups) {
            sb.append("(");
            sb.append(group.getLength());
            sb.append(" ");
            sb.append(group.getColor());
            sb.append(") ");
        }
        for (Cell cell : cells) {
            if (cell.isUndefined()) sb.append("_");
            else sb.append(cell.getColor());
        }
        return sb.toString();
    }

    public List<Integer> recountColors() {
        List<Integer> changes = new ArrayList<>();
        List<Set<ConsoleColor>> oldColors = new ArrayList<>();
        cells.forEach(cell -> {
            if (isRow) {
                oldColors.add(new HashSet<>(cell.getRowColors()));
                cell.getRowColors().clear();
            } else {
                oldColors.add(new HashSet<>(cell.getColumnColors()));
                cell.getColumnColors().clear();
            }
        });
        for (Group group : groups) {
            for (int pos = group.getLowerBound(); pos < group.getUpperBound(); ++pos) {
                Cell cell = cells.get(pos);
                if (isRow) cell.addRowColor(group.getColor());
                else cell.addColumnColor(group.getColor());
            }
        }
        for (int i = 0; i < cells.size(); ++i) {
            Set<ConsoleColor> currentColors = isRow ? cells.get(i).getRowColors() : cells.get(i).getColumnColors();
            if(!currentColors.equals(oldColors.get(i))) changes.add(i);
        }

        return changes;
    }
}
