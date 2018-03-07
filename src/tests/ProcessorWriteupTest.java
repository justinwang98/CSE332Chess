package tests;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.bots.JamboreeSearcher;
import chess.bots.ParallelSearcher;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Searcher;

public class ProcessorWriteupTest {
    public Searcher<ArrayMove, ArrayBoard> whitePlayer;
    public Searcher<ArrayMove, ArrayBoard> blackPlayer;
    
    private ArrayBoard board;
    
    public static void main(String[] args) {
    	String beg = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    	String mid = "4k3/p2n1pp1/3R1n2/4p3/2P2r2/P7/1PP5/2KR4 w Hk -";
    	String end = "8/2k5/5n2/2P1R3/8/2P3p1/r4pK1/8 w H -";
    	
    	search(beg, 8, run(beg, 4), 0);
    	search(mid, 8, run(mid, 4), 0);
    	search(end, 8, run(end, 4), 0);
    }
    
    public static void search(String pos, int curr, double bestTime, int bestCores) {
    	if (curr <= 32) {
    		double time4 = run(pos, curr);	
	    	if (time4 < bestTime) {
	    		bestTime = time4;
	    		bestCores = curr;
	    		search(pos, curr + 4, bestTime, bestCores);
	    	} else {
	    		double time0 = run(pos, curr - 4);
		    	double time1 = run(pos, curr - 3);
		    	double time2 = run(pos, curr - 2);
		    	double time3 = run(pos, curr - 1);
		    	
		    	double best = Math.min(Math.min(time0, time1), Math.min(time2, time3));
		    	
		    	if (best == time0) {
		    		bestCores = curr - 4;
		    	} else if (best == time1) {
		    		bestCores = curr - 3;
		    	} else if (best == time2) {
		    		bestCores = curr - 2;
		    	} else {
		    		bestCores = curr - 1;
		    	}
		    	
		    	System.out.println("best cores is: " + bestCores);
	    	}
    	}
    }
    
    public static double run(String startPos, int processors) {
        final int NUM_TESTS = 5;
        final int NUM_WARMUP = 2;
        
        double totalTime = 0;
        for (int i = 0; i < NUM_TESTS; i++) {
            long startTime = System.currentTimeMillis();
            // Put whatever you want to time here .....
            
            ProcessorWriteupTest game = new ProcessorWriteupTest(processors);
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
        
        System.out.println("Start position: " + pos + ", Processor: " + processors + " is: " + averageRuntime);
        return averageRuntime;
    }
    
    
    public ProcessorWriteupTest(int processors) {
        setupWhitePlayer(new ParallelSearcher<ArrayMove, ArrayBoard>(processors), 5, 3);
        setupBlackPlayer(new ParallelSearcher<ArrayMove, ArrayBoard>(processors), 5, 3);
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
