package jan.schuettken.bierpongleague.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.pm.PackageInfoCompat;

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
        doBackgroundLogin();
        showAppVersionToast();
    }

    private void doBackgroundLogin() {
        final Handler handler = new Handler();
        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
            checkLogin(handler);
        }).start();
    }

    private void checkLogin(Handler handler) {
        String username, password;

        PreferencesHandler prefHandler = new PreferencesHandler(this);
        try {
            username = prefHandler.getUsername();
            password = prefHandler.getPassword();
        } catch (EmptyPreferencesException e) {
            switchView(LoginActivity.class, true, handler);
            return; //otherwise it crashes ;)
        }


        try {
            assert username != null && password != null;
            ApiHandler apiHandler = new ApiHandler(username, password, this);
            prefHandler.setSessionId(apiHandler.getSession());
            switchView(OverviewActivity.class, true, handler);

        } catch (InvalidLoginException e) {
            switchView(LoginActivity.class, true, handler);
        } catch (NoConnectionException | DatabaseException e) {
            handler.post(() -> findViewById(R.id.retry_connect_to_server).setVisibility(View.VISIBLE));

        }
    }

    private void showAppVersionToast() {
        PackageManager manager = getPackageManager();
        String versionName;
        int versionCode = -1;
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            versionName = info.versionName;
            versionCode = (int) PackageInfoCompat.getLongVersionCode(info);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "Unknown";
        }

        TextView tv = findViewById(R.id.textView_version);
        tv.setText(getResString(R.string.show_version, versionName, versionCode));
    }

    public void retryConnectToServer(View view) {
        showToast(R.string.no_internet_connection);
        doBackgroundLogin();
    }
}
