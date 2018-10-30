package jan.schuettken.bierpongleague.exceptions;

/**
 * Created by Jan Schüttken on 30.10.2018 at 21:38
 */
public class SessionErrorException extends Exception {
    public SessionErrorException(String error) {
        super(error);
    }
}
