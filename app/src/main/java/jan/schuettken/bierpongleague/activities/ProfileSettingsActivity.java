package jan.schuettken.bierpongleague.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.exceptions.MailTakenException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.exceptions.UsernameTakenException;
import jan.schuettken.bierpongleague.exceptions.WrongOldPasswordException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.DialogHandler;
import jan.schuettken.bierpongleague.handler.LogoutHandler;
import jan.schuettken.bierpongleague.handler.PreferencesHandler;

public class ProfileSettingsActivity extends BasicDrawerPage {

    private String username;
    private PreferencesHandler preferencesHandler;
    private ApiHandler phpApiHandler;

    private enum ProfileType {
        MAIL, USER, PASSWORD
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        TextView tv = findViewById(R.id.textView_title);
        new Thread(() -> {
            if (currentUser != null) {
                username = currentUser.getFirstName() + " " + currentUser.getLastName();
                tv.setText(getResString(R.string.hello_user, username));
            }
        });


        Button buttonLogout = findViewById(R.id.button_logout);
        buttonLogout.setOnClickListener(v -> logout());

        TextView textViewChangeUsername = findViewById(R.id.textView_username);
        textViewChangeUsername.setOnClickListener(v -> changeUsername());

        TextView textViewChangePassword = findViewById(R.id.textView_password);
        textViewChangePassword.setOnClickListener(v -> changePassword());

        TextView textViewChangeMail = findViewById(R.id.textView_mail);
        textViewChangeMail.setOnClickListener(v -> changeMail());
    }

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_profile);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        switch (requestCode) {
            case REQUEST_PASSWORD:
                if (resultCode == RESULT_OK) {
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        handleChange(extras.getString(PASSWORD_PASS), ProfileType.PASSWORD, extras.getString(OLD_PASSWORD_PASS));
                    }
                }
                break;
        }
    }

    private void changeUsername() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_username);
        builder.setMessage(R.string.change_username_info);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(username);
        input.setEnabled(true);
        builder.setView(input);
        builder.setPositiveButton(R.string.button_change, (dialog, which) -> handleChange(input.getText().toString(), ProfileType.USER));
        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void changeMail() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_mail);
        builder.setMessage(R.string.change_mail_info);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setEnabled(true);
        builder.setView(input);
        builder.setPositiveButton(R.string.button_change, (dialog, which) -> handleChange(input.getText().toString(), ProfileType.MAIL));
        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.cancel());
        new Thread(() -> {
            try {
                input.setText(phpApiHandler.getMail());
            } catch (NoConnectionException | SessionErrorException | JSONException e) {
                e.printStackTrace();
            }
            handler.post(builder::show);
        }).start();
    }

    private void changePassword() {
        switchForResult(ProfilePasswordSettingsActivity.class, REQUEST_PASSWORD);
    }

    private void logout() {
        LogoutHandler.logout(this);
        switchView(StartActivity.class, true);
    }

    private void handleChange(final String newVar, final ProfileType type) {
        handleChange(newVar, type, null);
    }

    private void handleChange(final String newVar, final ProfileType type, final String oldVar) {
        final Handler handler = new Handler();
        showProgress(true);
        new Thread(() -> {
            try {
                if (phpApiHandler == null)
                    phpApiHandler = _createApiHandler();
                if (preferencesHandler == null)
                    preferencesHandler = new PreferencesHandler(ProfileSettingsActivity.this);
                if (type == ProfileType.MAIL)
                    phpApiHandler.changeMail(newVar);
                if (type == ProfileType.USER) {
                    phpApiHandler.changeUsername(newVar);
                    preferencesHandler.setUsername(newVar);
                }
                if (type == ProfileType.PASSWORD) {
                    phpApiHandler.changePassword(newVar, oldVar);
                    preferencesHandler.setPassword(newVar);
                }
                handler.post(() -> {
                    showProgress(false);
                    new DialogHandler().showAlterDialogOk(R.string.success, R.string.entry_changed, ProfileSettingsActivity.this);
                });
            } catch (WrongOldPasswordException e) {
                handler.post(() -> {
                    showProgress(false);
                    new DialogHandler().showAlterDialogOk(R.string.error, R.string.wrong_old_password, ProfileSettingsActivity.this);
                });
            } catch (MailTakenException e) {
                handler.post(() -> {
                    showProgress(false);
                    new DialogHandler().showAlterDialogOk(R.string.error, R.string.mail_taken, ProfileSettingsActivity.this);
                });
            } catch (UsernameTakenException e) {
                handler.post(() -> {
                    showProgress(false);
                    new DialogHandler().showAlterDialogOk(R.string.error, R.string.username_taken, ProfileSettingsActivity.this);
                });
            } catch (NoConnectionException e) {
                e.printStackTrace();
                handler.post(() -> {
                    showProgress(false);
                    showToast(R.string.an_error_has_occurred);
                });
            }
        }).start();
    }
}
