package jan.schuettken.bierpongleague.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import org.json.JSONException;

import java.util.List;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.DatabaseException;
import jan.schuettken.bierpongleague.exceptions.EmptyPreferencesException;
import jan.schuettken.bierpongleague.exceptions.InvalidLoginException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.DialogHandler;
import jan.schuettken.bierpongleague.handler.PreferencesHandler;

public class AddGameActivity extends BasicPage {

    private ApiHandler apiHandler;
    private Handler handler;
    private GameData game = new GameData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                if (!createApiHandler())
                    finish();
                createAutofill();
            }
        }.start();

    }

    private boolean createApiHandler() {
        PreferencesHandler preferencesHandler = new PreferencesHandler(this);
        try {
            //get Session if available
            apiHandler = new ApiHandler(preferencesHandler.getSessionId());
            return true;
        } catch (EmptyPreferencesException e) {
            try {
                apiHandler = new ApiHandler(preferencesHandler.getUsername(), preferencesHandler.getPassword());
                return true;
            } catch (NoConnectionException | DatabaseException e1) {
                //TODO Try again Later
                return false;
            } catch (InvalidLoginException | EmptyPreferencesException e1) {
                //You shouldn't be logged in
                switchView(LoginActivity.class, true);
                return false;
            }
        }
    }

    private void createAutofill() {
        final List<UserData> autofillUser;
        try {
            final UserData you = apiHandler.getYourself();
            autofillUser = apiHandler.getUser();
            handler.post(new Runnable() {
                @SuppressLint("SetTextI18n")
                @Override
                public void run() {
                    AutoCompleteTextView textView = findViewById(R.id.editText_player_a);
                    textView.setEnabled(false);
                    textView.setText(you.getFirstName() + " " + you.getLastName());
                    game.getParticipants()[0] = you;
                    setAutofill(1, R.id.editText_player_b, autofillUser);
                    setAutofill(2, R.id.editText_player_c, autofillUser);
                    setAutofill(3, R.id.editText_player_d, autofillUser);
                }
            });

        } catch (JSONException | SessionErrorException | NoConnectionException e) {
            e.printStackTrace();
            //should be impossible
        }
    }

    private void setAutofill(final int player, int id, final List<UserData> autofillUser) {
        final AutoCompleteTextView textView = findViewById(id);
        String[] userNames = new String[autofillUser.size()];
        for (int i = 0; i < userNames.length; i++)
            userNames[i] = autofillUser.get(i).getFirstName() + " " + autofillUser.get(i).getLastName();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int posInList = -1;
                for (int i = 0; i < autofillUser.size(); i++) {
                    if (textView.getText().toString().equals(autofillUser.get(i).getFirstName() + " " + autofillUser.get(i).getLastName())) {
                        posInList = i;
                        break;
                    }
                }
                if (posInList > -1)
                    game.getParticipants()[player] = autofillUser.get(posInList);
                else
                    throw new RuntimeException("User not in list - Internal Error");
            }
        });
    }

    public void addGame(View view) {
        if (createGame()) {
            try {
                if (apiHandler.addGame(game)) {
                    showToast(R.string.game_added);
                    finish();
                }
            } catch (SessionErrorException | NoConnectionException e) {
                createApiHandler();
                try {
                    if (apiHandler.addGame(game)) {
                        showToast(R.string.game_added);
                        finish();
                    }
                } catch (NoConnectionException | SessionErrorException e1) {
                    switchView(LoginActivity.class, true);
                }
            }
        }
    }

    private boolean createGame() {
        //TODO jeder user darf nut einmal auftauceh und alle felder müssen ausgefüllt sein
        int scoreA, scoreB;
        EditText et = findViewById(R.id.editText_score_team_a);
        try {
            scoreA = Integer.parseInt(et.getText().toString());
        } catch (Exception e) {
            et.setError(getString(R.string.error_field_required));
            return false;
        }
        et = findViewById(R.id.editText_score_team_b);
        try {
            scoreB = Integer.parseInt(et.getText().toString());
        } catch (Exception e) {
            et.setError(getString(R.string.error_field_required));
            return false;
        }

        game.setScores(scoreA, scoreB);
        return true;
    }

    public void showScoreHint(View view) {
        new DialogHandler().getAlterDialogOk(R.string.score, R.string.score_description, this).show();
    }
}
