package jan.schuettken.bierpongleague.basic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;

import jan.schuettken.bierpongleague.R;

/**
 * Created by Jan Schüttken on 22.08.2017 at 17:26
 */

@SuppressLint("Registered")
public class BasicPage extends AppCompatActivity implements PageInterfaceLarge {

    public static final int REQUEST_REFRESH = 1;
    public static final int REQUEST_FILTER = 2;
    public static final int REQUEST_DOWNLOAD_ENTRIES = 3;
    public static final int REQUEST_PASSWORD = 4;
    public static final int RESULT_DELETED = 10;

    @Override
    public void switchView(Class<?> o) {
        switchView(o, false);
    }

    @Override
    public void switchView(Class<?> o, boolean finish) {
        switchView(o, finish, false);
    }

    public boolean switchView(String packageName, String className) {
        return switchView(packageName, className, false);
    }

    public boolean switchView(String packageClassName) {
        return switchView(packageClassName, false);
    }

    public boolean switchView(String packageClassName, boolean finish) {
        try {
            Class<?> activityClass = Class.forName(packageClassName);
            switchView(activityClass, finish);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean switchView(String packageName, String className, boolean finish) {
        return switchView(packageName + "." + className, finish);
    }

    @Override
    public void switchView(Class<?> o, boolean finish, boolean clearTop) {
        Intent intent = new Intent(this, o);
        if (clearTop)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        if (finish)
            finish();
    }

    @Override
    public void switchView(Class<?> o, boolean finish, String parameterName, String parameter) {
        Intent intent = new Intent(this, o);
        intent.putExtra(parameterName, parameter);
        startActivity(intent);
        if (finish)
            finish();
    }

    @Override
    public void switchView(Class<?> o, boolean finish, String parameterName, Serializable parameter) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(parameterName, parameter);
        Intent intent = new Intent(this, o);
        intent.putExtras(bundle);
        startActivity(intent);
        if (finish)
            finish();
    }

    @Override
    public void switchView(Class<?> o, Portable... portables) {
        switchView(o, false, portables);
    }

    @Override
    public void switchView(Class<?> o, boolean finish, Portable... portables) {
        Bundle bundle = new Bundle();
        for (Portable p : portables) {
            bundle.putSerializable(p.getKey(), p.getSerializable());
        }
        Intent intent = new Intent(this, o);
        intent.putExtras(bundle);
        startActivity(intent);
        if (finish)
            finish();
    }

    @Override
    public void switchBackToView(Class<?> o) {
        Intent intent = new Intent(this, o);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    public void switchForResult(Class<?> o, int request) {
        Intent intent = new Intent(this, o);
        startActivityForResult(intent, request);
    }

    @Override
    public void switchForResult(Class<?> o, int request, Portable... portables) {
        Bundle bundle = new Bundle();
        for (Portable p : portables) {
            bundle.putSerializable(p.getKey(), p.getSerializable());
        }
        Intent intent = new Intent(this, o);
        intent.putExtras(bundle);
        startActivityForResult(intent, request);
    }

    @Override
    public void finishWithResult(int result) {
        Intent intent = new Intent();
        setResult(result, intent);
        finish();
    }

    @Override
    public void finishWithResult(int result, Portable... portables) {
        Bundle bundle = new Bundle();
        for (Portable p : portables) {
            bundle.putSerializable(p.getKey(), p.getSerializable());
        }
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(result, intent);
        finish();
    }


    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public String getParameter(String name) {
        Intent i = getIntent();
        return i.getStringExtra(name);
    }

    @Override
    public Serializable getObjectParameter(String name) {
        Intent i = getIntent();
        return i.getSerializableExtra(name);
    }

    public void showToast(String s) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (this.getMainLooper().isCurrentThread()) {
                    Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                } else {
                    Log.e("showToast", "Error catched: \"Can't create handler inside thread that has not called Looper.prepare()\" ### Message:" + s);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void showToast(int id) {
        showToast(this.getResources().getString(id));
    }

    public void showToast(int id, Object... res) {
        showToast(getString(id, res));
    }

    public void showProgress(final boolean show) {
        try {
            showProgress(show, findViewById(R.id.region_progress), findViewById(R.id.login_progress_bg));
        } catch (NullPointerException ignored) {
        }
    }

    public void showProgress(final boolean show, final View foreground, final View background) {

        int shortAnimTime = getApplicationContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        int longAnimTime = getApplicationContext().getResources().getInteger(android.R.integer.config_longAnimTime);

//        enableEditing(!show);
        foreground.setVisibility(show ? View.VISIBLE : View.GONE);
        background.animate().setDuration(longAnimTime).alpha(
                show ? 0.5f : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                foreground.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    protected String getResString(int key) {
        return getResources().getString(key);
    }

    protected String getResString(int key, Object... para) {
        return getResources().getString(key, para);
    }

    protected void finishWithExtra(String key, Serializable extra) {
        finishWithExtra(key, extra, RESULT_OK);
    }

    protected void finishWithExtra(String key, Serializable extra, int code) {
        Intent intent = new Intent();
        intent.putExtra(key, extra);
        setResult(code, intent);
        finish();
    }
}
