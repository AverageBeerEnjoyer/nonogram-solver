package ru.ac.uniyar.katkov.nonograms.backtracking;

import ru.ac.uniyar.katkov.nonograms.nonogram.Group;
import ru.ac.uniyar.katkov.nonograms.nonogram.Line;
import ru.ac.uniyar.katkov.nonograms.nonogram.Nonogram;
import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.util.TreeSet;

public class BacktrackingSolver {

    private Nonogram nng;
    private BacktrackingColumnInfo[] colsInfo;

    public void solve(Nonogram nonogram) {
        this.nng = nonogram;
        colsInfo = new BacktrackingColumnInfo[nng.width];
        for (int i = 0; i < nng.width; ++i) {
            colsInfo[i] = new BacktrackingColumnInfo(nng.getCols().get(i));
        }
        next(nng.getRows().get(0), 0, 0);
    }

    private int endOfGroup(Line line, int groupNum) {
        return groupNum < 0 ? 0 : line.getGroups().get(groupNum).getCurPlacement() + line.getGroups().get(groupNum).getLength();
    }

    private BacktrackingErrorInfo check(Line row, int groupNum) {
        Group group = row.getGroups().get(groupNum);
        BacktrackingErrorInfo errorInfo = new BacktrackingErrorInfo();

        int start = endOfGroup(row, groupNum - 1);
        for (int i = start; i < group.getCurPlacement(); ++i) {
            if (!colsInfo[i].isCorrect(row.getNum())) errorInfo.addBefore(i);
            else errorInfo.addChangedCol(i);
        }
        for (int i = group.getCurPlacement(); i < group.getCurPlacement() + group.getLength(); ++i) {
            if (!colsInfo[i].isCorrect(row.getNum())) errorInfo.addOn(i);
            else errorInfo.addChangedCol(i);
        }
        if (groupNum < row.getGroups().size() - 1) return errorInfo;
        for (int i = group.getCurPlacement() + group.getLength(); i < row.getLength(); ++i) {
            if (!colsInfo[i].isCorrect(row.getNum())) errorInfo.addAfter(i);
            else errorInfo.addChangedCol(i);
        }

        return errorInfo;
    }

    private BacktrackingErrorInfo placeGroup(Line line, int groupNum, int start) {
        BacktrackingErrorInfo errorInfo = new BacktrackingErrorInfo();
        Group group = line.getGroups().get(groupNum);
        group.setCurPlacement(start);
        int endPrev = endOfGroup(line, groupNum - 1);
        for (int i = endPrev; i < start; ++i) {
            if (!line.getCells().get(i).setColor(ConsoleColor.NONE)) errorInfo.addBefore(i);
        }
        for (int i = start; i < start + group.getLength(); ++i) {
            if (!line.getCells().get(i).setColor(group.getColor())) errorInfo.addOn(i);
        }
        if (groupNum == line.getGroups().size() - 1) {
            for (int i = group.getCurPlacement() + group.getLength(); i < line.getLength(); ++i) {
                if (!line.getCells().get(i).setColor(ConsoleColor.NONE)) errorInfo.addAfter(i);
            }
        }
        return errorInfo;
    }

    private void clearGroup(Line line, int groupNum) {
        Group group = line.getGroups().get(groupNum);

        int start = endOfGroup(line, groupNum - 1);
        int end = groupNum == line.getGroups().size() - 1 ? line.getLength() : group.getCurPlacement() + group.getLength();
        for (int i = start; i < end; ++i) {
            line.getCells().get(i).clear();
        }
        group.clear();
    }

    private void fillEmptyLine(Line line) {
        for (int i = 0; i < line.getLength(); ++i) {
            line.getCells().get(i).setColor(ConsoleColor.NONE);
        }
    }

    private BacktrackingErrorInfo checkEmptyLine(Line line) {
        BacktrackingErrorInfo errorInfo = new BacktrackingErrorInfo();
        for (int i = 0; i < line.getLength(); ++i) {
            if (!colsInfo[i].isCorrect(line.getNum())) errorInfo.addBefore(i);
            else errorInfo.addChangedCol(i);
        }
        return errorInfo;
    }

    private void turnBackColumnsEmptyLine(Line line, BacktrackingErrorInfo errorInfo) {
        for (int i : errorInfo.getChangedColsInfo()) {
            colsInfo[i].turnBackColInfo(line.getNum());
        }
    }

    private int next(Line row, int groupNum, int startPos) {
        if (Thread.currentThread().isInterrupted()) throw new RuntimeException("interrupted");
        if (row.getGroups().isEmpty()) {
            fillEmptyLine(row);
            BacktrackingErrorInfo errorInfo = checkEmptyLine(row);
            if (!errorInfo.has()) {
                if (row.getNum() == nng.height - 1) return -1;
                int res = next(nng.getRows().get(row.getNum() + 1), 0, 0);
                if (res == -1) return res;
                errorInfo.addBefore(0);
            }
            if (errorInfo.has()) {
                turnBackColumnsEmptyLine(row, errorInfo);
                return -2;
            }
        }

        Group group = row.getGroups().get(groupNum);

        for (int i = Math.max(startPos, group.getLowerBound()); i <= group.getUpperBound() - group.getLength(); ++i) {
            if (Thread.currentThread().isInterrupted()) throw new RuntimeException("interrupted");
            BacktrackingErrorInfo errorInfo = placeGroup(row, groupNum, i);
            errorInfo.union(check(row, groupNum));

            if (!errorInfo.has()) {

                if (row.getNum() == nng.height - 1 && groupNum == row.getGroups().size() - 1) return -1;

                Line nextLine;
                int nextGroup;
                int nextStartPos;

                if (groupNum < row.getGroups().size() - 1) {
                    nextLine = row;
                    nextGroup = groupNum + 1;
                    nextStartPos = group.getCurPlacement() + group.getLength();
                    if (row.getGroups().get(groupNum).isEqualColorRight()) ++nextStartPos;
                } else {
                    nextLine = nng.getRows().get(row.getNum() + 1);
                    nextGroup = 0;
                    nextStartPos = 0;
                }

                int fail = next(nextLine, nextGroup, nextStartPos);
                if (fail == -1) return fail;
                if (fail > -1) {
                    if (fail < group.getCurPlacement()) {
                        errorInfo.addBefore(fail);
                    } else if (fail < group.getCurPlacement() + group.getLength()) {
                        errorInfo.addOn(fail);
                    } else {
                        errorInfo.addAfter(fail);
                    }
                }
            }


            if (errorInfo.hasBefore()) {
                removeGroup(row, groupNum, errorInfo);
                return -2;
            }
            if (errorInfo.hasOn() || errorInfo.hasAfter()) {
                TreeSet<Integer> on = errorInfo.getOn();
                TreeSet<Integer> after = errorInfo.getAfter();
                int offsetOn = -1, offsetAfter = -1;
                if (errorInfo.hasOn()) {
                    offsetOn = on.last() - group.getCurPlacement() + 1;
                }
                if (errorInfo.hasAfter()) {
                    offsetAfter = after.last() - group.getCurPlacement() - group.getLength() + 1;
                }
                int offset = Math.max(offsetOn, offsetAfter);
                if (i + offset > group.getUpperBound() - group.getLength()) {
                    removeGroup(row, groupNum, errorInfo);
                    return -2;
                }
                i += (offset - 1);
                removeGroup(row, groupNum, errorInfo);
                continue;
            }
            removeGroup(row, groupNum, errorInfo);
        }
        return -2;
    }

    private void removeGroup(Line row, int groupNum, BacktrackingErrorInfo errorInfo) {
        for (int i : errorInfo.getChangedColsInfo()) {
            colsInfo[i].turnBackColInfo(row.getNum());
        }
        clearGroup(row, groupNum);
    }
}
