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
import android.widget.Spinner;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.data.AreaData;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.EmptyPreferencesException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.exceptions.UserNotInAreaException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.DialogHandler;
import jan.schuettken.bierpongleague.handler.PreferencesHandler;

public class AddGameActivity extends BasicPage {

    private ApiHandler apiHandler;
    private Handler handler;
    private GameData game = new GameData(false);
    private List<AreaData> areas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);
        handler = new Handler();
        apiHandler = createApiHandler();
        new Thread(() -> {
            createApiHandler();
            if (apiHandler == null)
                finish();
            createAutofill();
            loadAreas();
        }).start();

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
            handler.post(() -> {
                AutoCompleteTextView textView = findViewById(R.id.editText_player_a);
                textView.setEnabled(false);
                textView.setText(you.getFirstName() + " " + you.getLastName());
                game.getParticipants()[0] = you;
                setAutoComplete(1, R.id.editText_player_b, autofillUser);
                setAutoComplete(2, R.id.editText_player_c, autofillUser);
                setAutoComplete(3, R.id.editText_player_d, autofillUser);
            });

        } catch (JSONException | SessionErrorException | NoConnectionException e) {
            e.printStackTrace();
            //should be impossible
        }
    }

    private void loadAreas() {
        try {
            areas = apiHandler.getAreas();
            List<AreaData> areasConfirmed = new ArrayList<>();
            PreferencesHandler preferencesHandler = new PreferencesHandler(this);
            int areaPos = 0;
            int areaId = -1;
            try {
                areaId = preferencesHandler.getLastArea();
            } catch (EmptyPreferencesException ignored) {
            }
            for (AreaData ad : areas) {
                if (ad.isConfirmed()) {
                    areasConfirmed.add(ad);
                    if (areaId == ad.getId())
                        areaPos = areasConfirmed.size() - 1;
                }
            }
            String[] areaNames = new String[areasConfirmed.size()];
            for (int i = 0; i < areaNames.length; i++) {
                areaNames[i] = areasConfirmed.get(i).getName();
            }
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, areaNames);
            Spinner spinnerAreas = findViewById(R.id.spinner_areas);
            spinnerAreas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    preferencesHandler.setLastArea(areasConfirmed.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            int finalAreaPos = areaPos;
            handler.post(() -> {
                spinnerAreas.setAdapter(adapter1);
                spinnerAreas.setSelection(finalAreaPos);
            });

        } catch (SessionErrorException | NoConnectionException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void setAutoComplete(final int player, int id, final List<UserData> autoCompleteUser) {
        final AutoCompleteTextView textView = findViewById(id);
        String[] userNames = new String[autoCompleteUser.size()];
        for (int i = 0; i < userNames.length; i++)
            userNames[i] = autoCompleteUser.get(i).getFirstName() + " " + autoCompleteUser.get(i).getLastName();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener((parent, view, position, id1) -> {
            int posInList = -1;
            for (int i = 0; i < autoCompleteUser.size(); i++) {
                if (textView.getText().toString().equals(autoCompleteUser.get(i).getFirstName() + " " + autoCompleteUser.get(i).getLastName())) {
                    posInList = i;
                    break;
                }
            }
            if (posInList > -1) {
                game.getParticipants()[player] = autoCompleteUser.get(posInList);
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
            if (user != null && user.isSet()) {
                String name = user.getFirstName() + " " + user.getLastName();
                if (name.equalsIgnoreCase("dummy account"))
                    return true;
                if (name.equalsIgnoreCase("test test"))
                    return true;
                if (name.equalsIgnoreCase("do not use"))
                    return true;
            }
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
                    } catch (UserNotInAreaException e) {
                        handler.post(() -> new DialogHandler().showAlterDialogOk(R.string.error, R.string.user_not_in_area, AddGameActivity.this));
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
            handler.post(() -> finalEt.setError(getString(R.string.error_field_required)));
            return false;
        }
        et = findViewById(R.id.editText_score_team_b);
        try {
            scoreB = Integer.parseInt(et.getText().toString());
        } catch (Exception e) {
            final EditText finalEt = et;
            handler.post(() -> finalEt.setError(getString(R.string.error_field_required)));

            return false;
        }
        game.setScores(scoreA, scoreB);


        //####
        Spinner spinner = findViewById(R.id.spinner_areas);
        String areName = spinner.getSelectedItem().toString();
        Log.e("are", areName);
        for (AreaData ad : areas) {
            if (ad.getName().equals(areName))
                game.setAreaId(ad.getId());
        }

        return true;
    }

    public void showScoreHint(View view) {
        new DialogHandler().getAlterDialogOk(R.string.score, R.string.score_description, this).show();
    }
}
