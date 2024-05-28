package ru.ac.uniyar.katkov.nonograms.nonogram;

import ru.ac.uniyar.katkov.nonograms.utils.RGBToANSIConverter;
import ru.ac.uniyar.katkov.nonograms.utils.ConsoleColor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Parser {

    public static Nonogram parseOlsaksFile(File file) {
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found: " + file.getName());
        }
        for (int i = 0; i < 4; ++i) {
            scanner.nextLine();
        }
        HashMap<String, ConsoleColor> colors = new HashMap<>();
        while (!scanner.hasNext(":")) {
            String key = scanner.next().substring(0, 1);
            String hex = scanner.next().substring(1, 7);
            ConsoleColor color = new ConsoleColor(RGBToANSIConverter.selectColor(hex), key);
            colors.put(key, color);

            scanner.nextLine();
        }
        ArrayList<Line> rows = new ArrayList<>();
        ArrayList<ArrayList<Group>> rowGroups = new ArrayList<>();
        ArrayList<Line> cols = new ArrayList<>();
        ArrayList<ArrayList<Group>> colGroups = new ArrayList<>();
        scanner.nextLine();
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            if(s.startsWith(":")) break;
            rowGroups.add(scanLines(s, colors));
        }
        while (scanner.hasNextLine()) {
            colGroups.add(scanLines(scanner.nextLine(), colors));
        }
        for(int i=0;i< rowGroups.size();++i){
            Line line = new Line(colGroups.size(), i, rowGroups.get(i), true);
            rows.add(line);
        }
        for(int i=0;i< colGroups.size();++i){
            Line line = new Line(rowGroups.size(), i, colGroups.get(i), false);
            cols.add(line);
        }
        return new Nonogram(rows,cols, colors.values().stream().toList());
    }

    private static ArrayList<Group> scanLines(String s, HashMap<String, ConsoleColor> colors) {
        String[] split = s.split(" ");
        ArrayList<Group> groups = new ArrayList<>();
        if(split[0].isEmpty()) return groups;
        for (int i=0;i<split.length;++i){
            String groupString = split[i];
            String key = groupString.substring(groupString.length() - 1);
            String length = groupString.substring(0, groupString.length() - 1);
            Group group = new Group(i, Integer.parseInt(length), colors.get(key));
            groups.add(group);
        }
        return groups;
    }

    public static Nonogram parseMyFile(File file) {
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("file does not exist");
        }

        int height = scanner.nextInt();
        int width = scanner.nextInt();

        int colorsNumber = scanner.nextInt();
        List<ConsoleColor> colors = new ArrayList<>();
        for (int i = 0; i < colorsNumber; ++i) {
            String letter = scanner.next();
            String hex = scanner.next();
            scanner.nextLine();
            ConsoleColor color = new ConsoleColor(RGBToANSIConverter.selectColor(hex.substring(1)), letter);
            colors.add(color);
        }
        ArrayList<Line> rows = new ArrayList<>();
        ArrayList<Line> cols = new ArrayList<>();

        scanGroups(scanner, height, colors, rows, true);
        scanGroups(scanner, width, colors, cols, false);
        return new Nonogram(rows, cols, colors);
    }

    private static void scanGroups(Scanner scanner, int n, List<ConsoleColor> colors, ArrayList<Line> lines, boolean rows) {
        for (int i = 0; i < n; ++i) {
            int groupsNumber = scanner.nextInt();
            ArrayList<Group> groups = new ArrayList<>(groupsNumber);
            for (int j = 0; j < groupsNumber; ++j) {
                int length = scanner.nextInt();
                int colNum = scanner.nextInt();
                groups.add(new Group(j, length, colors.get(colNum)));
            }
            lines.add(new Line(n, i, groups, rows));
        }
    }
}
