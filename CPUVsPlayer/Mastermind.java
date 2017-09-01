import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Mastermind extends JFrame implements ActionListener {
    /**
     * The number of blocks that we want to guess the position for
     */
    private int width;
    /**
     * The number of allowable guesses
     */
    private int height;
    /**
     * The total number of colour options
     */
    private int numColors;
    /**
     * The number of guesses made so far
     */
    private int numGuesses;
    /**
     * The coloured pegs which display the computer guesses
     */
    private JButton[][] colouredPegs;
    /**
     * The white pegs which whether coloured pegs are the correct colour in the wrong position
     */
    private JButton[][] whites;
    /**
     * The white pegs which whether coloured pegs are the correct colour in the correct position
     */
    private JButton[][] blacks;
    /**
     * Represents the choice that the user makes
     */
    private JButton[] userChoice;
    /**
     * Stores the current state at each guess
     */
    private int state[][];
    /**
     * The underlying integers which represent the choice of the user
     */
    private int[] hiddenGuess;
    /**
     * The button that the user presses to confirm the guess
     */
    private JButton guess;
    /**
     * Panel for laying out coloured pegs
     */
    private JPanel colouredPanel = new JPanel();
    /**
     * Panel for laying out white pegs
     */
    private JPanel whitesPanel = new JPanel();
    /**
     * Panel for laying out black pegs
     */
    private JPanel blacksPanel = new JPanel();
    /**
     * Panel for laying out the user choice
     */
    private JPanel userChoicePanel = new JPanel();
    /**
     * The set of all remaining possibilities that the users choice could be
     */
    private Set<int[]> remainingPossibilities;
    /**
     * The list of all possible valid combinations of black and white pegs
     */
    private List<int[]> outcomes;
    /**
     * The number of whites on the previous guess
     */
    private int lastWhites;
    /**
     * The number of blacks on the previous guess
     */
    private int lastBlacks;

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
     * Returns a different Colour object depending on the integer passed in
     *
     * @param i     An integer
     * @return      A Color object
     */
    private static Color choose(int i) {
        switch (i) {
            case 0:
                return Color.red;
            case 1:
                return Color.green;
            case 2:
                return Color.blue;
            case 3:
                return Color.cyan;
            case 4:
                return Color.orange;
            default:
                return Color.yellow;
        }
    }

    /**
     * Generates a list of all possible combinations of black and white pegs
     * 
     * @param width   The number of holes  
     * @return       The list of possible combinations of blacks and white pegs 
     */
    private static List<int[]> generateAllOutcomes(int width) {
        List<int[]> outcomes = new ArrayList<>();
        int[] whiteBlackCombination = new int[2];

        // want to iterate only the up to maximum number of possible outcomes which is width * width
        int i = 0;
        while (i <= width * width) {
            // create a copy of the combination so that we aren't adding references to the same object multiple times
            int[] copyOfWBCombination = new int[whiteBlackCombination.length];
            System.arraycopy(whiteBlackCombination, 0, copyOfWBCombination, 0, whiteBlackCombination.length);

            // only want to add the combination if the sum doesn't exceed the number of holes
            if (whiteBlackCombination[0] + whiteBlackCombination[1] <= width) {
                if (!(whiteBlackCombination[0] == 3 && whiteBlackCombination[1] == 1)) {
                    outcomes.add(copyOfWBCombination);
                }
            }

            // increment the last element by 1
            ++whiteBlackCombination[1];

            // don't increment the loop counter if the last element goes beyond the number of holes (don't want to waste iteration)
            if (whiteBlackCombination[1] != width + 1) {
                ++i;
            }

            // if the last element exceeded width, then reset to 0 and increment first element
            if (whiteBlackCombination[1] > width) {
                whiteBlackCombination[1] = 0;
                ++whiteBlackCombination[0];
            }
        }

        return outcomes;
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
     * Creates a new Mastermind object
     *
     * @param h     The maximum number of guesses that are allowed
     * @param w     The number of coloured blocks that must be guessed
     * @param c     The number of colours to use in the program
     */
    public Mastermind(int h, int w, int c) {
        width = w;
        height = h;
        numColors = c;
        numGuesses = 0;
        hiddenGuess = new int[width];
        guess = new JButton("Computer Guess");
        state = new int[height][width];
        colouredPegs = new JButton[height][width];
        whites = new JButton[height][width];
        blacks = new JButton[height][width];
        userChoice = new JButton[width];

        // generate the set of all possible combinations of colours
        remainingPossibilities = generateAllPossibilities(width, numColors);

        outcomes = generateAllOutcomes(width);

        // want the all of the panels to have matching numbers of rows and columns in grid layout
        colouredPanel.setLayout(new GridLayout(height, width));
        blacksPanel.setLayout(new GridLayout(height, width));
        whitesPanel.setLayout(new GridLayout(height, width));
        // there is only one user choice so we leave it with only 1 row
        userChoicePanel.setLayout(new GridLayout(1, width));

        // create a border around the edge of the panels
        colouredPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        whitesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        blacksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        /**
         * Inner class that enables the user choices to be changed on click
         */
        class Bing implements ActionListener {
            private int x;

            private Bing(int x) {
                this.x = x;
            }

            public void actionPerformed(ActionEvent event) {
                if (numGuesses == 0) {
                    hiddenGuess[x] = (hiddenGuess[x] + 1) % numColors;

                    ((JButton) event.getSource()).setBackground(choose(hiddenGuess[x]));
                }
            }
        }

        // create all of the user choice buttons
        for (int k = 0; k < width; ++k) {
            userChoice[k] = new JButton();
            userChoice[k].setVisible(true);
            userChoice[k].addActionListener(new Bing(k));
            userChoicePanel.add(userChoice[k]);
            hiddenGuess[k] = 0;
            userChoice[k].setBackground(choose(hiddenGuess[k]));
        }

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                // initialise the state if all coloured pegs to 0
                state[i][j] = 0;
                // create each of the coloured pegs, add the ActionListeners to them and set the colour to colour 0
                colouredPegs[i][j] = new JButton();
                colouredPegs[i][j].setBackground(choose(state[i][j]));

                // create all of the black and white pegs, but make invisible for now
                whites[i][j] = new JButton();
                whites[i][j].setVisible(false);
                whites[i][j].setEnabled(false);
                whites[i][j].setBackground(Color.white);
                blacks[i][j] = new JButton();
                blacks[i][j].setVisible(false);
                blacks[i][j].setEnabled(false);
                blacks[i][j].setBackground(Color.black);

                // add the button to their respective panels
                colouredPanel.add(colouredPegs[i][j]);
                whitesPanel.add(whites[i][j]);
                blacksPanel.add(blacks[i][j]);

                // set all coloured pegs to invisible initially
                colouredPegs[i][j].setVisible(false);
                // set all colored pegs to disabled as they are only used to display the computer's guesses
                colouredPegs[i][j].setEnabled(false);
            }
        }

        // use a border layout to structure the program as a whole
        setLayout(new BorderLayout());

        // put the black pegs on the left, coloured pegs in the centre and the white pegs on the right
        add(blacksPanel, "West");
        add(colouredPanel, "Center");
        add(whitesPanel, "East");

        // create a new panel with flow layout and put it along the bottom containing the guess button
        JPanel guessPanel = new JPanel();
        guessPanel.setLayout(new FlowLayout());
        guessPanel.add(guess);
        add(guessPanel, "South");

        // create a panel for containing the black/white labels and the answer and put it at the top
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        topPanel.setLayout(new GridLayout(1, 3)); // want it to be 1 row, 3 cols
        topPanel.add(new JLabel("Blacks", JLabel.CENTER));
        topPanel.add(userChoicePanel);
        topPanel.add(new JLabel("Whites", JLabel.CENTER));
        add(topPanel, "North");

        // set the title and initial dimensions of the window
        setTitle("Mastermind");
        setMinimumSize(new Dimension(width * 50, height * 50));
        pack();
        setVisible(true);

        guess.addActionListener(this);

        // let the program exit if user tries to quit
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Makes a guess by choosing the first item from the hashset that's available
     *
     * @return      Returns the next guess to make
     */
    private int[] getRandomGuess() {
        Iterator<int[]> iterator = remainingPossibilities.iterator();
        int[] nextGuess = new int[width];

        if (iterator.hasNext()) {
            nextGuess = iterator.next();
        }

        return nextGuess;
    }

    /**
     * Makes an informed choice about which guess to make next by using minmax technique
     *
     * @return      Returns the next guess to make
     */
    private int[] getMinMaxedGuess() {
        int min = Integer.MAX_VALUE;
        int[] minCombination = new int[width];
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
     * Make a new guess based on the information from previous turn
     */
    private void makeGuess() {
        // if we're on the first guess just use 0,0,1,1 (or if longer than four holes just keep adding 1's)
        if (numGuesses == 0) {
            for (int i = 0; i < state[numGuesses].length; ++i) {
                state[numGuesses][i] = i < 2 ? 0 : 1;
                colouredPegs[numGuesses][i].setBackground(choose(state[numGuesses][i]));
            }
        }
        else {
            // remove any potential guesses that cannot be correct answer
            removeImpossibleGuesses();
            // get the values for next guess
            state[numGuesses] = getMinMaxedGuess();
            // update the colours of the buttons corresponding to the guess
            for (int i = 0; i < state[numGuesses].length; ++i) {
                colouredPegs[numGuesses][i].setBackground(choose(state[numGuesses][i]));
            }
        }
    }

    /**
     * Presents a confirmation dialog that asks whether the user wishes to restart the game
     *
     * @param title     The title of the dialog
     * @param message   The main message in the body of the dialog
     */
    private void endGamePrompt(String title, String message) {
        // display the confirmation dialog with the specified message
        int n = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
        // if the user decided to quit exit the program
        if (n == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
        // otherwise free up resources and start the game again
        else {
            dispose();
            new Mastermind(height, width, numColors);
        }
    }

    /**
     * Called when the guess button is clicked
     * Checks the number of coloured pegs in the right positions and updates the ui accordingly
     *
     * @param e     The ActionEvent triggered when the guess button is clicked
     */
    public void actionPerformed(ActionEvent e) {
        // disable the user choice buttons once the computer guess is first pressed
        if (numGuesses == 0) {
            for (JButton button : userChoice) {
                button.setEnabled(false);
            }
        }

        // let the computer make a guess before updating the UI
        makeGuess();

        // get the number of white and black pegs for the last guess
        lastWhites = whites(state[numGuesses], hiddenGuess);
        lastBlacks = blacks(state[numGuesses], hiddenGuess);

        // if the number of black pegs is the same as the total number of pegs, then the game is won
        if (lastBlacks == width) {
            // set visible the appropriate number of black pegs on the current row
            for (int i = 0; i < lastBlacks; ++i) {
                blacks[numGuesses][i].setVisible(true);
            }
            // set visible all of the coloured pegs on the current row
            for (int i = 0; i < width; ++i) {
                colouredPegs[numGuesses][i].setVisible(true);
            }
            // display the end of game prompts
            endGamePrompt("The computer won!", "The computer won! Would you like to play again?");
        }
        // id we haven't exceeded the maximum number of guesses
        if (numGuesses < height) {
            // set visible the appropriate number of white pegs on the current row
            for (int i = 0; i < lastWhites; ++i) {
                whites[numGuesses][i].setVisible(true);
            }
            // set visible the appropriate number of black pegs on the current row
            for (int i = 0; i < lastBlacks; ++i) {
                blacks[numGuesses][i].setVisible(true);
            }

            // set visible all of the coloured pegs on the current row
            for (int i = 0; i < width; ++i) {
                colouredPegs[numGuesses][i].setVisible(true);
            }

            // increment the number of guesses
            ++numGuesses;

            // check if we have exceeded the max number of guesses
            if (numGuesses >= height) {
                // display the end of game prompts
                endGamePrompt("The computer lost!", "The computer lost! Would you like to play again?");
            }
        }
    }

    /**
     * Main function
     *
     * @param args      optional CLI args (unused)
     */
    public static void main(String[] args) {
        try {
            // set cross-platform Java L&F so that the blocks are properly coloured on all platforms
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException|ClassNotFoundException|InstantiationException|IllegalAccessException e) {
            System.err.println(e.toString());
        }

        // create a new Mastermind object
        new Mastermind(10, 4, 6);
    }
}
