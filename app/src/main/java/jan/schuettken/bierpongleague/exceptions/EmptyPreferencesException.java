package jan.schuettken.bierpongleague.exceptions;

/**
 * Created by Jan Schüttken on 01.11.2018 at 14:56
 */
public class EmptyPreferencesException extends Exception {
    public EmptyPreferencesException(String errorText) {
        super("Empty preferences: " + errorText);
    }
}
