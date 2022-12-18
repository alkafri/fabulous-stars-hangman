package yh.fabulousstars.server.game;

import java.util.ArrayList;
import java.util.List;

public final class GameLogics {

    private static int MAX_DAMAGE = 11;
    /**
     *
     * @param gameState
     * @param clientId
     * @param guess
     * @return changed states
     */
    public static List<PlayState> makeGuess(GameState gameState, String clientId, String guess) {

        var sendStates = new ArrayList<PlayState>();
        var letter = guess.toUpperCase().charAt(0);
        var player = gameState.getPlayState(clientId);
        var opponent = gameState.getPlayState(player.getOpponentId());

        boolean foundMatch = false;

        // process the user input here...
        char[] letters = player.getCurrentWord().toCharArray();

        /*
        if (guessWord.length() > 1) {
            if (guessWord.equals(userWord)) {

                counter = wordCount;
                for (int i = 0; i < letters.length; i++) {

                    if (letters[i] == guessWord.charAt(i)) {
                        // If a match is found, print the index and character
                        System.out.println("Found a match at index " + i + ": " + letters[i]);
                        correctLetter[i] = letters[i];

                    }
                }
            }

        } else {
*/
        var correct = player.getCorrectGuesses();
        var wrong = player.getWrongGuesses();
            // Check if the user input is in the array of letters
            for (int i = 0; i < letters.length; i++) {

                if (letters[i] == letter) {
                    // If a match is found, print the index and character
                    // System.out.println("Found a match at index " + i + ": " + letters[i]);
                    //replace '*' with correct letter
                    correct.set(i, letter);
                    foundMatch = true;

                }
            }
            // Check if the user has lost
            if (!foundMatch) {
                wrong.add(letter);
                if (player.getTotalDamage() >= MAX_DAMAGE) {
                    player.setPlayerState(PlayState.DEAD);
                    sendStates.add(player);
                }
            }

        //}
        int counter = 0;
        if (foundMatch) {
            for (int i = 0; i < letters.length; i++) {
                if (!correct.get(i).equals('*')) {
                    counter++;
                }
            }
            opponent.addDamage(); // damage opponent by guessing correct
            if (opponent.getTotalDamage() >= MAX_DAMAGE) {
                opponent.setPlayerState(PlayState.DEAD);
            }
            sendStates.add(opponent);
        }

        // Check if the player has won
        if (counter == letters.length) {
            player.setPlayerState(PlayState.WON);

            // todo: player guessed word
            // Continue until all players have guessed or died
            // If more than one player left, draw new round for remaining.

            if(!sendStates.contains(player)) { sendStates.add(player); }
        }

        return sendStates;
    }

    /**
     *
     * @param gameState
     * @param clientId
     * @param word
     * @return changed states
     */
    public static List<PlayState> setWord(GameState gameState, String clientId, String word) {
        gameState.setPlayerWord(clientId, word);
        if(gameState.hasWords()) {
            gameState.chooseWords();
            return gameState.getPlayers();
        }
        return null;
    }
}
