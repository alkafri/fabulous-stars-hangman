package yh.fabulousstars.hangman;

import java.util.ArrayList;
import java.util.Scanner;

public class GameLogic  {
    public Char newLetter;
    public int guessedLetterPosition;
    public List<Char> gussedLetters; // = new ArrayList<>;
    public List<Char> missedLetters; // = new ArrayList<>;

    /**
     * SetPlayerTurn is a method to give the the play turn for specific player according to games rules.
     * @param playerIndex is the number of the player in the player list
     */
    public SetPlayerTurn (int playerIndex){  // NOT FINISH ..... i think this method can be delete! there is no use for it.

        return true;
    }

    /**
     * GetGuess is a method to get a letter from player as a new guess for new letter
     * @return this method return the letter that the player entered
     */
    public Char GetGuess (){  //This method will ask for nee letter from the palyer and returns this letter.
        Scanner scanner = new Scanner(System.in);
        this.newLetter = scanner.nextLine(); // assign the letter from the user to the paramaeter.

        return this.newLetter;
    }

    /**
     * CompareGuess thakes the player input and compare it to the hidden word, if the letter exists
     * then it returns the position of the letter in the hidden word
     * @param guessLetter the letter from the user
     * @param currentWord the hidden word
     * @return true and assigns the the letter position to the paramater or return false in case its wrong guess
     */
    public boolean CompareGuess (Char guessLetter, List<Char> currentWord){ //This method compares between the new letter and check if its exist in the hidden word
        boolean res = currentWord.contains(guessLetter);
        if (res == true) {
            this.guessedLetterPosition = currentWord.indexOf(guessLetter)+1; // this line gets the letter position in the word, +1 because list index starts with 0
            return true;
        }else{
            return false;
        }
    }

    /**
     * this method is to add new line to the hangman
     * @param playerIndex is the palyer index in the palyer list
     * @return
     */
    public boolean DrawHangMan (int playerIndex){ //this method should be related to the GUI part, its better to be buit thier.
        return true;
    }

    /**
     * This method is to check if the player is hanged already or not
     * i assum that it needs 10 steps to hang a player
     * if more or less then the value 10 needs to be modified
     * @param missedGuessList this is the list of total wrong guess
     * @return true in case the worng guess exceded the maximum allowed, returns false if still more guesses available.
     */
    public boolean CheckHanged (List<Char> missedGuessList){
        if missedGuessList.size >= 10 { // missedGuessList is the player list of missed guess. The number (10) is according to the steps of hangman and can be changed accordingly
            return true // return true if the player reached the maximum allowed wrong guess
        }else{
            return false
        }

        /**
         * this methiod prints out the game instruction
         */
        public static void GameInstruction(){
            System.out.println('*******************************************');
            System.out.println('WELCOME TO FABULOUS STARS HANGMAN GAME');
            System.out.println('This game is between multiplayers');
            System.out.println('Each player will take his turn to choose \n a word that need to be guessed by other players');
            System.out.println('Once you get hanged you will loose');
            System.out.println('The winner is the LAST MAN STANDING');
            System.out.println(*******************************************);
        }

        /**
         * this method to announce the winner of the game
         */
        public String resutl () { // need more information from the class PLAYERS to see the players list structure and decide the result

        }
}