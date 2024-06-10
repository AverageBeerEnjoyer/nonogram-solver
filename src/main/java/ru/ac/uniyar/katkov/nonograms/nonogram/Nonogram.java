package ru.ac.uniyar.katkov.nonograms.nonogram;

import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.util.Arrays;
import java.util.List;

public class Nonogram {
    public final int height, width;
    private final Cell[][] cells;
    private final List<Line> rows;
    private final List<Line> cols;
    private final List<ConsoleColor> colors;

    public Nonogram(List<Line> rows, List<Line> cols, List<ConsoleColor> colors) {
        this.height = rows.size();
        this.width = cols.size();
        cells = new Cell[height][width];
        this.cols = cols;
        this.rows = rows;
        this.colors = colors;

        createCells();
        rows.forEach(Line::recountColors);
        cols.forEach(Line::recountColors);
    }

    private void createCells() {
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                cells[i][j] = new Cell(i, j);
                rows.get(i).getCells().add(cells[i][j]);
                cols.get(j).getCells().add(cells[i][j]);
            }
        }
    }

    public void print(int rownum) {
        System.out.println();
        System.out.println(filledPart(rownum));
    }

    public void print() {
        System.out.println();
        System.out.println(withConsoleColors());
    }

    public List<ConsoleColor> getColors() {
        return colors;
    }

    public List<Line> getCols() {
        return cols;
    }

    public List<Line> getRows() {
        return rows;
    }

    public Cell[][] getCells() {
        return cells;
    }

    private String filledPart(int rownum) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rownum + 1; ++i) {
            Arrays.stream(cells[i]).forEach(cell -> sb.append(cell.toString()));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String withConsoleColors() {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(cells).forEach(cells1 -> {
            Arrays.stream(cells1).forEach(cell -> sb.append(cell.toString()));
            sb.append("\n");
        });
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        rows.forEach(row -> {
            sb.append(row.withoutGroups()).append("\n");
        });
        return sb.toString();
    }

    public String getMetaInfo() {
        return "Size: " + width + "x" + height + "\n";
    }

    public void fix() {
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (!cell.isUndefined()) cell.fix();
            }
        }
    }

    public void reset() {
        Arrays.stream(cells).forEach(row -> Arrays.stream(row).forEach(Cell::reset));
        rows.forEach(Line::reset);
        cols.forEach(Line::reset);
        rows.forEach(Line::recountColors);
        cols.forEach(Line::recountColors);
    }

    public boolean isCompleted() {
        boolean res = true;
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (cell.isUndefined()) return false;
            }
        }
        return true;
    }

    public void printLinesInfo() {
        for (Line line : rows) {
            System.out.println(line);
            for (Group group : line.getGroups()) {
                if (group.getUpperBound() - group.getLowerBound() != group.getLength()) {
                    System.out.println(group);
                }
            }
        }
        for (Line line : cols) {
            System.out.println(line);
            for (Group group : line.getGroups()) {
                if (group.getUpperBound() - group.getLowerBound() != group.getLength()) {
                    System.out.println(group);
                }
            }
        }
    }
}
