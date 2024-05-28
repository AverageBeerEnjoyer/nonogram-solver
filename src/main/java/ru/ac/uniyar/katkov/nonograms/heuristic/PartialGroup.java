package ru.ac.uniyar.katkov.nonograms.heuristic;

import ru.ac.uniyar.katkov.nonograms.nonogram.Group;
import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import javax.swing.plaf.basic.BasicGraphicsUtils;
import java.util.*;

public class PartialGroup {
    private int start;
    private int end;
    private int length;
    private ConsoleColor color;
    private TreeSet<Group> owners;
    private boolean completed;

    public PartialGroup(int start, int end, ConsoleColor color) {
        this.start = start;
        this.end = end;
        this.length = end - start;
        this.color = color;
        this.owners = new TreeSet<>(Comparator.comparingInt(Group::getSequenceNumber));
    }

    public void checkAndAddOwner(Group owner) {
        if (canBeOwnedBy(owner))
            this.owners.add(owner);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setRange(int start, int end) {
        this.start = start;
        this.end = end;
        this.length = end - start;
    }

    public int getLength() {
        return length;
    }

    public int getMinLength() {
        Optional<Group> opt = owners.stream().min(Comparator.comparingInt(Group::getLength));
        if (opt.isEmpty()) throw new RuntimeException("0 owners");
        return opt.get().getLength();
    }

    private boolean canBeOwnedBy(Group group) {
        return isInRange(group) && end - start <= group.getLength() && color == group.getColor();
    }

    public boolean isInRange(Group group){
        return start >= group.getLowerBound() && end <= group.getUpperBound();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(start).append(" - ").append(end).append(", ").append(color.getLetter()).append("}");
        return sb.toString();
    }

    public ConsoleColor getColor() {
        return color;
    }

    public TreeSet<Group> getOwners() {
        return owners;
    }

    public PartialGroup union(PartialGroup partialGroup) {
        if (color != partialGroup.color) throw new IllegalArgumentException("groups have different colors");
        return new PartialGroup(Math.min(start, partialGroup.start), Math.max(end, partialGroup.end), color);
    }
}
