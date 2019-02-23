package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.basic.Portable;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.UsernameTakenException;
import jan.schuettken.bierpongleague.handler.ApiHandler;

public class RegisterActivity extends BasicPage {

    private UserData user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        user = (UserData) getObjectParameter("user");
    }

    public void register(View view) {
        if (checkFields()) {
            ApiHandler apiHandler = new ApiHandler();
            try {
                apiHandler.register(user);
            } catch (NoConnectionException ignored) {
            } catch (UsernameTakenException e) {
                ((EditText) findViewById(R.id.username)).setError(getString(R.string.username_taken));
            }
            finishWithResult(RESULT_OK, new Portable("user", user));
        }
    }

    private boolean checkFields() {
        Bool check = new Bool();
        //first_name
        user.setFirstName(checkPartField(R.id.first_name, 2, check));
        user.setLastName(checkPartField(R.id.last_name, 2, check));
        user.setNickName(checkPartField(R.id.nick_name, -2, check));
        user.setUsername(checkPartField(R.id.username, 3, check));
        user.setEmail(checkPartField(R.id.mail, 5, check));
        user.setPassword(checkPartField(R.id.password, 8, check));
        user.setGender(((Spinner) findViewById(R.id.spinner_gender)).getSelectedItemPosition());

        return check.check;
    }

    private String checkPartField(int fieldId, int lengh, Bool check) {
        EditText text;
        text = findViewById(fieldId);
        text.setError(null);
        String value = text.getText().toString();
        if (lengh < 0) {
            return value;
        }
        if (value.length() < Math.abs(lengh)) {
            text.setError(getString(R.string.to_short));
            check.check = false;
            return null;
        } else {
            return value;
        }
    }

    private class Bool {
        boolean check = true;
    }
}
