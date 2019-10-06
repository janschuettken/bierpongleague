package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicPage;
import jan.schuettken.bierpongleague.data.AreaData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.DialogHandler;

public class AddAreaActivity extends BasicPage {

    private UserData currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_area);
        currentUser = (UserData) getObjectParameter(CURRENT_USER);
        createSpinner();
    }

    public void addArea(View view) {
        addArea();
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

    private void createSpinner() {
        Spinner spinnerAreas = findViewById(R.id.spinner_area_type);
        spinnerAreas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        String[] areaNames;
        if (currentUser != null && currentUser.getPower() >= 100)
            areaNames = new String[]{getResString(R.string.private_area), getResString(R.string.public_area)};
        else
            areaNames = new String[]{getResString(R.string.private_area)};
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, areaNames);
        spinnerAreas.setAdapter(adapter1);
    }

    private void addArea() {
        Handler handler = new Handler();
        new Thread(() -> {
            ApiHandler apiHandler = createApiHandler();
            try {
                UserData yourself = apiHandler.getYourself();
                apiHandler.addArea(createArea(), yourself);
            } catch (NoConnectionException | SessionErrorException | JSONException e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                showToast(R.string.area_added);
                finish();
            });
        }).start();
    }

    private AreaData createArea() {
        AreaData area = new AreaData();
        EditText text = findViewById(R.id.editText_area_name);
        area.setName(text.getText().toString());
        Spinner spinner = findViewById(R.id.spinner_area_type);
        int type;
        if (spinner.getSelectedItem().toString().equals(getResString(R.string.private_area))) {
            type = AreaData.TYPE_PRIVATE;
        } else if (spinner.getSelectedItem().toString().equals(getResString(R.string.public_area))) {
            type = AreaData.TYPE_PUBLIC;
        } else
            type = AreaData.TYPE_PRIVATE;
        area.setType(type);
        return area;
    }
}
