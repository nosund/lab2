package amazed.solver;

import amazed.maze.Maze;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * <code>ForkJoinSolver</code> implements a solver for
 * <code>Maze</code> objects using a fork/join multi-thread
 * depth-first search.
 * <p>
 * Instances of <code>ForkJoinSolver</code> should be run by a
 * <code>ForkJoinPool</code> object.
 */


public class ForkJoinSolver extends SequentialSolver{
    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal.
     *
     * @param maze   the maze to be searched
     */
    public ForkJoinSolver(Maze maze)
    {
        super(maze);
    }
    private ForkJoinSolver(int start,Maze maze)
    {
        this(maze);
        this.start = start;
    }

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal, forking after a given number of visited
     * nodes.
     *
     * @param maze        the maze to be searched
     * @param forkAfter   the number of steps (visited nodes) after
     *                    which a parallel task is forked; if
     *                    <code>forkAfter &lt;= 0</code> the solver never
     *                    forks new tasks
     */
    public ForkJoinSolver(Maze maze, int forkAfter)
    {
        this(maze);
        this.forkAfter = forkAfter;
    }

    /**
     * Searches for and returns the path, as a list of node
     * identifiers, that goes from the start node to a goal node in
     * the maze. If such a path cannot be found (because there are no
     * goals, or all goals are unreacheable), the method returns
     * <code>null</code>.
     *
     * @return   the list of node identifiers from the start node to a
     *           goal node in the maze; <code>null</code> if such a path cannot
     *           be found.
     */
    @Override
    public List<Integer> compute()
    {
        return parallelSearch();
    }

    private List<Integer> parallelSearch()
    {
        int id = 0;
        int forkCount = forkAfter-1;
        int player = maze.newPlayer(start);
        frontier.push(start);
        while(!frontier.empty()){
            int current = frontier.pop();
            //om målet ligger på current
            if(maze.hasGoal(current)){
                maze.move(player, current);
                return pathFromTo(start, current);
            }
            //om current node inte besökts, lägg till i visited och flytta till noden
            if(!visited.contains(current)){
                maze.move(player, current);
                visited.add(current);
                forkCount -= 1;
                //här ska nya threads skapas
                if(forkCount==0){                    
                    //id += maze.neighbors(current).size();
                    //för varje cell bredvid current
                    for(int nb: maze.neighbors(current)){
                        ForkJoinSolver fs = new ForkJoinSolver(nb, maze);
                        fs.fork();
                    }
                    forkCount = forkAfter-1;
                }
                else if(forkCount>0){
                    for (int nb: maze.neighbors(current)){
                        frontier.push(nb);
                        if (!visited.contains(nb)){
                            predecessor.put(nb, current);
                        }

                    }
                }
            }
        }
        return null;
    }
}
