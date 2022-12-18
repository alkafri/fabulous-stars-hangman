package yh.fabulousstars.hangman.client.events;

public class RequestWord extends AbstractEvent {
    private int minLength;
    private int maxLength;

    public RequestWord(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getMinLength() {
        return minLength;
    }
}
