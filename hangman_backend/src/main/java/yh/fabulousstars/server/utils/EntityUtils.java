package yh.fabulousstars.server.utils;

import com.google.appengine.api.datastore.Entity;

import java.util.List;

public class EntityUtils {

    /**
     * Copy properties from entity
     *
     * @param ent source
     * @param obj target
     */
    public static void setFromEntity(Entity ent, Object obj) {

        var fields = obj.getClass().getDeclaredFields();
        for (var field : fields) {
            var name = field.getName();
            if (ent.hasProperty(name)) {
                try {
                    field.set(obj, ent.getProperty(name));
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Copy properties from object
     *
     * @param obj  source
     * @param ent  target
     * @param skip skip copying
     */
    public static void setFromObject(Object obj, Entity ent, List<String> skip) {

        var fields = obj.getClass().getDeclaredFields();
        for (var field : fields) {
            var name = field.getName();
            if (skip != null && skip.contains(name)) {
                continue;
            }
            try {
                ent.setProperty(name, field.get(obj));
                field.set(obj, ent.getProperty(name));
            } catch (Exception e) {
            }
        }
    }
}
