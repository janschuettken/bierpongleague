package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.os.Handler;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.exceptions.DatabaseException;
import jan.schuettken.bierpongleague.exceptions.EmptyPreferencesException;
import jan.schuettken.bierpongleague.exceptions.InvalidLoginException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.PreferencesHandler;

/**
 * This Activity will show the logo and will be the lunch activity
 */
public class StartActivity extends BasicPage {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        final Handler handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                checkLogin(handler);
            }
        }.start();

    }

    private void checkLogin(Handler handler) {
        String username = null, password = null;

        PreferencesHandler prefHandler = new PreferencesHandler(this);
        try {
            username = prefHandler.getUsername();
            password = prefHandler.getPassword();
        } catch (EmptyPreferencesException e) {
            switchView(LoginActivity.class, true, handler);
            return;
        }


        try {
            assert username != null && password != null;
            ApiHandler apiHandler = new ApiHandler(username, password);
            prefHandler.setSessionId(apiHandler.getSession());
            switchView(OverviewActivity.class, true, handler);

        } catch (InvalidLoginException e) {
            switchView(LoginActivity.class, true, handler);
            return;
        } catch (NoConnectionException e) {
            //TODO OfflineMode - show retry button
        } catch (DatabaseException e) {
            //TODO try again - stay in activity
        }
    }
}
