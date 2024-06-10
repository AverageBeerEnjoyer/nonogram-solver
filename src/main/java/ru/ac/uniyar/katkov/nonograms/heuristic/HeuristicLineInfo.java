package ru.ac.uniyar.katkov.nonograms.heuristic;

import ru.ac.uniyar.katkov.nonograms.nonogram.Cell;
import ru.ac.uniyar.katkov.nonograms.nonogram.Group;
import ru.ac.uniyar.katkov.nonograms.nonogram.Line;
import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.util.ArrayList;
import java.util.List;

public class HeuristicLineInfo {
    private final Line line;
    private List<LineSegment> partialGroups;
    private List<LineSegment> emptySegments;


    public HeuristicLineInfo(Line line) {
        this.line = line;
        createPartialGroups();
        createEmptySegments();
        partialGroups.forEach(this::countOwnersOfPartialGroup);
        emptySegments.forEach(this::countGroupsCanBePlacedHere);
        correctOwners();
    }

    private void correctOwners() {
        for (int i = 0; i < partialGroups.size() - 1; ++i) {
            LineSegment cur = partialGroups.get(i);
            for (int j = i + 1; j < partialGroups.size(); ++j) {
                LineSegment next = partialGroups.get(j);
                while (cur.getOwners().first().getSequenceNumber() > next.getOwners().first().getSequenceNumber()) {
                    next.getOwners().remove(next.getOwners().first());
                }
                if (cur.getOwners().first() == next.getOwners().first()) {
                    LineSegment united = cur.union(next);
                    if (!united.canBeOwnedBy(next.getOwners().first())) {
                        next.getOwners().remove(next.getOwners().first());
                    }
                }
            }
        }
        for (int i = partialGroups.size() - 1; i > 0; --i) {
            LineSegment cur = partialGroups.get(i);
            for (int j = i - 1; j >= 0; --j) {
                LineSegment prev = partialGroups.get(j);
                while (cur.getOwners().last().getSequenceNumber() < prev.getOwners().last().getSequenceNumber()) {
                    prev.getOwners().remove(prev.getOwners().last());
                }
                if (cur.getOwners().last() == prev.getOwners().last()) {
                    LineSegment united = cur.union(prev);
                    if (!united.canBeOwnedBy(cur.getOwners().last()))
                        prev.getOwners().remove(prev.getOwners().last());
                }
            }
        }
    }

    private void createEmptySegments(){
        emptySegments = new ArrayList<>();
        int start = -1;
        boolean canStart = true;
        for (int i = 0; i < line.getCells().size(); ++i) {
            Cell cell = line.getCells().get(i);
            if (cell.isUndefined()) {
                if (start == -1 && canStart) {
                    start = i;
                    canStart = false;
                }
                continue;
            }
            if (cell.getColor() == ConsoleColor.NONE) {
                if (start != -1) {
                    emptySegments.add(new LineSegment(start, i, ConsoleColor.DEFAULT));
                    start = -1;
                    canStart = true;
                }
                continue;
            }
            start = -1;
            canStart = false;
        }
        if(start!=-1) emptySegments.add(new LineSegment(start, line.getLength(), ConsoleColor.DEFAULT));
    }

    private void createPartialGroups() {
        partialGroups = new ArrayList<>();
        int i = 0;
        while (i < line.getLength()) {
            Cell cell = line.getCells().get(i);

            if (!cell.isUndefined() && cell.getColor() != ConsoleColor.NONE) {
                int start = i;
                do ++i;
                while (i < line.getCells().size() && line.getCells().get(i).getColor() == cell.getColor());
                partialGroups.add(new LineSegment(start, i, cell.getColor()));
            } else ++i;
        }
    }

    public void countOwnersOfPartialGroup(LineSegment lineSegment) {
        lineSegment.getOwners().clear();
        for (Group group : line.getGroups()) {
            if (group.getLowerBound() > lineSegment.getStart()) break;
            lineSegment.checkAndAddOwner(group);
        }
    }

    public void countGroupsCanBePlacedHere(LineSegment emptySegment){
        for(Group group: line.getGroups()){
            if(group.getLowerBound()>=emptySegment.getEnd()) break;
            if(emptySegment.canContainGroup(group)) emptySegment.getOwners().add(group);
        }
    }

    public List<LineSegment> getEmptySegments() {
        return emptySegments;
    }

    public List<LineSegment> getPartialGroups() {
        return partialGroups;
    }
}
