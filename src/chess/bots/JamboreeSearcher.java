package chess.bots;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class JamboreeSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {
	
	private static final ForkJoinPool POOL = new ForkJoinPool();
	private static final int divideCutoff = 5;
	private static final double PERCENTAGE_SEQUENTIAL = 0.5;
	
	
	public M getBestMove(B board, int myTime, int opTime) {
		List<M> moves = board.generateMoves();
		
		//using the cutoff instance variable in AbstractSearcher
		BestMove<M> best = POOL.invoke(new GetBestMoveTask(board, ply, moves, cutoff,
				divideCutoff, 0, moves.size(), null, evaluator, -evaluator.infty(), evaluator.infty())); 
		return best.move;
    }
    
	public class GetBestMoveTask extends RecursiveTask<BestMove<M>> {
		B board;
    	int depth, sequentialCutOff, divideCutOff, lo, hi, alpha, beta;
    	M move;
    	List<M> moves;
    	Evaluator<B> evaluator;
	    
	    public GetBestMoveTask(B board, int depth, List<M> moves, int sequentialCutOff, int divideCutOff, int lo, int hi,
				M move, Evaluator<B> evaluator, int alpha, int beta) {
	    	this.evaluator = evaluator;	
    		this.board = board;
    		this.depth = depth;
    		
    		this.moves = moves;  
    		
    		this.sequentialCutOff = sequentialCutOff;
    		this.divideCutOff = divideCutOff;
    		
    		this.lo = lo;
    		this.hi = hi;
    		this.move = move;
    		this.alpha = alpha;
    		this.beta = beta;
		}

	    public BestMove<M> compute() {
	    	boolean full = false;
    		if (move != null) {
    			board = board.copy();
        		board.applyMove(move);
        		moves = board.generateMoves();
        		hi = moves.size();
        		full = true;
    		}
    		
    		// sequential
    		if (depth <= sequentialCutOff || moves.isEmpty()) {
    			return AlphaBetaSearcher.alphaBeta(evaluator, board, depth, alpha, beta);
    		}
    		
    		BestMove<M> best = new BestMove<M>(move, -evaluator.infty());
    		
			// make the moves, then parallelize each move to get the best move
			if (hi - lo <= divideCutoff) {
				
				BestMove<M> bestMove = new BestMove<M>(-evaluator.infty());
				ArrayList<GetBestMoveTask> tasksList = new ArrayList<GetBestMoveTask>();
				
				//add all the tasks, note that these are sequential tasks (lo = 0 = hi)
				for (int i = lo; i < hi; i++) {
					GetBestMoveTask task = new GetBestMoveTask (board, depth - 1, moves,
							sequentialCutOff, divideCutoff, 0, 0, moves.get(i), evaluator, -beta, -alpha);
					tasksList.add(task);
				}
				
				//fork all the tasks
				for (int i = 1; i < tasksList.size(); i++) {
					tasksList.get(i).fork();
				}
				
				int alphaValue;
				
				//compute the first task
				alphaValue = -tasksList.get(0).compute().value;
				
				//update best value
				if (alphaValue > bestMove.value) {
					bestMove.move = moves.get(0 + lo);
					bestMove.value = alphaValue;
				}	
				
				//finding best value for each task
				for (int i = 1; i < tasksList.size(); i++) {
					
					//join the other tasks
					alphaValue = -tasksList.get(i).join().value;
					
					//update best value
					if (alphaValue > bestMove.value) {
						bestMove.move = moves.get(i + lo);
						bestMove.value = alphaValue;
					}	
				}
				return bestMove;
		    }
			
			if (full) {
				// sequential run through of tasks
	    		for (int i = lo; i < (int) Math.ceil(PERCENTAGE_SEQUENTIAL * (hi - lo)) + lo; i++) {
		    		int value = -(new GetBestMoveTask(board, depth - 1, moves, sequentialCutOff, divideCutOff, 0, 0,
		    				moves.get(i), evaluator, -beta, -alpha)).compute().value;
		    		if (value > alpha) {
		    			alpha = value;
						
						//updating best
						best.move = moves.get(i);
						best.value = alpha;
		    		}
		    		if (alpha >= beta) {
		    			return best;
		    		}
		    	}
				
	    		// update lo to be the mid/start of parallel
		    	lo = (int) Math.ceil(PERCENTAGE_SEQUENTIAL * (hi - lo)) + lo;
			}
			
			// parallelism part 
			int mid = lo + (hi - lo) / 2;
			
			GetBestMoveTask left = new GetBestMoveTask (board, depth, moves, sequentialCutOff, divideCutoff, lo, mid, null, evaluator, alpha, beta);
			GetBestMoveTask right = new GetBestMoveTask (board, depth, moves, sequentialCutOff, divideCutoff, mid, hi, null, evaluator, alpha, beta);
			
			right.fork();
			
			BestMove<M> leftMove = left.compute();
			BestMove<M> rightMove = right.join();
	    	
			//return the higher value
			if (leftMove.value > rightMove.value) {
				if (leftMove.value > best.value) {
					return leftMove;
				}
			} else {
				if (rightMove.value > best.value) {
					return rightMove;
				}
			}
			return best;
    	}
	}
}
