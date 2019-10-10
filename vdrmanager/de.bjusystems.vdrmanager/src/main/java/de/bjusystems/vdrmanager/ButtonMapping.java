package de.bjusystems.vdrmanager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lado on 05.11.17.
 */
public class ButtonMapping {

    public static final String KEY = "key";
    public static final String LABEL = "label";
    public static final String COLOR = "color";

    public static final String PREFIX_SEPARATOR = "_";

    public static final String KEY_PREFIX = KEY + PREFIX_SEPARATOR;
    public static final String LABEL_PREFIX = LABEL + PREFIX_SEPARATOR;
    public static final String COLOR_PREFIX = COLOR + PREFIX_SEPARATOR;

    public static final Integer NO_COLOR = -1;

    /**
     * The Key.
     */
    public String key;
    /**
     * The Label.
     */
    public String label;
    /**
     * The Color.
     */
    public Integer color;

    public ButtonMapping() {

    }

    public ButtonMapping(String key, String label, Integer color) {
        this.key = key;
        this.label = label;
        this.color = color;
    }

    public JSONObject toJson() throws JSONException {
        return new JSONObject().put("key", key).put("label", label).put("color", color);
    }
}
