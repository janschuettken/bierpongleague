package jan.schuettken.bierpongleague.exceptions;

/**
 * Created by Jan Schüttken on 24.02.2019 at 16:18
 */
public class NoGamesException extends Exception {
    public NoGamesException(String errorText) {
        super(errorText);
    }

    public NoGamesException() {
    }
}
