package yh.fabulousstars.hangman.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class GameLogics {
    private static final int MIN_WORD_LENGTH = 4;
    private static final int MAX_WORD_LENGTH = 20;
    private static final int MAX_DAMAGE = 11;

    /**
     * Get random word from bucket for each player not belonging to player.
     */
    static void chooseWords(Map<String, String> wordBucket, List<PlayState> players) {
        // map of clientId -> word
        var opponentIds = new ArrayList<>(wordBucket.keySet());
        Collections.shuffle(opponentIds); // shuffle word order
        // choose word for each player
        for (var player : players) {
            // get first word not belonging to player
            for (int i = 0; i < opponentIds.size(); i++) {
                var opponentId = opponentIds.get(i);
                if (!opponentId.equals(player.getClientId())) {
                    // set word
                    player.setCurrentWord(opponentId, wordBucket.get(opponentId));
                    // remove used word from bucket
                    opponentIds.remove(opponentId);
                    break;
                }
            }
        }
    }

    /**
     * send start and request word to all
     * @param gameState
     * @return
     */
    public static List<EventEnvelope> start(GameState gameState) {
        var startedEvent = new EventObject("game_started");
        var wordEvent = new EventObject("request_word");
        wordEvent.put("minLength", String.valueOf(MIN_WORD_LENGTH));
        wordEvent.put("maxLength", String.valueOf(MAX_WORD_LENGTH));
        gameState.setStarted(true);
        // players
        var players = gameState.getPlayerEntries();
        // send events to participants
        var events = new ArrayList<EventEnvelope>();
        for (var player : players) {
            // start
            events.add(new EventEnvelope(player.getKey(),startedEvent));
            // word
            events.add(new EventEnvelope(player.getKey(),wordEvent));
        }
        return events;
    }

    /**
     * @param gameState Game state to operate on
     * @param clientId  Calling client
     * @param guess     Guessed letter
     * @return changed states
     */
    public static List<EventEnvelope> makeGuess(GameState gameState, String clientId, String guess) {

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
            player.setPlayerState(PlayState.FINISHED);

            // todo: player guessed word
            // Continue until all players have guessed or died
            // If more than one player left, draw new round for remaining.

            if (!sendStates.contains(player)) {
                sendStates.add(player);
            }
        }

        // TODO: return events here
        // if all finished or dead and at least 2 are alive, go to next round
        return sendStates;
    }

    /**
     * Set player word.
     * Guessing starts when all words are set.
     *
     * @param gameState Game state to operate on
     * @param clientId  Calling client
     * @param word      Word to set
     * @return changed states or null.
     */
    public static List<EventEnvelope> setWord(GameState gameState, String clientId, String word) {

        // check length and re request if needed
        var length = word.length();
        if(length < MIN_WORD_LENGTH || length > MAX_WORD_LENGTH) {
            var evt = new EventObject("request_word");
            evt.put("minLength", String.valueOf(MIN_WORD_LENGTH));
            evt.put("maxLength", String.valueOf(MAX_WORD_LENGTH));
            return List.of(
                    new EventEnvelope(clientId, evt)
            );
        }
        // set player word
        gameState.setPlayerWord(clientId, word);
        // if all words set, choose words for players
        if (gameState.hasWords()) {
            var players = gameState.getPlayerStates();
            chooseWords(gameState.getWordBucket(), players);
            var events = new ArrayList<EventEnvelope>();
            for (var player: players) {
                events.add(
                    new EventEnvelope(player.getClientId(), new EventObject("request_guess"))
                );
            }
            return events;
        }
        return List.of();
    }
}
