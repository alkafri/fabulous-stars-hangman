package yh.fabulousstars.hangman.game;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Event objects are sent to clients.
 */
public class GameEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1020304050L;
    /**
     * Various properties.
     */
    private final HashMap<String, String> properties;
    /**
     * Event name.
     */
    private GameEventType type;
    /**
     * Serializable payload.
     */
    private Object payload;

    /**
     * Construct event object.
     * @param type Event type.
     */
    public GameEvent(GameEventType type) {
        this.type = type;
        this.properties = new HashMap<>();
        this.payload = null;
    }

    public GameEvent(GameEventType type, Map<String, String> source) {
        this.type = type;
        this.properties = new HashMap<>(source);
        this.payload = null;
    }

    public GameEventType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public GameEvent setPayload(Object payload) {
        this.payload = payload;
        return this;
    }
    public String get(String key) {
        return properties.get(key);
    }

    public boolean contains(String key) {
        return properties.containsKey(key);
    }

    public GameEvent put(String key, String value) {
        properties.put(key, value);
        return this;
    }
}
