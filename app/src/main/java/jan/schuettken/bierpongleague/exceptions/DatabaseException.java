package jan.schuettken.bierpongleague.exceptions;

/**
 * Created by Jan Schüttken on 30.10.2018 at 21:35
 */
public class DatabaseException extends Exception {
    public DatabaseException(String errorText) {
        super(errorText);
    }
}
