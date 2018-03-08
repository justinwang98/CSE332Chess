package tests;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.AlphaBetaSearcher;
import chess.bots.JamboreeSearcher;
import chess.bots.ParallelSearcher;
import chess.bots.SimpleSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Searcher;

public class FinalWriteupTest {
    public Searcher<ArrayMove, ArrayBoard> whitePlayer;
    public Searcher<ArrayMove, ArrayBoard> blackPlayer;
    
    private ArrayBoard board;
    
    public static void main(String[] args) {
    	String beg = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    	String mid = "4k3/p2n1pp1/3R1n2/4p3/2P2r2/P7/1PP5/2KR4 w Hk -";
    	String end = "8/2k5/5n2/2P1R3/8/2P3p1/r4pK1/8 w H -";
    	
    	Searcher<ArrayMove, ArrayBoard> sBeg = new SimpleSearcher<ArrayMove, ArrayBoard>();
    	Searcher<ArrayMove, ArrayBoard> sMid = new SimpleSearcher<ArrayMove, ArrayBoard>();
    	Searcher<ArrayMove, ArrayBoard> sEnd = new SimpleSearcher<ArrayMove, ArrayBoard>();
    	Searcher<ArrayMove, ArrayBoard> pBeg = new ParallelSearcher<ArrayMove, ArrayBoard>(32);
    	Searcher<ArrayMove, ArrayBoard> pMid = new ParallelSearcher<ArrayMove, ArrayBoard>(30);
    	Searcher<ArrayMove, ArrayBoard> pEnd = new ParallelSearcher<ArrayMove, ArrayBoard>(28);
    	Searcher<ArrayMove, ArrayBoard> aBeg = new AlphaBetaSearcher<ArrayMove, ArrayBoard>();
    	Searcher<ArrayMove, ArrayBoard> aMid = new AlphaBetaSearcher<ArrayMove, ArrayBoard>();
    	Searcher<ArrayMove, ArrayBoard> aEnd = new AlphaBetaSearcher<ArrayMove, ArrayBoard>();
    	Searcher<ArrayMove, ArrayBoard> jBeg = new JamboreeSearcher<ArrayMove, ArrayBoard>(28);
    	Searcher<ArrayMove, ArrayBoard> jMid = new JamboreeSearcher<ArrayMove, ArrayBoard>(29);
    	Searcher<ArrayMove, ArrayBoard> jEnd = new JamboreeSearcher<ArrayMove, ArrayBoard>(20);
    	
    	run(sBeg, beg);
    	run(sMid, mid);
    	run(sEnd, end);
    	run(pBeg, beg);
    	run(pMid, mid);
    	run(pEnd, end);
    	run(aBeg, beg);
    	run(aMid, mid);
    	run(aEnd, end);
    	run(jBeg, beg);
    	run(jMid, mid);
    	run(jEnd, end);
    }
    
    
    public static double run(Searcher type, String startPos) {
        final int NUM_TESTS = 20;
        final int NUM_WARMUP = 3;
        
        double totalTime = 0;
        for (int i = 0; i < NUM_TESTS; i++) {
            long startTime = System.currentTimeMillis();
            // Put whatever you want to time here .....
            
            FinalWriteupTest game = new FinalWriteupTest(type);
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
        
        System.out.println("Optimized Start position: " + pos + " time: " + averageRuntime);
        return averageRuntime;
    }
    
    
    public FinalWriteupTest(Searcher type) {
        setupWhitePlayer(type, 5, 3);
        setupBlackPlayer(type, 5, 3);
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
