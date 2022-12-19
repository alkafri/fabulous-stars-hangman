package yh.fabulousstars.hangman.server.utils;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import yh.fabulousstars.hangman.utils.ObjectHelper;

public class EntityUtils {


    private static final String BLOB_NAME = "blobObject";

    /**
     * Put object to entity.
     *
     * @param entity
     * @param object
     */
    public static void putBlobObject(Entity entity, Object object) {
        // serialize object to bytes
        var bytes = ObjectHelper.toBytes(object);
        // set blob property
        entity.setProperty(BLOB_NAME, new Blob(bytes));
    }

    /**
     * Get object from entity.
     *
     * @param entity
     */
    public static Object getBlobObject(Entity entity) {
        // deserialize blob to object
        if (entity.hasProperty(BLOB_NAME)) {
            var blob = (Blob) entity.getProperty(BLOB_NAME);
            return ObjectHelper.fromBytes(blob.getBytes());
        }
        return null;
    }

    public static byte[] getBlobBytes(Entity entity) {
        // deserialize blob to object
        if (entity.hasProperty(BLOB_NAME)) {
            var blob = (Blob) entity.getProperty(BLOB_NAME);
            return blob.getBytes();
        }
        return null;
    }
}
