package jan.schuettken.bierpongleague.handler;


import android.os.Handler;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.activities.LoginActivity;
import jan.schuettken.bierpongleague.basic.BasicPage;

/**
 * Created by Jan Schüttken on 17.09.2019 at 21:52
 */
public class ExceptionHandler {

    public static void serverNotAvailable(Handler handler, BasicPage context){
        handler.post(() ->
                new DialogHandler().getAlterDialogOk(R.string.error, R.string.server_not_available, () ->
                        context.switchView(LoginActivity.class, true), context).show());
    }
}
