package ru.ac.uniyar.katkov.nonograms.heuristic;

import ru.ac.uniyar.katkov.nonograms.nonogram.Cell;
import ru.ac.uniyar.katkov.nonograms.nonogram.Group;
import ru.ac.uniyar.katkov.nonograms.nonogram.Line;
import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.util.ArrayList;
import java.util.List;

public class HeuristicLineInfo {
    private final Line line;
    private List<PartialGroup> partialGroups;


    public HeuristicLineInfo(Line line) {
        this.line = line;
        createPartialGroups();
        partialGroups.forEach(this::countOwnersOfPartialGroup);
        correctOwners();
    }

    private void correctOwners() {
        for (int i = 1; i < partialGroups.size(); ++i) {
            PartialGroup cur = partialGroups.get(i);
            PartialGroup prev = partialGroups.get(i - 1);
            while (cur.getOwners().first().getSequenceNumber() < prev.getOwners().first().getSequenceNumber()) {
                cur.getOwners().remove(cur.getOwners().first());
            }
            if (cur.getOwners().first() == prev.getOwners().first()) {
                PartialGroup united = cur.union(prev);
                countOwnersOfPartialGroup(united);
                if (!united.getOwners().contains(prev.getOwners().first()))
                    cur.getOwners().remove(cur.getOwners().first());
            }
        }
        for (int i = partialGroups.size() - 1; i > 0; --i) {
            PartialGroup cur = partialGroups.get(i);
            PartialGroup prev = partialGroups.get(i - 1);
            while (cur.getOwners().last().getSequenceNumber() < prev.getOwners().last().getSequenceNumber()) {
                prev.getOwners().remove(prev.getOwners().last());
            }
            if (cur.getOwners().last() == prev.getOwners().last()) {
                PartialGroup united = cur.union(prev);
                countOwnersOfPartialGroup(united);
                if (!united.getOwners().contains(cur.getOwners().last())) prev.getOwners().remove(prev.getOwners().last());
            }
        }
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
                partialGroups.add(new PartialGroup(start, i, cell.getColor()));
            } else ++i;
        }
    }

    public void countOwnersOfPartialGroup(PartialGroup partialGroup) {
        partialGroup.getOwners().clear();
        for (Group group : line.getGroups()) {
            if (group.getLowerBound() > partialGroup.getStart()) break;
            partialGroup.checkAndAddOwner(group);
        }
    }

    public List<PartialGroup> getPartialGroups() {
        return partialGroups;
    }
}
