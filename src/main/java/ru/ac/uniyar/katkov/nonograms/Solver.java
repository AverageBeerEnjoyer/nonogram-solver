package ru.ac.uniyar.katkov.nonograms;

import ru.ac.uniyar.katkov.nonograms.backtracking.BacktrackingSolver;
import ru.ac.uniyar.katkov.nonograms.heuristic.HeuristicSolver;
import ru.ac.uniyar.katkov.nonograms.nonogram.Nonogram;

public class Solver {
    private final BacktrackingSolver backtrackingSolver = new BacktrackingSolver();
    private final HeuristicSolver heuristicSolver = new HeuristicSolver();

    public void solveByBacktracking(Nonogram nonogram){
        backtrackingSolver.solve(nonogram);
    }

    public void solve(Nonogram nonogram){
        heuristicSolver.solve(nonogram);
        nonogram.fix();
        backtrackingSolver.solve(nonogram);
    }

    public void solveByHeuristic(Nonogram nonogram){
        heuristicSolver.solve(nonogram);
    }
}
