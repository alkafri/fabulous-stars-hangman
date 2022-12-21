package yh.fabulousstars.hangman.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class GameLogics {

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
     *
     * @param gameState
     * @return
     */
    public static List<EventEnvelope> start(GameState gameState) {
        var startedEvent = new GameEvent(GameEventType.Game_started);
        var requestWordEvent = new GameEvent(GameEventType.Request_word)
                .put("minLength", String.valueOf(Config.MIN_WORD_LENGTH))
                .put("maxLength", String.valueOf(Config.MAX_WORD_LENGTH));
        // players
        var players = gameState.getPlayerEntries();
        // send events to participants
        var events = new ArrayList<EventEnvelope>();
        for (var player : players) {
            // start
            events.add(new EventEnvelope(player.getKey(), startedEvent));
            // word
            events.add(new EventEnvelope(player.getKey(), requestWordEvent));
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
        var events = new ArrayList<EventEnvelope>();
        var letter = guess.toUpperCase().charAt(0);
        var player = gameState.getPlayState(clientId);
        var opponent = gameState.getPlayState(player.getOpponentId());
        boolean foundMatch = false;
        int stateChange = 0;

        // process the guess.
        var word = player.getCurrentWord();
        var correct = player.getCorrectGuesses();
        var wrong = player.getWrongGuesses();
        // Check if the user input is in the word.
        for (int i = 0; i < correct.length; i++) {
            if (word[i] == letter) {
                // If a match is found, replace '*' with correct letter in array.
                correct[i] = letter;
                foundMatch = true;
                stateChange++;
            }
        }

        if (foundMatch) { // damage opponent
            opponent.addDamage();
            stateChange++;
            // finished?
            int counter = 0;
            for (int i = 0; i < correct.length; i++) {
                if (correct[i] != '*') {
                    counter++;
                }
            }
            if (counter == word.length) {
                player.setPlayerState(PlayState.FINISHED);
                stateChange++;
            }
        } else {
            // Damage player
            wrong.add(letter);
            player.addDamage();
            stateChange++;
        }

        // correct/incorrect guess
        events.add(new EventEnvelope(clientId, new GameEvent(GameEventType.Guess_result)
                .put("correct", foundMatch ? "1" : "0")
                .put("finished", player.getPlayState()==PlayState.FINISHED?"1":"0")));

        if (stateChange != 0) {
            addPlayerStates(gameState, events);
        }

        // get some numbers
        int numAlive = 0;
        int numPlaying = 0;
        String winner = null;
        for (var pState : gameState.getPlayerStates()) {
            var state = pState.getPlayState();
            if (state != PlayState.DEAD) {
                numAlive++;
                winner = pState.getClientId();
                if (state == PlayState.PLAY) {
                    numPlaying++;
                }
            }
        }

        if (numAlive < 2) {
            // todo: winner (alive) or 1st(dead but least damage), 2nd, .. etc
            if (winner != null) {
                events.add(new EventEnvelope(winner, new GameEvent(GameEventType.Winner)));
            }
            for (var pState : gameState.getPlayerStates()) {
                if (!pState.getClientId().equals(winner)) {
                    events.add(new EventEnvelope(pState.getClientId(), new GameEvent(GameEventType.Loser)));
                }
            }
            gameState.setEnded();

        } else if (numPlaying == 0) {
            // new round
            gameState.getWordBucket().clear();
            var requestWordEvent = new GameEvent(GameEventType.Request_word)
                    .put("minLength", String.valueOf(Config.MIN_WORD_LENGTH))
                    .put("maxLength", String.valueOf(Config.MAX_WORD_LENGTH));
            for (var pState : gameState.getPlayerStates()) {
                if (pState.getPlayState() != PlayState.DEAD) {
                    pState.setPlayerState(PlayState.PLAY);
                    events.add(new EventEnvelope(pState.getClientId(), requestWordEvent));
                }
            }
        }

        return events;
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
        var events = new ArrayList<EventEnvelope>();
        // check length and re request if needed
        var length = word.length();
        if (length < Config.MIN_WORD_LENGTH || length > Config.MAX_WORD_LENGTH) {
            var evt = new GameEvent(GameEventType.Request_word)
                    .put("minLength", String.valueOf(Config.MIN_WORD_LENGTH))
                    .put("maxLength", String.valueOf(Config.MAX_WORD_LENGTH));
            return List.of(
                    new EventEnvelope(clientId, evt)
            );
        }
        // set player word
        gameState.setPlayerWord(clientId, word);

        // if all words set, choose words for players
        var living = gameState.getLivingPlayerStates();
        var bucket = gameState.getWordBucket();
        if (living.size() == bucket.size()) {
            var players = gameState.getLivingPlayerStates();
            chooseWords(bucket, living);
            addPlayerStates(gameState, events);
            for (var player : players) {
                events.add(new EventEnvelope(player.getClientId(),
                        new GameEvent(GameEventType.Request_guess)));
            }
        }
        return events;
    }

    private static void addPlayerStates(GameState gameState, List<EventEnvelope> target) {
        var players = gameState.getPlayerStates();
        var event = new GameEvent(GameEventType.Play_state)
                .setPayload(players);
        for (var player : players) {
            target.add(new EventEnvelope(player.getClientId(), event));
        }
    }
}
