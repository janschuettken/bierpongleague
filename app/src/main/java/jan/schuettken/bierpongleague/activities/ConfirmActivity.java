package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.os.Handler;

import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;

public class ConfirmActivity extends BasicDrawerPage {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.to_confirm);
        handler = new Handler();
    }

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_to_confirm);
    }
}
