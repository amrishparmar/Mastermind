import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Mastermind game
 */
public class Mastermind {
    /**
     * The number of holes that colours can be arranged into
     */
    private int holes;
    /**
     * The number of different colours available to choose from
     */
    private int colours;
    /**
     * The set of all possible combinations of colours in holes
     */
    private Set<int[]> allPossibilities;
    /**
     * The set of possible combinations that the solution could possibly be
     */
    private Set<int[]> remainingPossibilities;
    /**
     * The list of all possible valid combinations of black and white pegs
     */
    private List<int[]> outcomes;
    /**
     * The history of all guesses made in the current game
     */
    private int state[][];
    /**
     * The number of guesses made so far
     */
    private int numGuesses;
    /**
     * The number of white pegs revealed on the last guess
     */
    private int lastWhites;
    /**
     * The number of black pegs revealed on the last guess
     */
    private int lastBlacks;
    /**
     * The hidden solution that the computer is trying to guess
     */
    private int[] hiddenGuess;
    /**
     * The strategy to use choose the next guess
     */
    private Strategy strategy;

    /**
     * Constructor
     *
     * @param holes     The number of holes to arrange the colours in
     * @param colours   The number of colours available for selection
     * @param strategy  The strategy to use to select the next guess
     */
    public Mastermind(int holes, int colours, Strategy strategy) {
        this.holes = holes;
        this.colours = colours;
        allPossibilities = generateAllPossibilities(holes, colours);
        remainingPossibilities = new HashSet<>(allPossibilities);
        state = new int[20][holes]; // use arbitrary size of 20, all (non-trivial) strategies should be less than this
        numGuesses = 0;
        this.strategy = strategy;
        if (strategy == Strategy.MINIMAX) {
            outcomes = generateAllOutcomes(holes);
        }
    }

    /**
     * Generates all the possible ways of organising a given number of colours into a given number of holes
     *
     * @param holes     The number of holes to arrange the colours in
     * @param colours   The number of colours available for selection
     * @return          The set of all possible colour combinations
     */
    private static Set<int[]> generateAllPossibilities(int holes, int colours) {
        Set<int[]> allPossibilities = new HashSet<>();
        int[] possibility = new int[holes];

        // want to iterate colours to the power of holes times
        for (int i = 0; i < Math.pow(colours, holes); ++i) {

            // create a copy of the possibility so that we aren't adding references to the same object multiple times
            int[] copyOfPoss = new int[possibility.length];
            System.arraycopy(possibility, 0, copyOfPoss, 0, possibility.length);

            // add the copy of the current possibility
            allPossibilities.add(copyOfPoss);

            // increment the last element by 1
            ++possibility[possibility.length - 1];

            // iterate over each "column" from right to left
            for (int j = holes - 1; j > 0; --j) {
                // if we exceed our numeric base set it back to zero and increment the column to the left
                if (possibility[j] == colours) {
                    possibility[j] = 0;
                    ++possibility[j - 1];
                }
            }
        }
        return allPossibilities;
    }

    /**
     * Generates a list of all possible combinations of black and white pegs
     *
     * @param holes   The number of holes
     * @return       The list of possible combinations of blacks and white pegs
     */
    private static List<int[]> generateAllOutcomes(int holes) {
        List<int[]> outcomes = new ArrayList<>();
        int[] whiteBlackCombination = new int[2];

        // want to iterate only the up to maximum number of possible outcomes which is holes * holes
        int i = 0;
        while (i <= holes * holes) {
            // create a copy of the combination so that we aren't adding references to the same object multiple times
            int[] copyOfWBCombination = new int[whiteBlackCombination.length];
            System.arraycopy(whiteBlackCombination, 0, copyOfWBCombination, 0, whiteBlackCombination.length);

            // only want to add the combination if the sum doesn't exceed the number of holes
            if (whiteBlackCombination[0] + whiteBlackCombination[1] <= holes) {
                // impossible to have 3 blacks and 1 white, so don't want that
                if (!(whiteBlackCombination[0] == 3 && whiteBlackCombination[1] == 1)) {
                    outcomes.add(copyOfWBCombination);
                }
            }

            // increment the last element by 1
            ++whiteBlackCombination[1];

            // don't increment the loop counter if the last element goes beyond the number of holes (don't want to waste iteration)
            if (whiteBlackCombination[1] != holes + 1) {
                ++i;
            }

            // if the last element exceeded width, then reset to 0 and increment first element
            if (whiteBlackCombination[1] > holes) {
                whiteBlackCombination[1] = 0;
                ++whiteBlackCombination[0];
            }
        }

        return outcomes;
    }

    /**
     * Returns the number of guessed colours that are the correct colours in the correct positions
     *
     * @param one   The guess
     * @param two   The hidden answer
     * @return      The number of colours in the correct position
     */
    private static int blacks(int[] one, int[] two) {
        int val = 0;
        // iterate over the arrays
        for (int i = 0; i < one.length; ++i) {
            // if they have the same value at the same point in the array increment the val counter
            if (one[i] == two[i]) {
                ++val;
            }
        }
        return val;
    }

    /**
     * Returns the number of guessed colours that are the correct colours in the incorrect positions
     *
     * @param one   The guess
     * @param two   The hidden answer
     * @return      The number of correct colours in the wrong positions
     */
    private static int whites(int[] one, int[] two) {
        int val = 0;
        int[] oneA = new int[one.length];
        int[] twoA = new int[two.length];

        // create a copy of the two input arrays
        System.arraycopy(one, 0, oneA, 0, one.length);
        System.arraycopy(two, 0, twoA, 0, two.length);

        // check each of the items in corresponding positions
        for (int i = 0; i < one.length; ++i) {
            // if they are the same, i.e. in the correct position
            if (oneA[i] == twoA[i]) {
                // mark as dealt with (since blacks() handles that) by subtracting the current iteration and an arbitrary sufficiently different value from each
                oneA[i] = 0 - i - 10;
                twoA[i] = 0 - i - 20;
            }
        }

        // compare each item in oneA to every item in twoA
        for (int i = 0; i < one.length; ++i) {
            for (int j = 0; j < one.length; ++j) {
                // don't want to check items at the same index - we did that earlier - only ones in different positions
                if (i != j && oneA[i] == twoA[j]) {
                    // increment the counter of whites
                    ++val;
                    // mark each one as dealt with
                    oneA[i] = 0 - i - 10;
                    twoA[j] = 0 - j - 20;
                    break;
                }
            }
        }
        return val;
    }

    /**
     * Remove any guesses from our set of possibilities that cannot be the answer
     */
    private void removeImpossibleGuesses() {
        // use an iterator over the set since we want to remove some elements
        Iterator<int[]> iterator = remainingPossibilities.iterator();
        // iterate over the remaining possible combinations
        while(iterator.hasNext()) {
            int[] possibility = iterator.next();
            // don't want to keep the element if it cannot possibly be the correct answer
            if (whites(possibility, state[numGuesses - 1]) != lastWhites || blacks(possibility, state[numGuesses - 1]) != lastBlacks) {
                iterator.remove();
            }
        }
    }

    /**
     * Makes a guess by choosing the first item from the hashset that's available
     *
     * @return      Returns the next guess to make
     */
    private int[] getRandomGuess() {
        Iterator<int[]> iterator = remainingPossibilities.iterator();
        int[] nextGuess = new int[holes];

        if (iterator.hasNext()) {
            nextGuess = iterator.next();
        }

        return nextGuess;
    }

    /**
     * Makes an informed choice about which guess to make next by using minimax technique
     * Partially based on the C# implementation in <a href="http://stackoverflow.com/a/20418736">http://stackoverflow.com/a/20418736</a>
     *
     * @return      Returns the next guess to make
     */
    private int[] getMinMaxedGuess() {
        int min = Integer.MAX_VALUE;
        int[] minCombination = new int[holes];
        int[] whiteBlackCombination = new int[2];

        // check every possible guess against every other possible guess
        for (int[] guess : remainingPossibilities) {
            // the max number that would not be eliminated
            int max = 0;
            // want to compare for every possible outcome
            for (int[] outcome : outcomes) {
                // the current elimination count
                int count = 0;
                for (int[] solution : remainingPossibilities) {
                    // check the potential guess against other possibilities
                    whiteBlackCombination[0] = whites(guess, solution);
                    whiteBlackCombination[1] = blacks(guess, solution);
                    // if it would be not be eliminated then increment the count
                    if (Arrays.equals(whiteBlackCombination, outcome)) {
                        ++count;
                    }
                }
                // if we have found a higher count then set max to the count
                if (count > max) {
                    max = count;
                }
            }
            // if the max for the current guess is less than the min then let this be our guess for now
            if (max < min) {
                min = max;
                minCombination = guess;
            }
        }

        return minCombination;
    }

    /**
     * Makes the next guess, returns true if we have found the guess that we were looking for
     *
     * @return  True if we found the right solution, false otherwise
     */
    public boolean guess() {
        // if we're on the first guess just use 0,0,1,1 (or if longer than four holes just keep adding 1's)
        if (numGuesses == 0) {
            for (int i = 0; i < state[numGuesses].length; ++i) {
                state[numGuesses][i] = i < 2 ? 0 : 1;
            }
        }
        else {
            // remove any potential guesses that cannot be correct answer
            removeImpossibleGuesses();

            // get the next guess using the appropriate strategy
            if (strategy == Strategy.RANDOM) {
                state[numGuesses] = getRandomGuess();
            }
            else if (strategy == Strategy.MINIMAX) {
                state[numGuesses] = getMinMaxedGuess();
            }
        }

        // get the number of white and black pegs for the current guess
        lastWhites = whites(state[numGuesses], hiddenGuess);
        lastBlacks = blacks(state[numGuesses], hiddenGuess);

        // increment the number of guesses
        ++numGuesses;

        // if we guessed correctly then reinitialise and return true
        if (lastBlacks == holes) {
            reset();
            return true;
        }

        return false;
    }

    /**
     * Resets the main parts of the program back to it's initial state
     */
    private void reset() {
        state = new int[20][holes];
        remainingPossibilities = new HashSet<>(allPossibilities);
        numGuesses = 0;
    }

    /**
     * Getter for the list of all possible combinations of colours
     *
     * @return  The set of all possible combinations
     */
    public Set<int[]> getAllPossibilities() {
        return allPossibilities;
    }

    /**
     * Sets the hidden guess
     *
     * @param hiddenGuess   The hidden guess
     */
    public void setHiddenGuess(int[] hiddenGuess) {
        // ensure that the guess is the correct length
        if (hiddenGuess.length == holes) {
            // ensure that each of the integers is within the correct range
            for (int col : hiddenGuess) {
                if (col < 0 || col >= colours) {
                    throw new InvalidGuessException("Invalid integer in guess.");
                }
            }
            // assign the new guess since everything is valid
            this.hiddenGuess = hiddenGuess;
        }
        else {
            throw new InvalidGuessException("Length of hidden guess array invalid.");
        }
    }
}
