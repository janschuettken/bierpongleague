package jan.schuettken.bierpongleague.handler;

import android.content.Context;

/**
 * Created by Jan Schüttken on 20.05.2019 at 23:45
 */
public class LogoutHandler {


    public LogoutHandler() {
    }

    public static void logout(Context context) {
        PreferencesHandler preferencesHandler = new PreferencesHandler(context);
        preferencesHandler.setPassword("");
        preferencesHandler.setUsername("");
        preferencesHandler.setSessionId("");
    }

}
