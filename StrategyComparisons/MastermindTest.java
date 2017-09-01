import java.util.Set;

/**
 * Test class for calculating worst and average case performance of Mastermind strategies
 */
public class MastermindTest {
    public static void main(String[] args) {
        // run the strategy that uses the first available element of the set as the next guess
        System.out.println("==== Using \"first available\" Strategy ====");
        runGameSims(100, Strategy.RANDOM);

        System.out.println();

        // runs the strategy that uses minimax technique to determine the next guess
        System.out.println("==== Using Minimax (Knuth-based) Strategy ====");
        System.out.println("WARNING: May take a long time to complete.");
        runGameSims(100, Strategy.MINIMAX);
    }

    /**
     * Run the game simulation set a given number of times for each strategy
     *
     * Example: If we have 4 holes, 6 colours at 100 runs a total of 6^4 * 100 = 129600 games would be run
     *
     * @param holes     The number of holes that pegs can be placed in
     * @param colours   The number of colours that can be guessed
     * @param runs      The total number of simulation runs
     * @param strategy  The strategy type to use
     */
    private static void runGameSims(int holes, int colours, int runs, Strategy strategy) {
        int maxGuesses = 0;
        int totalOfGuesses = 0;
        int numOfRounds = 0;

        // play the game runs times for every single possible solution
        for (int i = 0; i < runs; ++i) {
            System.out.print("Running game set " + (i+1) + " of " + runs + "...");

            // create a new Mastermind object
            Mastermind mastermind = new Mastermind(holes, colours, strategy);

            // get a list of all possible options
            Set<int[]> allPossibilities = mastermind.getAllPossibilities();

            // for every possible combination see how many guesses it takes to reach solution
            for (int[] possibility : allPossibilities) {
                // set the hidden guess to the current possible combination
                mastermind.setHiddenGuess(possibility);

                // record the number of guesses to reach right answer for this possibility
                int currentGuesses = 0;

                // keep guessing until we get the right answer
                do {
                    ++currentGuesses;
                } while (!mastermind.guess());

                // add the number of guesses to our list of all guesses made
                totalOfGuesses += currentGuesses;

                // if we exceeded the previous maximum number of guesses update to the new one
                if (currentGuesses > maxGuesses) {
                    maxGuesses = currentGuesses;
                }

                // increment number of rounds we have played
                ++numOfRounds;
            }
            System.out.println("done!");
        }

        // get the average of all rounds of guesses
        double averageGuesses = ((double)totalOfGuesses)/numOfRounds;

        // print out our calculated values
        System.out.println("The average number of guesses is: " + averageGuesses);
        System.out.println("The maximum number of guesses is: " + maxGuesses);
    }

    /**
     * Default version of runGameSims for 6 colours and 4 holes
     *
     * @param runs      The total number of simulation runs
     * @param strategy  The strategy type to use
     */
    private static void runGameSims(int runs, Strategy strategy) {
        runGameSims(4, 6, runs, strategy);
    }
}
