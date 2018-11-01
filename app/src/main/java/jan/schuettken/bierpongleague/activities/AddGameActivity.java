package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.view.View;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;

public class AddGameActivity extends BasicPage {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);

    }

    public void addGame(View view){
        showToast(R.string.comming_soon);
    }
}
