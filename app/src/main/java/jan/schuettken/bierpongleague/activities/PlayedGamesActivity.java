package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.custom.GameRecyclerListAdapter;
import jan.schuettken.bierpongleague.custom.SimpleItemTouchHelperCallback;
import jan.schuettken.bierpongleague.data.AreaData;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.NoGamesException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;

public class PlayedGamesActivity extends BasicDrawerPage {

    private Handler handler;
    private GameRecyclerListAdapter recyclerList;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView templateList;
    private ApiHandler apiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_played_games);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.played_games);
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

        recyclerList = new GameRecyclerListAdapter(this);
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
        selectPage(R.id.nav_played_games);
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
                final List<AreaData> areas = apiHandler.getAreas();
                final List<GameData> games = apiHandler.getGames(apiHandler.getYourself(), true);
                apiHandler.getGameAdmins(games);
                handler.post(() -> {
                    findViewById(R.id.no_games_played_warning).setVisibility(View.GONE);
                    recyclerList.setAreas(areas);
                    recyclerList.getItems().clear();
                    recyclerList.getItems().addAll(games);
                    templateList.setAdapter(recyclerList);
                    swipeContainer.setRefreshing(false);
                });

            } catch (NoConnectionException | SessionErrorException | JSONException | NoGamesException e) {
                //no games are played
                handler.post(() -> {
                    findViewById(R.id.no_games_played_warning).setVisibility(View.VISIBLE);
                    swipeContainer.setRefreshing(false);
                });
            }
        }).start();
    }

    private void setRefreshing(final boolean refresh) {
        handler.post(() -> swipeContainer.setRefreshing(refresh));
    }
}
