package ru.ac.uniyar.katkov.nonograms.heuristic;

import java.util.*;

public class HeuristicErrorInfo {
    private boolean error = false;
    private boolean changed = false;
    private Set<Integer> changes= new HashSet<>();

    public HeuristicErrorInfo(boolean error,boolean changed){
        this.changed = error;
        this.error = changed;
    }
    public HeuristicErrorInfo(){}

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
    public void addChange(int change){
        changes.add(change);
        changed = true;
    }
    public void addChanges(Collection<Integer> changes){
        this.changes.addAll(changes);
        if(!changes.isEmpty()) this.changed = true;
    }

    public Set<Integer> getChanges() {
        return changes;
    }

    public void union(HeuristicErrorInfo errorInfo){
        changes.addAll(errorInfo.changes);
        changed = changed || errorInfo.changed;
        error = error || errorInfo.error;
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
