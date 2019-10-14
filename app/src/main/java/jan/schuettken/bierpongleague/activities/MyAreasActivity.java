package jan.schuettken.bierpongleague.activities;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.basic.Portable;
import jan.schuettken.bierpongleague.custom.AreaRecyclerListAdapter;
import jan.schuettken.bierpongleague.custom.SimpleItemTouchHelperCallback;
import jan.schuettken.bierpongleague.data.AreaData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.exceptions.UserAlreadyInAreaException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.DialogHandler;

public class MyAreasActivity extends BasicDrawerPage {

    private Handler handler;
    private AreaRecyclerListAdapter recyclerList;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView templateList;
    private ApiHandler apiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_areas);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.my_areas);
        handler = new Handler();
        initializeRefreshListener();
        initializeList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_areas, menu);

        changeMenuColorToWhite(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_area:
                switchForResult(AddAreaActivity.class, REQUEST_REFRESH, new Portable(CURRENT_USER, currentUser));
                return true;
            case R.id.action_join_area:
                addToAreaViaCode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeRefreshListener() {
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        //swipeContainer.setRefreshing(false);
        swipeContainer.setOnRefreshListener(this::loadAreas);
    }

    private void initializeList() {
        templateList = findViewById(R.id.listView_areas);
        templateList.setHasFixedSize(true);
        templateList.setLayoutManager(new LinearLayoutManager(this));

        recyclerList = new AreaRecyclerListAdapter(this);
        recyclerList.setCurrentUser(currentUser);
        //Enable reordering the list
        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(recyclerList);
        callback.setLongPressDragEnabled(false);
        callback.setItemViewSwipeEnabled(false);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(templateList);
        templateList.setAdapter(recyclerList);
        recyclerList.setOnItemClickListener(position -> {
            AreaData areaData = recyclerList.getItems().get(position);
            switchForResult(EditAreaActivity.class, REQUEST_REFRESH,
                    new Portable(PASS_AREA, areaData),
                    new Portable(CURRENT_USER, currentUser));
        });

        loadAreas();
    }

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_area);
    }

    private boolean checkApiHandler() {
        apiHandler = createApiHandler();
        return apiHandler != null;
    }

    public void loadAreas() {
        swipeContainer.setRefreshing(true);
        new Thread(() -> {
            if (!checkApiHandler())
                return;
            try {
                final List<AreaData> areas = apiHandler.getAreas();
                apiHandler.getAreaUsers(areas);
                handler.post(() -> {
//                    findViewById(R.id.no_games_played_warning).setVisibility(View.GONE);
                    recyclerList.getItems().clear();
                    recyclerList.getItems().addAll(areas);
                    templateList.setAdapter(recyclerList);
                    swipeContainer.setRefreshing(false);
                });

            } catch (NoConnectionException | SessionErrorException | JSONException e) {
                e.printStackTrace();
                //no games are played
                handler.post(() -> {
//                    findViewById(R.id.no_games_played_warning).setVisibility(View.VISIBLE);
                    swipeContainer.setRefreshing(false);
                });
            }
        }).start();
    }

    private void addToAreaViaCode() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.join_area);
        builder.setMessage(R.string.enter_area_code);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setEnabled(true);
        input.setText(preloadTextField());
        builder.setView(input);
        builder.setPositiveButton(R.string.button_ok, (dialog, which) -> {
            String code = input.getText().toString();
            if (code.length() > 0) {
                new Thread(() -> {
                    try {
                        apiHandler.addUserToAreaWithCode(code);
                        handler.post(() -> {
                            showToast(R.string.player_added);
                            loadAreas();
                        });
                    } catch (NoConnectionException | SessionErrorException e) {
                        e.printStackTrace();
                    } catch (UserAlreadyInAreaException e) {
                        handler.post(() -> new DialogHandler().showAlterDialogOk(R.string.error, R.string.user_already_in_area, MyAreasActivity.this));
                    }
                }).start();
            } else {
                handler.post(() -> showToast(R.string.no_code_entered));
            }
        });
        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private String preloadTextField() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final int LENGH_OF_CODE = 10;//10 is the lengh of the area Codes
        if (clipboard != null)
            if (clipboard.getPrimaryClip() != null)
                if (clipboard.getPrimaryClip().getItemCount() > 0) {
                    String copy = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
                    if (copy.length() == LENGH_OF_CODE) {
                        showToast(R.string.code_from_clipboard);
                        return copy;
                    }

                }
        return "";
    }
}
