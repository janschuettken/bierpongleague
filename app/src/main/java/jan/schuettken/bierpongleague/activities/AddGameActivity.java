package jan.schuettken.bierpongleague.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SeekBar;

import org.json.JSONException;

import java.util.List;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.DialogHandler;

public class AddGameActivity extends BasicPage {

    private ApiHandler apiHandler;
    private Handler handler;
    private GameData game = new GameData(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        handler = new Handler();
        apiHandler = createApiHandler();
        new Thread() {
            @Override
            public void run() {
                createApiHandler();
                if (apiHandler == null)
                    finish();
                createAutofill();
            }
        }.start();

        final EditText et_a = findViewById(R.id.editText_score_team_a);
        final EditText et_b = findViewById(R.id.editText_score_team_b);
        final SeekBar seekBar_a = findViewById(R.id.seekBar_score_team_a);
        final SeekBar seekBar_b = findViewById(R.id.seekBar_score_team_b);
        et_a.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    try {
                        int number = Integer.parseInt(s.toString());

                        if (number > 0) {
                            et_b.setText("0");
                            et_b.setEnabled(false);
                            seekBar_a.setProgress(number);
                            seekBar_b.setProgress(0);
                        } else if (number == 0) {
                            et_b.setEnabled(true);
                        } else {
                            resetScoreFields(et_a, et_b);
                        }
                    } catch (Exception e) {
                        resetScoreFields(et_a, et_b);
                    }

                } else {
                    et_b.setEnabled(true);
                }
            }
        });
        et_b.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    try {
                        int number = Integer.parseInt(s.toString());
                        if (number > 0) {
                            et_a.setText("0");
                            et_a.setEnabled(false);
                            seekBar_b.setProgress(number);
                            seekBar_a.setProgress(0);
                        } else if (number == 0) {
                            et_a.setEnabled(true);
                        } else {
                            resetScoreFields(et_a, et_b);
                        }
                    } catch (Exception e) {
                        resetScoreFields(et_a, et_b);
                    }

                } else {
                    et_a.setEnabled(true);
                }
            }
        });

        seekBar_a.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                et_a.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar_b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                et_b.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void resetScoreFields(EditText et_a, EditText et_b) {
        et_b.setText("");
        et_b.setEnabled(true);
        et_a.setText("");
        et_a.setEnabled(true);
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
                if (posInList > -1) {
                    game.getParticipants()[player] = autofillUser.get(posInList);
                    if (checkUserForDuplicate()) {
                        game.getParticipants()[player] = null;
                        textView.setText("");
                        textView.setError(getString(R.string.user_used));
                    }
                    if (checkForBadPlayer() && game.getParticipants()[0].getPower() < 50) {
                        game.getParticipants()[player] = null;
                        textView.setText("");
                        textView.setError(getString(R.string.bad_user));
                    }
                } else
                    throw new RuntimeException("User not in list - Internal Error");


            }
        });
    }

    private boolean checkUserForDuplicate() {
        for (int i = 0; i < game.getParticipants().length; i++) {
            for (int n = 0; n < game.getParticipants().length; n++) {
                if (i == n)
                    continue;
                if (game.getParticipant(i) == null || game.getParticipant(n) == null)
                    continue;
                if (game.getParticipant(i).equals(game.getParticipant(n))) {
                    Log.e("CHECK", i + ":" + n + " - " + game.getParticipant(i).getFullName() + ":" + game.getParticipant(n).getFullName());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkForBadPlayer() {
        for (int i = 0; i < game.getParticipants().length; i++) {
            UserData user = game.getParticipant(i);
            String name = user.getFirstName() + " " + user.getLastName();
            if (name.equalsIgnoreCase("dummy account"))
                return true;
            if (name.equalsIgnoreCase("test test"))
                return true;
            if (name.equalsIgnoreCase("do not use"))
                return true;
        }
        return false;
    }

    public void addGame(View view) {
        createApiHandler();
        new Thread() {
            @Override
            public void run() {
                if (createGame()) {
                    try {
                        if (apiHandler.addGame(game)) {
                            showToast(R.string.game_added, handler);
                            finish(handler);
                        }
                    } catch (SessionErrorException | NoConnectionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    private boolean createGame() {
        int scoreA, scoreB;
        EditText et = findViewById(R.id.editText_score_team_a);
        try {
            scoreA = Integer.parseInt(et.getText().toString());
        } catch (Exception e) {
            final EditText finalEt = et;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    finalEt.setError(getString(R.string.error_field_required));
                }
            });
            return false;
        }
        et = findViewById(R.id.editText_score_team_b);
        try {
            scoreB = Integer.parseInt(et.getText().toString());
        } catch (Exception e) {
            final EditText finalEt = et;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    finalEt.setError(getString(R.string.error_field_required));
                }
            });

            return false;
        }

        game.setScores(scoreA, scoreB);
        return true;
    }

    public void showScoreHint(View view) {
        new DialogHandler().getAlterDialogOk(R.string.score, R.string.score_description, this).show();
    }
}
