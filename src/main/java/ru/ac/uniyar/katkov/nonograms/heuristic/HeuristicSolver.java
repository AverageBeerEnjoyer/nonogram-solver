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
    private boolean debug = true;

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
        if (debug) System.out.println(nng.getMetaInfo());
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
                    subIteration = 1000 + row.getNum();
                    System.out.println(iteration + "." + subIteration);
                }

                HeuristicProcessResult errorInfo;
                errorInfo = processLine(row);
                if (errorInfo.isError())
                    throw new RuntimeException("an error was encountered at iteration " + iteration);
                if (applyChanges(row, errorInfo)) hasChanges = true;
            }
            rowChanges.clear();
            colChanges.addAll(colSelfChanges);
            colSelfChanges.clear();
            subIteration = 2000;
            if (debug) System.out.println("columns");
            for (int i : colChanges) {
                Line col = nng.getCols().get(i);
                if (debug) {
                    subIteration = 2000 + col.getNum();
                    System.out.println(iteration + "." + subIteration);
                }

                HeuristicProcessResult errorInfo;
                errorInfo = processLine(col);
                if (errorInfo.isError())
                    throw new RuntimeException("an error was encountered at iteration " + iteration);
                if (applyChanges(col, errorInfo)) hasChanges = true;
            }
            firstIteration = false;
        }
    }

    private HeuristicProcessResult processLine(Line line) {
        if (Thread.currentThread().isInterrupted()) throw new RuntimeException("interrupted");
        if (debug) System.out.println(line);
        HeuristicProcessResult errorInfo = new HeuristicProcessResult(), last;

        currentLineInfo = new HeuristicLineInfo(line);

        last = rule3_1(line);
        errorInfo.union(last);

        last = rule2_2(line);
        errorInfo.union(last);

        last = rule2_3(line);
        errorInfo.union(last);

        last = rule2_4(line);
        errorInfo.union(last);

        last = rule2_1(line);
        errorInfo.union(last);

        last = rule1_2(line);
        errorInfo.union(last);

        last = rule1_4(line);
        errorInfo.union(last);
//
        last = rule1_5(line);
        errorInfo.union(last);

        last = rule1_1(line);
        errorInfo.union(last);

        last = rule1_3(line);
        errorInfo.union(last);

        last = rule1_6(line);
        errorInfo.union(last);

        return errorInfo;
    }

    private boolean applyChanges(Line line, HeuristicProcessResult processResult) {
        HeuristicProcessResult result = new HeuristicProcessResult();

        List<Integer> colorChanges = line.recountColors();
        if (line.isRow()) colChanges.addAll(colorChanges);
        else rowChanges.addAll(colorChanges);

        for (HashMap.Entry<Integer, ConsoleColor> change : processResult.getChanges().entrySet()) {
            if (line.getCells().get(change.getKey()).setColor(change.getValue())) {
                if (line.isRow()) colChanges.add(change.getKey());
                else rowChanges.add(change.getKey());
                result.addChange(change.getKey(), change.getValue());
            }
        }
        if (result.isChanged() || processResult.isRangeUpdated() || !colorChanges.isEmpty()) {
            if (line.isRow()) rowSelfChanges.add(line.getNum());
            else colSelfChanges.add(line.getNum());
        }
        if (debug) System.out.println(line);
        return !colorChanges.isEmpty() || result.isChanged() || processResult.isRangeUpdated();
    }


    /**
     * классическое пересечение
     */
    private HeuristicProcessResult rule1_1(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
        for (int i = 0; i < line.getGroups().size(); ++i) {
            Group group = line.getGroups().get(i);
            for (int j = group.getUpperBound() - group.getLength(); j < group.getLowerBound() + group.getLength(); ++j) {
                errorInfo.addChange(j, group.getColor());
            }
        }
        return errorInfo;
    }

    /**
     * если ячейка не в ренже хотя бы одной группы, она отмечается пустой
     */
    private HeuristicProcessResult rule1_2(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
        int l, r;
        l = 0;
        for (int i = 0; i < line.getGroups().size() + 1; ++i) {
            if (i != line.getGroups().size()) r = line.getGroups().get(i).getLowerBound();
            else r = line.getLength();
            for (int j = l; j < r; ++j) {
                errorInfo.addChange(j, ConsoleColor.NONE);
            }
            if (i != line.getGroups().size()) l = line.getGroups().get(i).getUpperBound();
        }
        return errorInfo;
    }

    /**
     * если крайняя клетка ренжа группы закрашена тем же цветом, что и группа
     * тогда если все потенциальные владельцы с того же конца, что и закрашенная клетка(т.е. справа или слева)
     * имеют группу того же цвета, или являются крайними, то помечает сесоднюю клетку за ренжом текущей группы пустой
     */
    private HeuristicProcessResult rule1_3(Line line) {
        HeuristicProcessResult errorInfo = rule1_3Loop(line, true);
        errorInfo.union(rule1_3Loop(line, false));
        return errorInfo;
    }

    private HeuristicProcessResult rule1_3Loop(Line line, boolean leftDirection) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
        for (LineSegment lineSegment : currentLineInfo.getPartialGroups()) {
            Group rootGroup = rule1_3GetRootGroup(lineSegment, leftDirection);
            if (rootGroup == null) break;

            boolean passed = true;
            for (Group owner : lineSegment.getOwners()) {
                if (leftDirection && owner.getSequenceNumber() > rootGroup.getSequenceNumber()) break;
                if (!leftDirection && owner.getSequenceNumber() < rootGroup.getSequenceNumber()) continue;
                if (leftDirection && owner.getSequenceNumber() > 0 && !owner.isEqualColorLeft()) {
                    passed = false;
                    break;
                }
                if (!leftDirection && owner.getSequenceNumber() < line.getGroups().size() - 1 && !owner.isEqualColorRight()) {
                    passed = false;
                    break;
                }
                if (owner != rootGroup && owner.getLength() != lineSegment.getLength()) {
                    passed = false;
                    break;
                }
            }
            if (passed) {
                int position;
                if (leftDirection) position = rootGroup.getLowerBound() - 1;
                else position = rootGroup.getUpperBound();
                if (position >= 0 && position < line.getLength()) {
                    errorInfo.addChange(position, ConsoleColor.NONE);
                }
            }
        }
        return errorInfo;
    }

    private Group rule1_3GetRootGroup(LineSegment lineSegment, boolean leftDirection) {
        for (Group group : lineSegment.getOwners()) {
            if (leftDirection && group.getLowerBound() == lineSegment.getStart()) return group;
            if (!leftDirection && group.getUpperBound() == lineSegment.getEnd()) return group;
        }
        return null;
    }

    private HeuristicProcessResult rule1_4(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
        for (LineSegment lineSegment : currentLineInfo.getPartialGroups()) {
            boolean equalSizes = true;
            for (Group owner : lineSegment.getOwners()) {
                if (lineSegment.getLength() != owner.getLength()) {
                    equalSizes = false;
                    break;
                }
            }
            if (!equalSizes) continue;
            boolean equalColorsLeft = true;
            boolean equalColorsRight = true;
            for (Group owner : lineSegment.getOwners()) {
                if (owner.getSequenceNumber() != 0 && !owner.isEqualColorLeft()) {
                    equalColorsLeft = false;
                }
                if (owner.getSequenceNumber() != line.getGroups().size() - 1 && !owner.isEqualColorRight()) {
                    equalColorsRight = false;
                }
            }
            if (equalColorsLeft) {
                int pos = lineSegment.getStart() - 1;
                if (pos >= 0) {
                    errorInfo.addChange(pos, ConsoleColor.NONE);
                }
            }
            if (equalColorsRight) {
                int pos = lineSegment.getEnd();
                if (pos < line.getLength()) {
                    errorInfo.addChange(pos, ConsoleColor.NONE);
                }
            }
        }
        return errorInfo;
    }

    private HeuristicProcessResult rule1_5(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
//        System.out.println("!" + line);
        for (int i = 0; i < currentLineInfo.getPartialGroups().size() - 1; ++i) {
            LineSegment cur = currentLineInfo.getPartialGroups().get(i);
            LineSegment next = currentLineInfo.getPartialGroups().get(i + 1);
            if (cur.getColor() == next.getColor() && cur.getEnd() == next.getStart() - 1 && line.getCells().get(cur.getEnd()).isUndefined()) {
                LineSegment united = cur.union(next);
                currentLineInfo.countOwnersOfPartialGroup(united);
                if (!united.getOwners().isEmpty()) continue;
                boolean pass = true;
                for (ConsoleColor color : nng.getColors()) {
                    if (cur.getColor() == color) continue;
                    LineSegment toCheck = new LineSegment(cur.getEnd(), next.getStart(), color);
                    currentLineInfo.countOwnersOfPartialGroup(toCheck);
                    toCheck.getOwners().removeIf(g -> g.getLength() > 1);
                    if (!toCheck.getOwners().isEmpty()) {
                        pass = false;
                        break;
                    }
                }

                if (!pass) continue;
                errorInfo.addChange(cur.getEnd(), ConsoleColor.NONE);
            }
        }
//        if (errorInfo.isChanged()) System.out.println(":" + line);
        return errorInfo;
    }

    private HeuristicProcessResult rule1_6(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
        for (LineSegment segment : currentLineInfo.getEmptySegments()) {
            if (segment.getOwners().isEmpty()) {
                for (int i = segment.getStart(); i < segment.getEnd(); ++i) {
                    errorInfo.addChange(i, ConsoleColor.NONE);
                }
            }
        }
        return errorInfo;
    }

    /**
     * обновляет ренжи всех групп, чтобы текущая группа влезала от своего начала до начала следующей
     */
    private HeuristicProcessResult rule2_1(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
        int l, r, len;
        int size = line.getGroups().size();
        for (int i = 0; i < size; ++i) {
            Group curGroup = line.getGroups().get(i);
            l = curGroup.getLowerBound();
            if (i != size - 1) r = line.getGroups().get(i + 1).getLowerBound();
            else r = line.getLength();

            len = curGroup.getLength();
            if (curGroup.isEqualColorRight()) len += 1;

            if (r - l < len) {
                if (i == size - 1) {
                    errorInfo.setError(true);
                    return errorInfo;
                }
                line.getGroups().get(i + 1).setLowerBound(l + len);
                errorInfo.setRangeUpdated(true);
            }
        }
        for (int i = 0; i < size; ++i) {
            Group curGroup = line.getGroups().get(size - i - 1);
            r = curGroup.getUpperBound();
            if (i != size - 1) l = line.getGroups().get(size - i - 2).getUpperBound();
            else l = 0;

            len = curGroup.getLength();
            if (curGroup.isEqualColorLeft()) len += 1;

            if (r - l < len) {
                if (i == size - 1) {
                    errorInfo.setError(true);
                    return errorInfo;
                }
                line.getGroups().get(size - i - 2).setUpperBound(r - len);
                errorInfo.setRangeUpdated(true);
            }
        }
        return errorInfo;
    }

    /**
     * если у группы только один потенциальный владелец, обновляет его ренж
     */
    private HeuristicProcessResult rule2_2(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
        for (LineSegment lineSegment : currentLineInfo.getPartialGroups()) {
            TreeSet<Group> owners = lineSegment.getOwners();
            if (owners.isEmpty()) throw new RuntimeException("error (0 owners " + lineSegment + ") at line \n" + line);
            if (owners.size() == 1) {
                HeuristicProcessResult errorInfo1 = updateRange(owners.first(), lineSegment);
                if (errorInfo1.isChanged()) errorInfo.setRangeUpdated(true);
            }
        }
        return errorInfo;
    }

    /**
     * дополняет частичную группу до минимальной длины потенциальных владельцев, отталкиваясь от краев,
     * пустых клеток и соседних частичных групп, если текущая частичная группа не может быть соседней одним целым
     */
    private HeuristicProcessResult rule3_1(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();
        for (int groupNum = 0; groupNum < currentLineInfo.getPartialGroups().size(); ++groupNum) {
            LineSegment lineSegment = currentLineInfo.getPartialGroups().get(groupNum);
            int minLength = lineSegment.getMinLength();
            int checkRange = minLength - lineSegment.getLength();

            int fillFrom = lineSegment.getStart();
            int fillTo = lineSegment.getEnd();

            for (int i = 0; i < checkRange + 1; ++i) {
                int pos = lineSegment.getStart() - i - 1;
                if (pos < 0) {
                    fillTo = minLength;
                    break;
                }

                Cell cell = line.getCells().get(pos);
                if (!cell.isUndefined()) {
                    if (cell.getColor() == lineSegment.getColor()) {
                        LineSegment united = lineSegment.union(currentLineInfo.getPartialGroups().get(groupNum - 1));
                        currentLineInfo.countOwnersOfPartialGroup(united);
                        if (united.getOwners().isEmpty()) fillTo = pos + 2 + minLength;
                        else continue;
                    } else fillTo = pos + 1 + minLength;
                    break;
                }
            }

            for (int i = 0; i < checkRange + 1; ++i) {
                int pos = lineSegment.getEnd() + i;
                if (pos >= line.getLength()) {
                    fillFrom = line.getLength() - minLength;
                    break;
                }

                Cell cell = line.getCells().get(pos);
                if (!cell.isUndefined()) {
                    if (cell.getColor() == lineSegment.getColor()) {
                        LineSegment united = lineSegment.union(currentLineInfo.getPartialGroups().get(groupNum + 1));
                        currentLineInfo.countOwnersOfPartialGroup(united);
                        if (united.getOwners().isEmpty()) fillFrom = pos - 1 - minLength;
                        else continue;
                    } else fillFrom = pos - minLength;
                    break;
                }
            }
            for (int i = fillFrom; i < fillTo; ++i) {
                errorInfo.addChange(i, lineSegment.getColor());
            }
        }
        return errorInfo;
    }


    /**
     * обновляет ренж, основываясь на разделении ренжа на сегмненты
     * в качестве разделителей использует пустые клетки, клетки другого цвета и клетки,
     * в которых цвет текущей группы не может быть поставлен
     */
    private HeuristicProcessResult rule2_3(Line line) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();

        for (Group group : line.getGroups()) {
            List<LineSegment> segments = new ArrayList<>();
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
                    segments.add(new LineSegment(start, i, group.getColor()));
                    segmentFlag = false;
                }
            }
            if (segmentFlag) {
                segments.add(new LineSegment(start, group.getUpperBound(), group.getColor()));
            }

            segments.removeIf(seg -> seg.getLength() < group.getLength());
            if (segments.isEmpty()) throw new RuntimeException("0 segments");
            int lb = segments.get(0).getStart();
            int ub = segments.get(segments.size() - 1).getEnd();
            if (lb != group.getLowerBound() || ub != group.getUpperBound()) errorInfo.setRangeUpdated(true);
            group.setLowerBound(lb);
            group.setUpperBound(ub);
        }
        return errorInfo;
    }

    private HeuristicProcessResult updateRange(Group group, LineSegment lineSegment) {
        HeuristicProcessResult errorInfo = new HeuristicProcessResult();

        if (group.setUpperBound(Math.min(group.getUpperBound(), lineSegment.getStart() + group.getLength())))
            errorInfo.setRangeUpdated(true);
        if (group.setLowerBound(Math.max(group.getLowerBound(), lineSegment.getEnd() - group.getLength())))
            errorInfo.setRangeUpdated(true);
        return errorInfo;
    }

    /**
     * если клетка на расстоянии длины группы закрашена в тот же цвет, проверяет может ли эта частичная группа принадлежать текущей группе,
     * если нет, исключает эту частичную группу из ренжа, иначе двигает границу ренжа на 1
     *
     */
    private HeuristicProcessResult rule2_4(Line line) {
        System.out.println("!"+line);
        HeuristicProcessResult result = new HeuristicProcessResult();
        List<Group> groups = line.getGroups();
        for (Group group : groups) {
            if (group.getUpperBound() - group.getLowerBound() == group.getLength()) continue;
            int pos = group.getLowerBound() + group.getLength();
            if (line.getCells().get(pos).getColor() == group.getColor()) {
                int finalPos = pos;
                Optional<LineSegment> partialGroup = currentLineInfo.getPartialGroups()
                        .stream()
                        .filter(partgroup -> partgroup.getStart() <= finalPos && partgroup.getEnd() > finalPos)
                        .findFirst();
                if (partialGroup.isEmpty()) throw new RuntimeException("cant find partial group");
                int newLowerBound;
                if (partialGroup.get().getOwners().contains(group)) {
                    newLowerBound = partialGroup.get().getEnd() - group.getLength();
                } else newLowerBound = partialGroup.get().getEnd() + 1;
                newLowerBound = Math.max(newLowerBound, group.getLowerBound());
                if(group.setLowerBound(newLowerBound)) result.setRangeUpdated(true);
            }
            pos = group.getUpperBound() - group.getLength() - 1;
            if (line.getCells().get(pos).getColor() == group.getColor()) {
                int finalPos1 = pos;
                Optional<LineSegment> partialGroup = currentLineInfo.getPartialGroups()
                        .stream()
                        .filter(partgroup -> partgroup.getStart() <= finalPos1 && partgroup.getEnd() > finalPos1)
                        .findFirst();
                if (partialGroup.isEmpty()) throw new RuntimeException("cant find partial group");

                int newUpperBound;
                if (partialGroup.get().getOwners().contains(group)) {
                    newUpperBound = partialGroup.get().getStart() + group.getLength();
                } else newUpperBound = partialGroup.get().getStart() - 1;
                newUpperBound = Math.min(newUpperBound, group.getUpperBound());
                if(group.setUpperBound(newUpperBound)) result.setRangeUpdated(true);
            }
        }
        if(result.isRangeUpdated()) System.out.println(":"+ line);
        return result;
    }
}
