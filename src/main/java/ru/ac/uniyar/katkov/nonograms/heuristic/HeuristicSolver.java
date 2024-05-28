package ru.ac.uniyar.katkov.nonograms.heuristic;

import ru.ac.uniyar.katkov.nonograms.nonogram.Cell;
import ru.ac.uniyar.katkov.nonograms.nonogram.Group;
import ru.ac.uniyar.katkov.nonograms.nonogram.Line;
import ru.ac.uniyar.katkov.nonograms.nonogram.Nonogram;
import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.util.*;

public class HeuristicSolver {
    private Nonogram nng;
    private Set<Integer> colChanges, rowChanges, colSelfChanges, rowSelfChanges;
    private int iteration = 0;
    private int subIteration = 0;
    private HeuristicLineInfo currentLineInfo;
    private boolean debug = false;

    public void solve(Nonogram nng) {
        try {
            this.nng = nng;
            colChanges = new HashSet<>();
            for (int i = 0; i < nng.getCols().size(); ++i) colChanges.add(i);
            rowChanges = new HashSet<>();
            for (int i = 0; i < nng.getRows().size(); ++i) rowChanges.add(i);
            colSelfChanges = new HashSet<>();
            rowSelfChanges = new HashSet<>();
            runSolution();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("iteration: " + iteration + "." + subIteration);
        }

    }

    private void runSolution() {
        boolean hasChanges = true;
        boolean firstIteration = true;
        if(debug) System.out.println(nng.getMetaInfo());
        while (hasChanges) {
            subIteration = 1000;
            hasChanges = false;
            if (debug) {
                ++iteration;
                System.out.println("rows");
            }

            if (!firstIteration) colChanges.clear();
            rowChanges.addAll(rowSelfChanges);
            rowSelfChanges.clear();

            for (int i : rowChanges) {
                Line row = nng.getRows().get(i);
                if (debug) {
                    ++subIteration;
                    System.out.println(iteration + "." + subIteration);
                }
                HeuristicErrorInfo errorInfo;

                errorInfo = processLine(row);
                if (errorInfo.isError())
                    throw new RuntimeException("an error was encountered at iteration " + iteration);
                if (!firstIteration) colChanges.addAll(errorInfo.getChanges());
                if (errorInfo.isChanged()) {
                    hasChanges = true;
                    rowSelfChanges.add(row.getNum());
                    colChanges.addAll(errorInfo.getChanges());
                }
            }
            rowChanges.clear();
            colChanges.addAll(colSelfChanges);
            colSelfChanges.clear();
            subIteration = 2000;
            if (debug) System.out.println("columns");
            for (int i : colChanges) {
                Line col = nng.getCols().get(i);
                if (debug) {
                    ++subIteration;
                    System.out.println(iteration + "." + subIteration);
                }

                HeuristicErrorInfo errorInfo;
                errorInfo = processLine(col);
                if (errorInfo.isError())
                    throw new RuntimeException("an error was encountered at iteration " + iteration);
                rowChanges.addAll(errorInfo.getChanges());
                if (errorInfo.isChanged()) {
                    rowChanges.addAll(errorInfo.getChanges());
                    colSelfChanges.add(col.getNum());
                    hasChanges = true;
                }
            }
            firstIteration = false;
        }
    }

    private HeuristicErrorInfo processLine(Line line) {
        if(Thread.currentThread().isInterrupted()) throw new RuntimeException("interrupted");
        if (debug) System.out.println(line);
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo(), last;

        currentLineInfo = new HeuristicLineInfo(line);

        last = rule3_1(line);
        errorInfo.union(last);

        last = rule2_2(line);
        errorInfo.union(last);

        last = rule2_3(line);
        errorInfo.union(last);

        last = rule2_1(line);
        errorInfo.union(last);

        last = rule1_2(line);
        errorInfo.union(last);

        last = rule1_1(line);
        errorInfo.union(last);

        last = rule1_3(line);
        errorInfo.union(last);

        last.addChanges(line.recountColors());

        if (debug) System.out.println(line);
        return errorInfo;
    }


    /**
     * классическое пересечение
     */
    private HeuristicErrorInfo rule1_1(Line line) {
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo();
        for (int i = 0; i < line.getGroups().size(); ++i) {
            Group group = line.getGroups().get(i);
            for (int j = group.getUpperBound() - group.getLength(); j < group.getLowerBound() + group.getLength(); ++j) {
                if (line.getCells().get(j).setColor(group.getColor())) errorInfo.addChange(j);
            }
        }
        return errorInfo;
    }

    /**
     * если ячейка не в ренже хотя бы одной группы, она отмечается пустой
     */
    private HeuristicErrorInfo rule1_2(Line line) {
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo();
        int l, r;
        l = 0;
        for (int i = 0; i < line.getGroups().size() + 1; ++i) {
            if (i != line.getGroups().size()) r = line.getGroups().get(i).getLowerBound();
            else r = line.getLength();
            for (int j = l; j < r; ++j) {
                if (line.getCells().get(j).setColor(ConsoleColor.NONE)) errorInfo.addChange(j);
            }
            if (i != line.getGroups().size()) l = line.getGroups().get(i).getUpperBound();
        }
        return errorInfo;
    }

    /**
     * обновляет ренжи всех групп, чтобы текущая группа влезала от своего начала до начала следующей
     */
    private HeuristicErrorInfo rule2_1(Line line) {
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo();
        int l, r, len;
        int size = line.getGroups().size();
        for (int i = 0; i < size; ++i) {
            Group curGroup = line.getGroups().get(i);
            l = curGroup.getLowerBound();
            if (i != size - 1) r = line.getGroups().get(i + 1).getLowerBound();
            else r = line.getLength();

            len = curGroup.getLength();
            if (line.hasSimilarColorWithNextGroup(i)) len += 1;

            if (r - l < len) {
                if (i == size - 1) {
                    errorInfo.setError(true);
                    return errorInfo;
                }
                line.getGroups().get(i + 1).setLowerBound(l + len);
                errorInfo.setChanged(true);
            }
        }
        for (int i = 0; i < size; ++i) {
            Group curGroup = line.getGroups().get(size - i - 1);
            r = curGroup.getUpperBound();
            if (i != size - 1) l = line.getGroups().get(size - i - 2).getUpperBound();
            else l = 0;

            len = curGroup.getLength();
            if (line.hasSimilarColorWithPrevGroup(size - i - 1)) len += 1;

            if (r - l < len) {
                if (i == size - 1) {
                    errorInfo.setError(true);
                    return errorInfo;
                }
                line.getGroups().get(size - i - 2).setUpperBound(r - len);
                errorInfo.setChanged(true);
            }
        }
        return errorInfo;
    }

    /**
     * если у группы только один потенциальный владелец, обновляет его ренж
     */
    private HeuristicErrorInfo rule2_2(Line line) {
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo();
        for (PartialGroup partialGroup : currentLineInfo.getPartialGroups()) {
            TreeSet<Group> owners = partialGroup.getOwners();
            if (owners.isEmpty()) throw new RuntimeException("error (0 owners " + partialGroup + ") at line \n" + line);
            if (owners.size() == 1) {
                HeuristicErrorInfo errorInfo1 = updateRange(owners.first(), partialGroup);
                if (errorInfo1.isChanged()) errorInfo.setChanged(true);
            }
        }
        return errorInfo;
    }

    /**
     * дополняет частичную группу до минимальной длины потенциальных владельцев, отталкиваясь от краев,
     * пустых клеток и соседних частичных групп, если текущая частичная группа не может быть соседней одним целым
     */
    private HeuristicErrorInfo rule3_1(Line line) {
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo();
        for (int groupNum = 0; groupNum < currentLineInfo.getPartialGroups().size(); ++groupNum) {
            PartialGroup partialGroup = currentLineInfo.getPartialGroups().get(groupNum);
            int minLength = partialGroup.getMinLength();
            int checkRange = minLength - partialGroup.getLength();

            int fillFrom = partialGroup.getStart();
            int fillTo = partialGroup.getEnd();

            for (int i = 0; i < checkRange + 1; ++i) {
                int pos = partialGroup.getStart() - i - 1;
                if (pos < 0) {
                    fillTo = minLength;
                    break;
                }

                Cell cell = line.getCells().get(pos);
                if (!cell.isUndefined()) {
                    if (cell.getColor() == partialGroup.getColor()) {
                        PartialGroup united = partialGroup.union(currentLineInfo.getPartialGroups().get(groupNum - 1));
                        currentLineInfo.countOwnersOfPartialGroup(united);
                        if (united.getOwners().isEmpty()) fillTo = pos + 2 + minLength;
                        else continue;
                    } else fillTo = pos + 1 + minLength;
                    break;
                }
            }

            for (int i = 0; i < checkRange + 1; ++i) {
                int pos = partialGroup.getEnd() + i;
                if (pos >= line.getLength()) {
                    fillFrom = line.getLength() - minLength;
                    break;
                }

                Cell cell = line.getCells().get(pos);
                if (!cell.isUndefined()) {
                    if (cell.getColor() == partialGroup.getColor()) {
                        PartialGroup united = partialGroup.union(currentLineInfo.getPartialGroups().get(groupNum + 1));
                        currentLineInfo.countOwnersOfPartialGroup(united);
                        if (united.getOwners().isEmpty()) fillFrom = pos - 1 - minLength;
                        else continue;
                    } else fillFrom = pos - minLength;
                    break;
                }
            }
            boolean changed = false;
            for (int i = fillFrom; i < fillTo; ++i) {
                if (line.getCells().get(i).setColor(partialGroup.getColor())) {
                    changed = true;
                    errorInfo.addChange(i);
                }
            }
            if (changed) {
                currentLineInfo = new HeuristicLineInfo(line);
                for (int i = 0; i < currentLineInfo.getPartialGroups().size(); ++i) {
                    if (currentLineInfo.getPartialGroups().get(i).getStart() == fillFrom) {
                        groupNum = i;
                        break;
                    }
                }
            }
        }
        return errorInfo;
    }

    /**
     * обновляет ренж, основываясь на разделении ренжа на сегмненты
     * в качестве разделителей использует пустые клетки, клетки другого цвета и клетки,
     * в которых цвет текущей группы не может быть поставлен
     */
    private HeuristicErrorInfo rule2_3(Line line) {
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo();

        for (Group group : line.getGroups()) {
            List<PartialGroup> segments = new ArrayList<>();
            int start = group.getLowerBound();
            boolean segmentFlag = false;
            for (int i = group.getLowerBound(); i < group.getUpperBound(); ++i) {
                Cell cell = line.getCells().get(i);
                if (cell.getColor() == group.getColor() || cell.isUndefined() && cell.canBePaintedBy(group.getColor())) {
                    if (!segmentFlag) {
                        start = i;
                        segmentFlag = true;
                    }
                } else if (segmentFlag) {
                    segments.add(new PartialGroup(start, i, group.getColor()));
                    segmentFlag = false;
                }
            }
            if (segmentFlag) {
                segments.add(new PartialGroup(start, group.getUpperBound(), group.getColor()));
            }

            segments.removeIf(seg -> seg.getLength() < group.getLength());
            if (segments.isEmpty()) throw new RuntimeException("0 segments");
            int lb = segments.get(0).getStart();
            int ub = segments.get(segments.size() - 1).getEnd();
            if (lb != group.getLowerBound() || ub != group.getUpperBound()) errorInfo.setChanged(true);
            group.setLowerBound(lb);
            group.setUpperBound(ub);
        }
        return errorInfo;
    }

    private HeuristicErrorInfo updateRange(Group group, PartialGroup partialGroup) {
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo();

        if (group.setUpperBound(Math.min(group.getUpperBound(), partialGroup.getStart() + group.getLength())))
            errorInfo.setChanged(true);
        if (group.setLowerBound(Math.max(group.getLowerBound(), partialGroup.getEnd() - group.getLength())))
            errorInfo.setChanged(true);
        return errorInfo;
    }

    /**
     * если крайняя клетка ренжа группы закрашена тем же цветом, что и группа
     * тогда если все потенциальные владельцы с того же конца, что и закрашенная клетка(т.е. справа или слева)
     * имеют группу того же цвета, или являются крайними, то помечает сесоднюю клетку за ренжом текущей группы пустой
     */
    private HeuristicErrorInfo rule1_3(Line line) {
        HeuristicErrorInfo errorInfo = rule1_3Loop(line, true);
        errorInfo.union(rule1_3Loop(line, false));
        return errorInfo;
    }

    private HeuristicErrorInfo rule1_3Loop(Line line, boolean leftDirection) {
        HeuristicErrorInfo errorInfo = new HeuristicErrorInfo();
        for (PartialGroup partialGroup : currentLineInfo.getPartialGroups()) {
            Group rootGroup = rule1_3GetRootGroup(partialGroup, leftDirection);
            if (rootGroup == null) break;

            boolean passed = true;
            for (Group owner : partialGroup.getOwners()) {
                if (leftDirection && owner.getSequenceNumber() > rootGroup.getSequenceNumber()) break;
                if (!leftDirection && owner.getSequenceNumber() < rootGroup.getSequenceNumber()) continue;
                if (leftDirection && owner.getSequenceNumber() > 0 && !line.hasSimilarColorWithPrevGroup(owner.getSequenceNumber())) {
                    passed = false;
                    break;
                }
                if (!leftDirection && owner.getSequenceNumber() < line.getGroups().size() - 1 && !line.hasSimilarColorWithNextGroup(owner.getSequenceNumber())) {
                    passed = false;
                    break;
                }
                if (owner != rootGroup && owner.getLength() != partialGroup.getLength()) {
                    passed = false;
                    break;
                }
            }
            if (passed) {
                int position;
                if (leftDirection) position = rootGroup.getLowerBound() - 1;
                else position = rootGroup.getUpperBound();
                if (position >= 0 && position < line.getLength()) {
                    if (line.getCells().get(position).setColor(ConsoleColor.NONE))
                        errorInfo.addChange(position);
                }
            }
        }
        return errorInfo;
    }

    private Group rule1_3GetRootGroup(PartialGroup partialGroup, boolean leftDirection) {
        for (Group group : partialGroup.getOwners()) {
            if (leftDirection && group.getLowerBound() == partialGroup.getStart()) return group;
            if (!leftDirection && group.getUpperBound() == partialGroup.getEnd()) return group;
        }
        return null;
    }
}
