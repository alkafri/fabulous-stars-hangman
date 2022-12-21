package yh.fabulousstars.hangman;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaHelper {
    private static List<String> IMAGE_LIST = Arrays.asList(
            "/HangmanTranState1.png",
            "/HangmanTranState2.png",
            "/HangmanTranState3.png",
            "/HangmanTranState4.png",
            "/HangmanTranState5.png",
            "/HangmanTranState6.png",
            "/HangmanTranState7.png",
            "/HangmanTranState8.png",
            "/HangmanTranState9.png",
            "/HangmanTranState10.png",
            "/HangmanTranState11.png",
            "/BlackBarTR.png"
    );
    private static List<String> AUDIO_LIST = Arrays.asList(
            "/button.wav",
            "/error.wav",
            "/success.wav"
    );
    private static List<String> MEDIA_LIST = Arrays.asList(
            "/8-bit-brisk-music-loop.mp3"
    );

    private static MediaHelper singleton = null;
    private final Map<String, Image> imageMap;
    private final Map<String, AudioClip> soundMap;
    private final Map<String, Media> mediaMap;

    private MediaHelper() {
        this.imageMap = new HashMap<>();
        this.soundMap = new HashMap<>();
        this.mediaMap = new HashMap<>();

        // load images
        for (var fileName : IMAGE_LIST) {
            var uri = GameApplication.class.getResource(fileName);
            var name = fileName.substring(1, fileName.lastIndexOf('.'));
            imageMap.put(name, new Image(uri.toString()));
        }
        // load sounds
        for (var fileName : AUDIO_LIST) {
            var uri = GameApplication.class.getResource(fileName);
            var name = fileName.substring(1, fileName.lastIndexOf('.'));
            soundMap.put(name, new AudioClip(uri.toString()));
        }
        // load media
        for (var fileName : MEDIA_LIST) {
            var uri = GameApplication.class.getResource(fileName);
            var name = fileName.substring(1, fileName.lastIndexOf('.'));
            mediaMap.put(name, new Media(uri.toString()));
        }
    }

    public static MediaHelper getInstance() {
        if (singleton == null) {
            singleton = new MediaHelper();
        }
        return singleton;
    }

    public AudioClip getSound(String name) {
        return soundMap.get(name);
    }

    public Image getImage(String name) {
        return imageMap.get(name);
    }

    public MediaPlayer getMedia(String name) {
        return new MediaPlayer(mediaMap.get(name));
    }
}
