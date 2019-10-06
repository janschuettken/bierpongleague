package jan.schuettken.bierpongleague.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.data.AreaData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.NotEnoughPowerException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.exceptions.UserAlreadyInAreaException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.DialogHandler;

public class EditAreaActivity extends BasicPage {

    private AreaData area;
    private UserData currentUser;
    private Handler handler;
    private UserData addUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_area);
        area = (AreaData) getObjectParameter(PASS_AREA);
        currentUser = (UserData) getObjectParameter(CURRENT_USER);
        handler = new Handler();
        if (area == null || currentUser == null)
            finishWithResult(RESULT_CANCELED);
        Objects.requireNonNull(getSupportActionBar()).setTitle(area.getName());
        loadArea();
        createAutofill();
    }

    private void loadArea() {
        EditText etName = findViewById(R.id.editText_area_name);
        etName.setText(area.getName());

        Spinner spinnerAreas = findViewById(R.id.spinner_area_type);
        String[] areaNames = {getResString(R.string.public_area), getResString(R.string.private_area)};//new String[areasConfirmed.size()];
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, areaNames);
        spinnerAreas.setAdapter(adapter1);

        spinnerAreas.setSelection(area.getType() - 1);

        TextView tv = findViewById(R.id.area_code);
        tv.setText(area.getEntranceCode());

        //set Admin visibility
        boolean admin = area.isAdmin(currentUser);
        etName.setEnabled(admin);
        spinnerAreas.setEnabled(admin);
        findViewById(R.id.region_add_user).setVisibility(admin && area.isPrivate() ? View.VISIBLE : View.GONE);
        findViewById(R.id.save_changes_button).setVisibility(admin ? View.VISIBLE : View.GONE);
    }

    private void createAutofill() {
        new Thread(() -> {
            final List<UserData> autofillUser;
            ApiHandler apiHandler = createApiHandler();
            try {
                autofillUser = apiHandler.getUser();
                handler.post(() -> setAutoComplete(autofillUser));

            } catch (JSONException | SessionErrorException | NoConnectionException e) {
                e.printStackTrace();
                //should be impossible
            }
        }).start();
    }

    private void setAutoComplete(final List<UserData> autoCompleteUser) {
        final AutoCompleteTextView textView = findViewById(R.id.editText_user_name);
        String[] userNames = new String[autoCompleteUser.size()];
        for (int i = 0; i < userNames.length; i++)
            userNames[i] = autoCompleteUser.get(i).getFirstName() + " " + autoCompleteUser.get(i).getLastName();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener((parent, view, position, id1) -> {
            addUser = null;
            for (int i = 0; i < autoCompleteUser.size(); i++) {
                if (textView.getText().toString().equals(autoCompleteUser.get(i).getFirstName() + " " + autoCompleteUser.get(i).getLastName())) {
                    addUser = autoCompleteUser.get(i);
                    break;
                }
            }
        });
    }

    public void save(View view) {
    }

    private void addUser() {
        new Thread(() -> {
            ApiHandler apiHandler = createApiHandler();
            boolean reset = false;
            try {
                apiHandler.addUserToArea(area, addUser);
                reset = true;
                showToast(R.string.add_player);
            } catch (NoConnectionException | SessionErrorException e) {
                e.printStackTrace();
            } catch (NotEnoughPowerException e) {
                handler.post(() -> new DialogHandler().showAlterDialogOk(R.string.error, R.string.must_be_admin, EditAreaActivity.this));
            } catch (UserAlreadyInAreaException e) {
                reset = true;
                handler.post(() -> new DialogHandler().showAlterDialogOk(R.string.error, R.string.user_already_in_area, EditAreaActivity.this));
            }
            if (reset) {
                EditText editText = findViewById(R.id.editText_user_name);
                editText.setText("");
            }
        }).start();
    }

    public void showAreaHint(View view) {
        Spinner spinner = findViewById(R.id.spinner_area_type);
        int infoTextId = 0;
        if (spinner.getSelectedItem().toString().equals(getResString(R.string.private_area))) {
            infoTextId = R.string.private_area_info;
        } else if (spinner.getSelectedItem().toString().equals(getResString(R.string.public_area))) {
            infoTextId = R.string.public_area_info;
        }
        if (infoTextId != 0)
            new DialogHandler().showAlterDialogOk(R.string.info, infoTextId, this);
    }

    public void showAreaCodeHint(View view) {
        new DialogHandler().showAlterDialogOk(R.string.info, R.string.area_code_hint, this);
    }

    public void copyAreaCode(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("areaCode", area.getEntranceCode());
        clipboard.setPrimaryClip(clip);
        showToast(R.string.saved_to_clipboard);
    }

    public void addPlayer(View view) {
        addUser();
    }

}
