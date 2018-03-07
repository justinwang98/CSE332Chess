package tests;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.JamboreeSearcher;
import chess.bots.ParallelSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Searcher;

public class WriteupTest {
    public Searcher<ArrayMove, ArrayBoard> whitePlayer;
    public Searcher<ArrayMove, ArrayBoard> blackPlayer;
    
    private ArrayBoard board;
    
    public static void main(String[] args) {
    	String beg = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    	String mid = "4k3/p2n1pp1/3R1n2/4p3/2P2r2/P7/1PP5/2KR4 w Hk -";
    	String end = "8/2k5/5n2/2P1R3/8/2P3p1/r4pK1/8 w H -";

    	for (int i = 0; i < 6; i++) {
    		run(beg, i);
    		run(mid, i);
    		run(end, i);
    	}
    }
    
    public static void run(String startPos, int cutoff) {
        final int NUM_TESTS = 5;
        final int NUM_WARMUP = 2;
        
        double totalTime = 0;
        for (int i = 0; i < NUM_TESTS; i++) {
            long startTime = System.currentTimeMillis();
            // Put whatever you want to time here .....
            
            WriteupTest game = new WriteupTest(cutoff);
            game.play(startPos);
            
            long endTime = System.currentTimeMillis();
            if (NUM_WARMUP <= i) { // Throw away first NUM_WARMUP runs to exclude JVM warmup
                totalTime += (endTime - startTime);
            }
        }
        double averageRuntime = totalTime / (NUM_TESTS - NUM_WARMUP);
        
        String pos = "";
        if (startPos.equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")) {
        	pos = "Start";
        } else if (startPos.equals("4k3/p2n1pp1/3R1n2/4p3/2P2r2/P7/1PP5/2KR4 w Hk -")) {
        	pos = "Mid";
        } else {
        	pos = "End";
        }
        
        System.out.println("Start position: " + pos + ", Cutoff: " + cutoff + " is: " + averageRuntime);
    }
    
    public WriteupTest(int cutoff) {
        setupWhitePlayer(new ParallelSearcher<ArrayMove, ArrayBoard>(), 5, cutoff);
        setupBlackPlayer(new ParallelSearcher<ArrayMove, ArrayBoard>(), 5, cutoff);
    }
    
    public void play(String startPos) {
       this.board = ArrayBoard.FACTORY.create().init(startPos);
       Searcher<ArrayMove, ArrayBoard> currentPlayer = this.blackPlayer;
       
       int turn = 0;
       
       /* Note that this code does NOT check for stalemate... */
       while ((!board.inCheck() || board.generateMoves().size() > 0) && turn < 5) {
           currentPlayer = currentPlayer.equals(this.whitePlayer) ? this.blackPlayer : this.whitePlayer;
           //System.out.printf("%3d: " + board.fen() + "\n", turn);
           this.board.applyMove(currentPlayer.getBestMove(board, 1000, 1000));
           turn++;
       }
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
