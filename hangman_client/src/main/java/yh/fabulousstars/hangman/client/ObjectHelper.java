package yh.fabulousstars.hangman.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class ObjectHelper {

    public static Object fromBytes(byte[] bytes) {
        try(var input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return input.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static byte[] toBytes(Object object) {
        var bytes = new ByteArrayOutputStream();
        try(var output = new ObjectOutputStream(bytes)) {
            output.writeObject(object);
            output.flush();
            return bytes.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
