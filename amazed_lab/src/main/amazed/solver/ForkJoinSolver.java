package amazed.solver;

import amazed.maze.Maze;

/* import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set; */
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;;

/**
 * <code>ForkJoinSolver</code> implements a solver for
 * <code>Maze</code> objects using a fork/join multi-thread
 * depth-first search.
 * <p>
 * Instances of <code>ForkJoinSolver</code> should be run by a
 * <code>ForkJoinPool</code> object.
 */


public class ForkJoinSolver extends SequentialSolver{
    // public Thread thread;
    protected class gubbe {
        final ForkJoinSolver thread;
        final int start;
        gubbe(ForkJoinSolver thread, int start){
            this.thread = thread;
            this.start = start;
        }
    }

    static private ConcurrentSkipListSet<Integer> visited = new ConcurrentSkipListSet<>();
    static private AtomicBoolean goalFound = new AtomicBoolean();

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal.
     * 
     * @param maze   the maze to be searched
     */

    public ForkJoinSolver(Maze maze)
    {
        super(maze);
        visited.add(start);
    }

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * new start node to a goal.
     * @param start
     * @param maze
     */

    //takes nb as start and starts searching 
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
    public List<Integer> compute() {
        return parallelSearch();
    }

    private List<Integer> parallelSearch(){
        //return parallelSearch(start);
    //}

    //private List<Integer> parallelSearch(int start)
    //{
        //int forkCount = forkAfter;
        int player = maze.newPlayer(start);
        frontier.push(start);
        List<gubbe> threads = new ArrayList<>();
        List<gubbe> threads_join = new ArrayList<>();
        List<Integer> pathFromTo = null;
        int steps = 0;
        /*if(forkAfter==9){
            forkAfter = 3;
        }*/

        while(!frontier.empty()){
            int current = frontier.pop();
                       
            //om målet ligger på current
            if(maze.hasGoal(current)){
                maze.move(player, current);
                goalFound.set(true);
                pathFromTo = pathFromTo(start, current);
                break;
            }
            if(goalFound.get()){
                break;
            }

            maze.move(player, current);
            steps ++;
            int numNB = 0;

                //if(forkCount==0){                    
                //för varje cell bredvid current
            for(int nb: maze.neighbors(current)){
                    //frontier.push(nb);
                //om nb inte besökts innan; 
                if(visited.add(nb)){
                    numNB++;
                } 
                else {
                    continue;
                }
                    
                //om minst en väg finns, lägg till i visited 
                if(numNB == 1){
                    frontier.push(nb);
                    predecessor.put(nb, current);
                }
                
                else if(numNB > 1){
                    ForkJoinSolver fs = new ForkJoinSolver(nb, maze);
                    gubbe gubben = new gubbe(fs, current);
                    threads.add(gubben);
                    threads_join.add(gubben);
/* 
                    threads.add(new gubbe(fs, current));
                    threads_join.add(gubbe(fs,current));
                    */
                }
            }
            if(steps > forkAfter){
                for(gubbe i : threads){
                    i.thread.fork();
                }
            }
            else{
                continue;
            }
            threads.clear();
            steps = 0;
        }
        
        //här ska threads joinas
        for(gubbe thread: threads_join){
            List<Integer> path = thread.thread.join();
            if(thread.thread.join() != null) {
                pathFromTo = pathFromTo(start, thread.start);
                pathFromTo.addAll(path);
                System.out.println("gubbe joinad");
            }
            else{
                System.out.println(start + " and " + thread.start);
            }
        }
        return pathFromTo;
    }
}