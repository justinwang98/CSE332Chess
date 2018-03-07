package experiments;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.AlphaBetaSearcher;
import chess.bots.LazySearcher;
import chess.bots.SimpleSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

public class CountingNodes {
	
	public static long count = 0;
	
    public Searcher<ArrayMove, ArrayBoard> whitePlayer;
    public Searcher<ArrayMove, ArrayBoard> blackPlayer;
    public static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    
    private ArrayBoard board;
    
    public static void main(String[] args) {

    	count = 0;
    	JamboreeSearcherTest.JamboreeCount.reset();
    	//from testGame
    	CountingNodes game = new CountingNodes();
    	
    	int ply = 5;
    	//for (int ply = 1; ply <= 5; ply++) {
	        game.playSimpleSearcher(ply);
	        //divide count by number of fens
	        count = count / 64;
	        System.out.println("Count for Minimax when ply is " + ply + " : " + count);
	        count = 0;
	        
	        game.playParallelSearcher(ply);
	        //divide count by number of fens
	        count = count / 64;
	        System.out.println("Count for ParallelMinimax when ply is " + ply + " : " + count);
	        count = 0;
	        
	        game.playAlphaBetaSearcher(ply);
	        //divide count by number of fens
	        count = count / 64;
	        System.out.println("Count for AlphaBeta when ply is " + ply + " : " + count);
	        count = 0;
	        JamboreeSearcherTest.JamboreeCount.reset();
	        
	        game.playJamboreeSearcher(ply);
	        //divide count by number of fens
	        long totalCount = JamboreeSearcherTest.JamboreeCount.intValue();
	        totalCount += count;
	        totalCount = totalCount / 64;
	        System.out.println("Count for Jamboree when ply is " + ply + " : " + totalCount);
	        count = 0;
	        totalCount = 0;
	        JamboreeSearcherTest.JamboreeCount.reset();
    	//}
    }

    public CountingNodes() {
        setupWhitePlayer(new SimpleSearcher<ArrayMove, ArrayBoard>(), 3, 3);
        setupBlackPlayer(new AlphaBetaSearcher<ArrayMove, ArrayBoard>(), 4, 4);
    }
    
    public void playSimpleSearcher(int ply) {
       this.board = ArrayBoard.FACTORY.create().init(STARTING_POSITION);
       Searcher<ArrayMove, ArrayBoard> currentPlayer = this.blackPlayer;
       
       /* Note that this code does NOT check for stalemate... */
       while (!board.inCheck() || board.generateMoves().size() > 0) {
           currentPlayer = currentPlayer.equals(this.whitePlayer) ? this.blackPlayer : this.whitePlayer;
           //System.out.printf("%3d: " + board.fen() + "\n", turn);
           this.board.applyMove(currentPlayer.getBestMove(board, 1000, 1000));
         //from testStartPosition
           Searcher<ArrayMove, ArrayBoard> SimpleSearcherTest = new SimpleSearcherTest<>();
           getBestMove(board.fen(), SimpleSearcherTest, ply, 0);
       }
    }
    
    public void playParallelSearcher(int ply) {
        this.board = ArrayBoard.FACTORY.create().init(STARTING_POSITION);
        Searcher<ArrayMove, ArrayBoard> currentPlayer = this.blackPlayer;
        
        /* Note that this code does NOT check for stalemate... */
        while (!board.inCheck() || board.generateMoves().size() > 0) {
            currentPlayer = currentPlayer.equals(this.whitePlayer) ? this.blackPlayer : this.whitePlayer;
            //System.out.printf("%3d: " + board.fen() + "\n", turn);
            this.board.applyMove(currentPlayer.getBestMove(board, 1000, 1000));
          //from testStartPosition
            Searcher<ArrayMove, ArrayBoard> ParallelSearcherTest = new ParallelSearcherTest<>();
            getBestMove(board.fen(), ParallelSearcherTest, ply, ply);
        }
     }
    
    public void playAlphaBetaSearcher(int ply) {
        this.board = ArrayBoard.FACTORY.create().init(STARTING_POSITION);
        Searcher<ArrayMove, ArrayBoard> currentPlayer = this.blackPlayer;
        
        /* Note that this code does NOT check for stalemate... */
        while (!board.inCheck() || board.generateMoves().size() > 0) {
            currentPlayer = currentPlayer.equals(this.whitePlayer) ? this.blackPlayer : this.whitePlayer;
            //System.out.printf("%3d: " + board.fen() + "\n", turn);
            this.board.applyMove(currentPlayer.getBestMove(board, 1000, 1000));
          //from testStartPosition
            Searcher<ArrayMove, ArrayBoard> AlphaBetaSearcherTest = new AlphaBetaSearcherTest<>();
            getBestMove(board.fen(), AlphaBetaSearcherTest, ply, ply / 2);
        }
     }
    
    public void playJamboreeSearcher(int ply) {
        this.board = ArrayBoard.FACTORY.create().init(STARTING_POSITION);
        Searcher<ArrayMove, ArrayBoard> currentPlayer = this.blackPlayer;
        
        /* Note that this code does NOT check for stalemate... */
        while (!board.inCheck() || board.generateMoves().size() > 0) {
            currentPlayer = currentPlayer.equals(this.whitePlayer) ? this.blackPlayer : this.whitePlayer;
            //System.out.printf("%3d: " + board.fen() + "\n", turn);
            this.board.applyMove(currentPlayer.getBestMove(board, 1000, 1000));
          //from testStartPosition
            Searcher<ArrayMove, ArrayBoard> JamboreeSearcherTest = new JamboreeSearcherTest<>();
            getBestMove(board.fen(), JamboreeSearcherTest, ply, ply / 2);
        }
     }

    public static ArrayMove getBestMove(String fen, Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) { 
        searcher.setDepth(depth);
        searcher.setCutoff(cutoff);
        searcher.setEvaluator(new SimpleEvaluator());

        return searcher.getBestMove(ArrayBoard.FACTORY.create().init(fen), 0, 0);
    }
    
    public Searcher<ArrayMove, ArrayBoard> setupPlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        searcher.setDepth(depth);
        searcher.setCutoff(cutoff);
        searcher.setEvaluator(new SimpleEvaluator());
        return searcher; 
    }
    public void setupWhitePlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        this.whitePlayer = setupPlayer(searcher, depth, cutoff);
    }
    public void setupBlackPlayer(Searcher<ArrayMove, ArrayBoard> searcher, int depth, int cutoff) {
        this.blackPlayer = setupPlayer(searcher, depth, cutoff);
    }
}
