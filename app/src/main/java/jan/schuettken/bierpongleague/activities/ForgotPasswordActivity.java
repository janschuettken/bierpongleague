package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.exceptions.MailNotTakenException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.DialogHandler;
import jan.schuettken.bierpongleague.handler.ExceptionHandler;

public class ForgotPasswordActivity extends BasicPage {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        Button forgotPasswordButton = findViewById(R.id.forgot_password_button);
        forgotPasswordButton.setOnClickListener(view -> {
            EditText et = findViewById(R.id.email_for_reset);
            handleForgotPassword(et.getText().toString());
        });
    }

    private void handleForgotPassword(final String mail) {
        final Handler handler = new Handler();
        showProgress(true);
        new Thread(() -> {
            ApiHandler phpApiHandler = new ApiHandler();
            try {
                phpApiHandler.forgotPassword(mail);
                handler.post(() -> {
                    showProgress(false);
                    new DialogHandler().showAlterDialogOk(R.string.hint_mail, R.string.you_received_an_mail, ForgotPasswordActivity.this);
                });
            } catch (MailNotTakenException e) {
                e.printStackTrace();
                handler.post(() -> {
                    showProgress(false);
                    new DialogHandler().showAlterDialogOk(R.string.error, R.string.email_not_taken, this);
                });
            } catch (NoConnectionException e) {
                e.printStackTrace();
                showProgress(false);
                ExceptionHandler.serverNotAvailable(handler, ForgotPasswordActivity.this);
            }
        }).start();
    }
}
