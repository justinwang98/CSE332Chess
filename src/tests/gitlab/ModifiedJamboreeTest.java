package tests.gitlab;

import chess.board.ArrayBoard;
import chess.board.ArrayMove;
import chess.game.SimpleEvaluator;
import cse332.chess.interfaces.Move;
import cse332.chess.interfaces.Searcher;

import chess.bots.JamboreeSearcher;

import tests.TestsUtility;

public class ModifiedJamboreeTest extends SearcherTests {

    public static void main(String[] args) { new JamboreeTests().run(); }
    public static void init() { STUDENT = new JamboreeSearcher<ArrayMove, ArrayBoard>(); }

	
	@Override
	protected void run() {
        SHOW_TESTS = true;
        PRINT_TESTERR = true;

        ALLOWED_TIME = 20000;
        
        final int NUM_TESTS = 10;
        final int NUM_WARMUP = 3;
        
        double totalTime = 0;
        for (int i = 0; i < NUM_TESTS; i++) {
            long startTime = System.currentTimeMillis();
            // Put whatever you want to time here .....
            test("depth4", TestingInputs.FENS_TO_TEST.length);
            long endTime = System.currentTimeMillis();
            if (NUM_WARMUP <= i) { // Throw away first NUM_WARMUP runs to exclude JVM warmup
                totalTime += (endTime - startTime);
            }
        }
        double averageRuntime = totalTime / (NUM_TESTS - NUM_WARMUP);
        System.out.println("Average time is: " + averageRuntime);
            
//        test("depth2", TestingInputs.FENS_TO_TEST.length);
//        test("depth3", TestingInputs.FENS_TO_TEST.length);
//        test("depth4", TestingInputs.FENS_TO_TEST.length);

//        ALLOWED_TIME = 60000;
//        test("depth5", TestingInputs.FENS_TO_TEST.length);
		
		finish();
	} 
}
