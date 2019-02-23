package jan.schuettken.bierpongleague.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.basic.Portable;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.DatabaseException;
import jan.schuettken.bierpongleague.exceptions.EmptyPreferencesException;
import jan.schuettken.bierpongleague.exceptions.InvalidLoginException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.PreferencesHandler;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BasicPage {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_REGISTER = 1;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private UserData user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = findViewById(R.id.email);
        PreferencesHandler preferencesHandler = new PreferencesHandler(this);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        try {
            mUsernameView.setText(preferencesHandler.getUsername());
            mPasswordView.setText(preferencesHandler.getPassword());
        } catch (EmptyPreferencesException ignored) {
        }

        Button mEmailSignInButton = findViewById(R.id.username_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void register(View view) {
        user = new UserData();
        user.setUsername(mUsernameView.getText().toString());
        user.setPassword(mPasswordView.getText().toString());
        switchForResult(RegisterActivity.class, REQUEST_REGISTER, new Portable("user", user));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_REGISTER:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        user = (UserData) data.getSerializableExtra("user");
                        if (user != null) {
                            mUsernameView.setText(user.getUsername());
                            mPasswordView.setText(user.getPassword());
                        }
                    }
                }
                break;
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //save Username and Password to prefs
            showProgress(true);
            login(username, password);
//            mAuthTask = new UserLoginTask(username, password);
//            mAuthTask.execute((Void) null);
        }
    }

    private void login(final String username, final String password) {
        final Handler handler = new Handler();

        new Thread() {
            @Override
            public void run() {
                boolean success = false;
                ApiHandler apiHandler = null;
                try {
                    // Simulate network access.
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
                try {
                    apiHandler = new ApiHandler(username, password,LoginActivity.this);
                    //automatically logged in
                    success = true;
                } catch (InvalidLoginException ignored) {
                } catch (NoConnectionException | DatabaseException e) {
                    showToast(R.string.no_internet_connection);
                }


                final boolean finalSuccess = success;
                final ApiHandler finalApiHandler = apiHandler;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        Log.e("LOGIN", "result:" + finalSuccess);
                        if (finalSuccess) {
                            PreferencesHandler preferencesHandler = new PreferencesHandler(LoginActivity.this);
                            preferencesHandler.setSessionId(finalApiHandler.getSession());
                            preferencesHandler.setUsername(username);
                            preferencesHandler.setPassword(password);
                            switchView(OverviewActivity.class, true);
                        } else {
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                        }
                    }
                });
            }
        }.start();

    }

    private boolean isEmailValid(String email) {
        return email.length() >= 3;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

