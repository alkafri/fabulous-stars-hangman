package yh.fabulousstars.hangman;

import java.util.ArrayList;
import java.util.Scanner;

public class GameLogic  {
    public Char newLetter;
    public int guessedLetterPosition;
    public List<Char> gussedLetters; // = new ArrayList<>;
    public List<Char> missedLetters; // = new ArrayList<>;

    public SetPlayerTurn (int playerIndex){  // NOT FINISH ..... i think this method can be delete! there is no use for it.

        return true;
    }

    public Char GetGuess (){  //This method will ask for nee letter from the palyer and returns this letter.
        Scanner scanner = new Scanner(System.in);
        this.newLetter = scanner.nextLine();

        return this.newLetter;
    }

    public boolean CompareGuess (Char guessLetter, List<Char> currentWord){ //This method compares between the new letter and check if its exist in the hidden word
        boolean res = currentWord.contains(guessLetter);
        if (res == true) {
            this.guessedLetterPosition = currentWord.indexOf(guessLetter)+1; // this line gets the letter position in the word, +1 because list index starts with 0
            return true;
        }else{
            return false;
        }
    }

    public boolean DrawHangMan (int playerIndex){ //this method should be related to the GUI part, its better to be buit thier.
        return true;
    }

    public boolean CheckHanged (List<Char> missedGuessList){
        if missedGuessList.size >= 10 { // missedGuessList is the player list of missed guess. The number (10) is according to the steps of hangman and can be changed accordingly
            return true
        }else{
            return false
        }


        public static void GameInstruction(){
            System.out.println('*******************************************');
            System.out.println('WELCOME TO FABULOUS STARS HANGMAN GAME');
            System.out.println('This game is between multiplayers');
            System.out.println('Each player will take his turn to choose \n a word that need to be guessed by other players');
            System.out.println('Once you get hanged you will loose');
            System.out.println('The winner is the LAST MAN STANDING');
            System.out.println(*******************************************);
        }

        public String resutl () { // need more information from the class PLAYERS to see the players list structure and decide the result

        }
}