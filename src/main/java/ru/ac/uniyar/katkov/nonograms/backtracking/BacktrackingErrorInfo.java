package ru.ac.uniyar.katkov.nonograms.backtracking;

import java.util.*;

public class BacktrackingErrorInfo {
    private final TreeSet<Integer> before = new TreeSet<>();
    private final TreeSet<Integer> on = new TreeSet<>();
    private final TreeSet<Integer> after = new TreeSet<>();
    private final TreeSet<Integer> changedColsInfo = new TreeSet<>();

    public void addBefore(int colNum) {
        before.add(colNum);
    }

    public void addOn(int colNum) {
        on.add(colNum);
    }

    public void addAfter(int colNum) {
        after.add(colNum);
    }


    public boolean hasBefore() {
        return !before.isEmpty();
    }

    public boolean hasOn(){
        return !on.isEmpty();
    }

    public boolean hasAfter(){
        return !after.isEmpty();
    }

    public TreeSet<Integer> getAfter() {
        return after;
    }

    public TreeSet<Integer> getBefore() {
        return before;
    }

    public TreeSet<Integer> getOn() {
        return on;
    }
    public boolean has(){
        return !(before.isEmpty() && on.isEmpty() && after.isEmpty());
    }
    public void addChangedCol(int n){
        changedColsInfo.add(n);
    }

    public TreeSet<Integer> getChangedColsInfo() {
        return changedColsInfo;
    }

    public void union(BacktrackingErrorInfo errorInfo){
        before.addAll(errorInfo.before);
        on.addAll(errorInfo.on);
        after.addAll(errorInfo.after);
        changedColsInfo.addAll(errorInfo.changedColsInfo);
    }
}
