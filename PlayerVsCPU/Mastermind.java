import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.util.Random;

public class Mastermind extends JFrame implements ActionListener {
    /**
     * The number of blocks that we want to guess the position for
     */
    int width;
    /**
     * The number of allowable guesses
     */
    int height;
    /**
     * The total number of colour options
     */
    int numColors;
    /**
     * The number of guesses made so far
     */
    int numGuesses;
    /**
     * The coloured pegs which the user can click on
     */
    JButton[][] colouredPegs;
    /**
     * The white pegs which whether coloured pegs are the correct colour in the wrong position
     */
    JButton[][] whites;
    /**
     * The white pegs which whether coloured pegs are the correct colour in the correct position
     */
    JButton[][] blacks;
    /**
     * Represents the randomly generated coloured pegs which the user is trying to guess
     */
    JButton[] computerGuess;
    /**
     * Stores the current state at each guess
     */
    int state[][];
    /**
     * The underlying integers which represent the the randomly generated coloured pegs which the user is trying to guess
     */
    int[] hiddenGuess;
    /**
     * The button that the user presses to confirm the guess
     */
    JButton guess = new JButton("Guess");
    /**
     * Panel for laying out coloured pegs
     */
    JPanel colouredPanel = new JPanel();
    /**
     * Panel for laying out white pegs
     */
    JPanel whitesPanel = new JPanel();
    /**
     * Panel for laying out black pegs
     */
    JPanel blacksPanel = new JPanel();
    /**
     * Panel for laying out the computer answer
     */
    JPanel computerGuessPanel = new JPanel();

    /**
     * Returns the number of guessed colours that are the correct colours in the correct positions
     *
     * @param one   The guess of the user
     * @param two   The hidden answer
     * @return      The number of colours in the correct position
     */
    static int blacks(int[] one, int[] two) {
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
     * @param one   The guess of the user
     * @param two   The hidden answer
     * @return      The number of correct colours in the wrong positions
     */
    static int whites(int[] one, int[] two) {
        int val = 0;
        int[] oneA = new int[one.length];
        int[] twoA = new int[one.length];

        // create a copy of the two input arrays
        for (int i = 0; i < one.length; ++i) {
            oneA[i] = one[i];
            twoA[i] = two[i];
        }

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
    static Color choose(int i) {
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
        state = new int[height][width];
        colouredPegs = new JButton[height][width];
        whites = new JButton[height][width];
        blacks = new JButton[height][width];
        computerGuess = new JButton[width];

        // want the all of the panels to have matching numbers of rows and columns in grid layout
        colouredPanel.setLayout(new GridLayout(height, width));
        blacksPanel.setLayout(new GridLayout(height, width));
        whitesPanel.setLayout(new GridLayout(height, width));
        // there is only one computer guess so we leave it with only 1 row
        computerGuessPanel.setLayout(new GridLayout(1, width));

        // create a border around the edge of the panels
        colouredPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        whitesPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        blacksPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        /**
         * Helper class that implements the ActionListener interface.
         * Used for updating the colour of coloured pegs when clicked on.
         */
        class Bing implements ActionListener {
            int x;
            int y;

            /**
             * Updates the colour of the button that called it
             *
             * @param e     The button clicked ActionEvent
             */
            public void actionPerformed(ActionEvent e) {
                state[x][y] = (state[x][y] + 1) % numColors;

                ((JButton) (e.getSource())).setBackground(choose(state[x][y]));
            }

            /**
             * Creates a new Bing object
             *
             * @param p     The row of the button to update
             * @param q     The column of the button to update
             */
            public Bing(int p, int q) {
                x = p;
                y = q;
            }
        }

        // assign a random colour for each colour peg in the answer
        Random rand = new Random();
        for (int k = 0; k < width; ++k) {
            computerGuess[k] = new JButton();
            computerGuess[k].setVisible(false);
            computerGuessPanel.add(computerGuess[k]);
            hiddenGuess[k] = rand.nextInt(numColors);// just for now
            computerGuess[k].setBackground(choose(hiddenGuess[k]));
        }

        for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j) {
                // initialise the state if all coloured pegs to 0
                state[i][j] = 0;
                // create each of the coloured pegs, add the ActionListeners to them and set the colour to colour 0
                colouredPegs[i][j] = new JButton();
                colouredPegs[i][j].addActionListener(new Bing(i, j));
                colouredPegs[i][j].setBackground(choose(state[i][j]));

                // create all of the black and white pegs, but make invisible for now
                whites[i][j] = new JButton();
                whites[i][j].setVisible(false);
                whites[i][j].setBackground(Color.white);
                blacks[i][j] = new JButton();
                blacks[i][j].setVisible(false);
                blacks[i][j].setBackground(Color.black);

                // add the button to their respective panels
                colouredPanel.add(colouredPegs[i][j]);
                whitesPanel.add(whites[i][j]);
                blacksPanel.add(blacks[i][j]);

                // only show the first line of coloured pegs on program start
                if (i > 0) {
                    colouredPegs[i][j].setVisible(false);
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
        topPanel.add(computerGuessPanel);
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
     * Presents a confirmation dialog that asks whether the user wishes to restart the game
     *
     * @param title     The title of the dialog
     * @param message   The main message in the body of the dialog
     */
    public void endGamePrompt(String title, String message) {
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
        // get the number of white and black pegs
        int whiteThings = whites(state[numGuesses], hiddenGuess);
        int blackThings = blacks(state[numGuesses], hiddenGuess);

        // disable the current row of coloured pegs
        for (int i = 0; i < width; ++i) {
            colouredPegs[numGuesses][i].setEnabled(false);
        }
        // if the number of black pegs is the same as the total number of pegs, then the game is won
        if (blackThings == width) {
            // set visible the appropriate number of black pegs on the current row
            for (int i = 0; i < blackThings; ++i) {
                blacks[numGuesses][i].setVisible(true);
            }
            // show that guess is correct to the user by displaying the original
            for (int i = 0; i < width; ++i) {
                computerGuess[i].setVisible(true);
            }
            // display the end of game prompts
            endGamePrompt("You've won!", "You've won! Would you like to play again?");
        }
        // id we haven't exceeded the maximum number of guesses
        if (numGuesses < height) {
            // set visible the appropriate number of white pegs on the current row
            for (int i = 0; i < whiteThings; ++i) {
                whites[numGuesses][i].setVisible(true);
            }
            // set visible the appropriate number of black pegs on the current row
            for (int i = 0; i < blackThings; ++i) {
                blacks[numGuesses][i].setVisible(true);
            }
            // increment the number of guesses and check again if we have exceeded the max number of guesses
            numGuesses++;
            if (numGuesses < height) {
                // set visible the appropriate number of coloured pegs on the current row
                for (int i = 0; i < width; ++i) {
                    colouredPegs[numGuesses][i].setVisible(true);
                }
            }
            else {
                // show the actual answer
                for (int i = 0; i < width; ++i) {
                    computerGuess[i].setVisible(true);
                }
                // display the end of game prompts
                endGamePrompt("You've lost!", "You've lost! Would you like to play again?");
            }
        }
    }

    /**
     * Main function
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
