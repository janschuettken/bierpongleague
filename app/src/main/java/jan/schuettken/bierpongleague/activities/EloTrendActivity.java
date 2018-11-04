package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;

import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;

public class EloTrendActivity extends BasicDrawerPage {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elo_trend);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.elo_trend);
    }

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_elo_trend);
    }
}
