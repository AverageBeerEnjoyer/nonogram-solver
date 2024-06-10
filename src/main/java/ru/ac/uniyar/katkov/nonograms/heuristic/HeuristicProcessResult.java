package ru.ac.uniyar.katkov.nonograms.heuristic;

import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.util.*;

public class HeuristicProcessResult {
    private boolean error = false;
    private boolean changed = false;
    private boolean rangeUpdated = false;
    private HashMap<Integer, ConsoleColor> changes = new HashMap<>();

    public HeuristicProcessResult(boolean error, boolean changed){
        this.changed = error;
        this.error = changed;
    }
    public HeuristicProcessResult(){}

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
    public void addChange(int change, ConsoleColor color){
        changes.put(change, color);
        changed = true;
    }
    public void setRangeUpdated(boolean rangeUpdated){
        this.rangeUpdated = rangeUpdated;
    }

    public boolean isRangeUpdated() {
        return rangeUpdated;
    }

    public void addChanges(Map<Integer, ConsoleColor> changes){
        this.changes.putAll(changes);
        if(!changes.isEmpty()) this.changed = true;
    }

    public HashMap<Integer, ConsoleColor> getChanges() {
        return changes;
    }

    public void union(HeuristicProcessResult errorInfo){
        changes.putAll(errorInfo.changes);
        changed = changed || errorInfo.changed;
        error = error || errorInfo.error;
        rangeUpdated = rangeUpdated || errorInfo.rangeUpdated;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isChanged() {
        return changed;
    }

    public boolean isError() {
        return error;
    }
}
