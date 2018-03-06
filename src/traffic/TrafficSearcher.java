package traffic;

import java.util.List;

import chess.bots.AlphaBetaSearcher;
import chess.bots.BestMove;
import cse332.chess.interfaces.AbstractSearcher;
import cse332.chess.interfaces.Board;
import cse332.chess.interfaces.Evaluator;
import cse332.chess.interfaces.Move;

public class TrafficSearcher<M extends Move<M>, B extends Board<M, B>> extends
        AbstractSearcher<M, B> {

    public M getBestMove(B board, int myTime, int opTime) {
    	return alphaBeta(evaluator, board, ply, -evaluator.infty(), evaluator.infty()).move;
    }
    
public static <M extends Move<M>, B extends Board<M, B>> BestMove<M> alphaBeta(Evaluator<B> evaluator, B board, int depth, int alpha, int beta) {
		
    	//p is a leaf
    	if (depth == 0) {
			return new BestMove<M>(evaluator.eval(board));
		} 
    	
		List<M> moves = board.generateMoves();
		
		//if there is no moves, call to eval on the board
		if (moves.isEmpty()) {
			return new BestMove<M>(evaluator.eval(board));
		} 
		
		//set best value as negative infinity
		BestMove<M> best = new BestMove<M>(-evaluator.infty());
		
		for (M move : moves) {
			
			board.applyMove(move);
			
			//int value = -alphabeta(p, -beta, -alpha);
			int value = -(alphaBeta(evaluator, board, depth - 1, -beta, -alpha)).value;
			
			board.undoMove();
			
			//if value is found between alpha and beta, new lower bound
			if (value > alpha) {
				
				alpha = value;
				
				//updating best
				best.move = move;
				best.value = alpha;
			}
			
			//if value is bigger than beta, don't change
			if (alpha >= beta){
				return best;
			}		
			
		}
		
		return best;
	}
}