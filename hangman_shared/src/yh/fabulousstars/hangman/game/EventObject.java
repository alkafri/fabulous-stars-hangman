package yh.fabulousstars.hangman.game;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EventObject implements Serializable {
    @Serial
    private static final long serialVersionUID = 1020304050L;
    private final HashMap<String, String> properties;
    private String name;
    private Object payload;

    public EventObject(String name) {
        this.name = name;
        this.properties = new HashMap<>();
        this.payload = null;
    }

    public EventObject(String name, Map<String, String> source) {
        this.name = name;
        this.properties = new HashMap<>(source);
        this.payload = null;
    }

    public String getName() {
        return name;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String get(String key) {
        return properties.get(key);
    }

    public boolean contains(String key) {
        return properties.containsKey(key);
    }

    public void put(String key, String value) {
        properties.put(key, value);
    }
}
