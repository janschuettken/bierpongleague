package jan.schuettken.bierpongleague.basic;

import java.io.Serializable;

/**
 * Created by Jan Schüttken on 22.12.2017 at 00:17
 */

public class Portable {
    private String key;
    private Serializable serializable;

    public Portable(String key, Serializable serializable) {
        this.key = key;
        this.serializable = serializable;
    }

    public String getKey() {
        return key;
    }

    public Serializable getSerializable() {
        return serializable;
    }
}
