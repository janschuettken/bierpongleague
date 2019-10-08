package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.basic.Portable;

/**
 * Created by Jan Schüttken on 10.03.2018 at 14:44
 */

public class ProfilePasswordSettingsActivity extends BasicPage {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_settings_profile_password);
        final EditText password = findViewById(R.id.editText_password);
        final EditText confirm = findViewById(R.id.editText_password_confirm);
        confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!password.getText().toString().equals(charSequence.toString())) {
                    confirm.setError(getResString(R.string.not_identical_passwords));
                } else
                    confirm.setError(null);//remove error field!
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isPasswordValid(charSequence.toString())) {
                    password.setError(getResString(R.string.to_short));
                } else
                    confirm.setError(null);//remove error field!
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    public void goBack(View view) {
        finishWithResult(RESULT_CANCELED);
    }

    public void changePassword(View view) {
        EditText password = findViewById(R.id.editText_password);
        EditText oldPassword = findViewById(R.id.editText_old_password);
        EditText confirm = findViewById(R.id.editText_password_confirm);

        String password_text = password.getText().toString();
        String old_password_text = oldPassword.getText().toString();
        String confirm_text = confirm.getText().toString();
        if (password_text.equals(confirm_text) && isPasswordValid(password_text)) {
            finishWithExtra(RESULT_OK, new Portable(PASSWORD_PASS, password_text), new Portable(OLD_PASSWORD_PASS, old_password_text));
        } else {
            showToast(R.string.not_identical_passwords);
        }
    }
}