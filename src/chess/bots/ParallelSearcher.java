package chess.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Move;
import java.util.concurrent.RecursiveTask;
import cse332.chess.interfaces.Evaluator;

public class ParallelSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {
	
<<<<<<< HEAD
=======
	private static final int divideCutoff = 3;
>>>>>>> branch 'master' of ssh://git@gitlab.cs.washington.edu/cse332-18wi/p3-gauntletlegends.git
	private static final ForkJoinPool POOL = new ForkJoinPool();
	private static final int divideCutoff = 3;
	
	public M getBestMove(B board, int myTime, int opTime) {
		List<M> moves = board.generateMoves();
		
		//using the cutoff instance variable in AbstractSearcher
		BestMove<M> best = POOL.invoke(new GetBestMoveTask(board, ply, moves, cutoff,
				divideCutoff, 0, moves.size(), null, evaluator)); 
		return best.move;
    }
	
	class GetBestMoveTask extends RecursiveTask<BestMove<M>> {
		
		B board;
    	int depth;
    	
    	List<M> moveList; 
    	
    	int sequentialCutOff;
    	int divideCutoff;
    	
    	//lo and hi for separating the array
    	int lo;
    	int hi;
    	M move;
    	
    	Evaluator<B> evaluator;
		
    	//constructor
    	public GetBestMoveTask(B board, int depth, List<M> moveList,
    			int sequentialCutOff, int divideCutoff, int lo, int hi, M move, Evaluator<B> evaluator) {
    		
    		this.evaluator = evaluator;	
    		this.board = board;
    		this.depth = depth;
    		
    		this.moveList = moveList;  
    		
    		this.sequentialCutOff = sequentialCutOff;
    		this.divideCutoff = divideCutoff;
    		
    		this.lo = lo;
    		this.hi = hi;
    		this.move = move;

    	}
    	
    	public BestMove<M> compute() {
    		
    		if (move != null) {
    			board = board.copy();
        		board.applyMove(move);
        		moveList = board.generateMoves();
        		hi = moveList.size();	
    		}
    		
    		// sequential
    		if (depth <= sequentialCutOff) {
    			return SimpleSearcher.minimax(board, depth, evaluator);
    		}
    		
			// make the moves, then parallelize each move to get the best move
			if (hi - lo <= divideCutoff) {
				
				BestMove<M> bestMove = new BestMove<M>(-evaluator.infty());
				ArrayList<GetBestMoveTask> tasksList = new ArrayList<GetBestMoveTask>();
				
				//add all the tasks, note that these are sequential tasks (lo = 0 = hi)
				for (int i = lo; i < hi; i++) {
					GetBestMoveTask task = new GetBestMoveTask (board, depth - 1, moveList,
							sequentialCutOff, divideCutoff, 0, 0, moveList.get(i), evaluator);
					tasksList.add(task);
				}
				
				//fork all the tasks
				for (int i = 1; i < tasksList.size(); i++) {
					tasksList.get(i).fork();
				}
				
				int bestValue;
				
				//compute the first task
				bestValue = tasksList.get(0).compute().negate().value;
				
				//update best value
				if (bestValue > bestMove.value) {
					bestMove.move = moveList.get(0 + lo);
					bestMove.value = bestValue;
				}	
				
				//finding best value for each task
				for (int i = 1; i < tasksList.size(); i++) {
					
					//join the other tasks
					bestValue = tasksList.get(i).join().negate().value;
					
					//update best value
					if (bestValue > bestMove.value) {
						bestMove.move = moveList.get(i + lo);
						bestMove.value = bestValue;
					}	
				}
				return bestMove;
			}
			
			// parallelism part 
			int mid = lo + (hi - lo) / 2;
			
			GetBestMoveTask left = new GetBestMoveTask (board, depth, moveList, sequentialCutOff, divideCutoff, lo, mid, null, evaluator);
			GetBestMoveTask right = new GetBestMoveTask (board, depth,moveList, sequentialCutOff, divideCutoff, mid, hi, null, evaluator);
			
			right.fork();
			
			BestMove<M> leftMove = left.compute();
			BestMove<M> rightMove = right.join();
			
			//return the higher value
			if (leftMove.value > rightMove.value) {
				return leftMove;
			}
			else {
				return rightMove;
			}	
    	}
    }
}