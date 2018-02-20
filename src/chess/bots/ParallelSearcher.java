package chess.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Move;
import java.util.concurrent.RecursiveTask;
import cse332.chess.interfaces.Evaluator;

// 2/21
public class ParallelSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {
	
	private static final int divideCutoff = 4;
	private static final ForkJoinPool POOL = new ForkJoinPool();
	
	public M getBestMove(B board, int myTime, int opTime) {
		List<M> moves = board.generateMoves();
		
		//using the cutoff instance variable in AbstractSearcher
    	return (POOL.invoke(new GetBestMoveTask(this.evaluator, board, ply, 
    			moves, cutoff, divideCutoff, 0, moves.size(), null))).move; 
    }
	
	class GetBestMoveTask extends RecursiveTask<BestMove<M>> {
		Evaluator<B> evaluator;
		B board;
    	int depth;
    	
    	List<M> moves; 
    	
    	int sequentialCutOff;
    	int divideCutoff;
    	
    	//lo and hi for separating the array
    	int lo;
    	int hi;
    	M move;
		
    	//constructor
    	public GetBestMoveTask(Evaluator<B> evaluator, B board, int depth,
    			List<M> moves, int sequentialCutOff, int divideCutoff, int lo, int hi, M move) {
    		
    		this.evaluator = evaluator;	
    		this.board = board;
    		this.depth = depth;
    		
    		this.moves = moves;  
    		
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
        		moves = board.generateMoves();
        		hi = moves.size();	
    		}
    		
    		// sequential
    		if (depth <= sequentialCutOff || moves.isEmpty()) {
    			return SimpleSearcher.minimax(evaluator, board, depth);
    		}
    		
			// make the moves, then parallelize each move to get the best move
			if (hi - lo <= divideCutoff) {
				
				BestMove<M> bestMove = new BestMove<M>(-evaluator.infty());
				ArrayList<GetBestMoveTask> tasks = new ArrayList<GetBestMoveTask>();
				
				//add all the tasks, note that these are sequential tasks (lo = 0 = hi)
				for (int i = lo; i < hi; i++) {
					tasks.add(new GetBestMoveTask (evaluator, board, depth - 1,
							moves, sequentialCutOff, divideCutoff, 0, 0, moves.get(i)));
				}
				
				//fork all the tasks
				for (int i = 1; i < tasks.size(); i++) {
					tasks.get(i).fork();
				}
				
				//finding best value for each task
				for (int i = 0; i < tasks.size(); i++) {
					
					int bestValue;
					
					//compute the first one, and join the others
					if (i == 0) {
						bestValue = tasks.get(i).compute().negate().value;
					} 
					else {
						bestValue = tasks.get(i).join().negate().value;
					}
					
					//update best value
					if (bestValue > bestMove.value) {
						bestMove.move = moves.get(i + lo);
						bestMove.value = bestValue;
					}	
				}
				return bestMove;
			}
			
			// parallelism part 
			int mid = lo + (hi - lo) / 2;
			
			GetBestMoveTask left = new GetBestMoveTask (evaluator, board, depth,
					moves, sequentialCutOff, divideCutoff, lo, mid, null);
			GetBestMoveTask right = new GetBestMoveTask (evaluator, board, depth,
					moves, sequentialCutOff, divideCutoff, mid, hi, null);
			
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