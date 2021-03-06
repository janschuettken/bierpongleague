package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.os.Handler;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.custom.ScoreboardRecyclerListAdapter;
import jan.schuettken.bierpongleague.custom.SimpleItemTouchHelperCallback;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.NoScoreboardException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;

/**
 * Created by Jan Schüttken on 15.02.2019 at 09:30
 */
public class ScoreboardActivity extends BasicDrawerPage {
    private Handler handler;
    private ScoreboardRecyclerListAdapter recyclerList;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView templateList;
    private ApiHandler apiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.scoreboard);
        handler = new Handler();
        initializeRefreshListener();
        initializeList();
    }

    private void initializeRefreshListener() {
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        //swipeContainer.setRefreshing(false);
        swipeContainer.setOnRefreshListener(this::loadGames);
    }

    private void initializeList() {
        templateList = findViewById(R.id.listView_elements);
        templateList.setHasFixedSize(true);
        templateList.setLayoutManager(new LinearLayoutManager(this));

        recyclerList = new ScoreboardRecyclerListAdapter(this);
        recyclerList.setCurrentUser(currentUser);
        //Enable reordering the list
        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(recyclerList);
        callback.setLongPressDragEnabled(false);
        callback.setItemViewSwipeEnabled(false);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(templateList);
        templateList.setAdapter(recyclerList);
//        recyclerList.setOnItemClickListener(new ListAction() {
//            public void action(int position) {
//                if (position == recyclerList.getItems().size() - 1)
//                    return;
//                String tempItem = recyclerList.getItems().get(position).getSaveString();
//                switchForResult(TemplatesSettingsMaskActivity.class, REQUEST_REFRESH,
//                        new Portable(template_pass, tempItem));
//            }
//        });

        loadGames();
    }

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_scoreboard);
    }

    private boolean checkApiHandler() {
        apiHandler = createApiHandler();
        return apiHandler != null;
    }

    public void loadGames() {
        swipeContainer.setRefreshing(true);
        new Thread(() -> {
            if (!checkApiHandler())
                return;
            try {
                final List<UserData> _games = apiHandler.getScoreboard();
                List<UserData> users = new ArrayList<>();
                if (currentUser == null)
                    currentUser = apiHandler.getYourself();
                for (UserData ud : _games)
                    if (ud.getElo() != 0 || ud.getId() == currentUser.getId())
                        users.add(ud);
                handler.post(() -> {
                    recyclerList.getItems().clear();
                    recyclerList.getItems().addAll(users);
                    templateList.setAdapter(recyclerList);
                    swipeContainer.setRefreshing(false);
                });

            } catch (NoConnectionException | SessionErrorException | JSONException e) {
                //should not happen
                e.printStackTrace();
            } catch (NoScoreboardException e) {

            }
        }).start();
    }
}
