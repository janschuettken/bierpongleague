package jan.schuettken.bierpongleague.exceptions;

/**
 * Created by Jan Schüttken on 30.10.2018 at 21:35
 */
public class WrongPasswordException extends Exception {
    public WrongPasswordException(String errorText) {
        super(errorText);
    }
}
