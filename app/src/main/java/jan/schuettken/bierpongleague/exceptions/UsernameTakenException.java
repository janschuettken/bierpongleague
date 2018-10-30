package jan.schuettken.bierpongleague.exceptions;

/**
 * Created by Jan Schüttken on 30.10.2018 at 21:35
 */
public class UsernameTakenException extends Exception {
    public UsernameTakenException(String errorText) {
        super(errorText);
    }
}
