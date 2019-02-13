package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.custom.GameRecyclerListAdapter;
import jan.schuettken.bierpongleague.custom.SimpleItemTouchHelperCallback;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
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

    private void initializeRefreshListener(){
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadGames();
                //swipeContainer.setRefreshing(false);
            }
        });
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
        new Thread() {
            @Override
            public void run() {
                if (!checkApiHandler())
                    return;
                try {
                    final List<GameData> games = apiHandler.getGames(apiHandler.getYourself(), true);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerList.getItems().clear();
                            recyclerList.getItems().addAll(games);
                            templateList.setAdapter(recyclerList);
                            swipeContainer.setRefreshing(false);
                        }
                    });

                } catch (NoConnectionException | SessionErrorException | JSONException e) {
                    //should not happen
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
