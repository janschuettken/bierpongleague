package jan.schuettken.bierpongleague.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import org.json.JSONException;

import java.util.List;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.custom.GameRecyclerListAdapter;
import jan.schuettken.bierpongleague.custom.SimpleItemTouchHelperCallback;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.exceptions.DatabaseException;
import jan.schuettken.bierpongleague.exceptions.EmptyPreferencesException;
import jan.schuettken.bierpongleague.exceptions.InvalidLoginException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.PreferencesHandler;

public class PlayedGamesActivity extends BasicDrawerPage {

    private Handler handler;
    private GameRecyclerListAdapter recyclerList;
    private RecyclerView templateList;
    private ApiHandler apiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_played_games);
        handler = new Handler();

        initializeList();
    }

    private void initializeList() {
        templateList = findViewById(R.id.listView_elements);
        templateList.setHasFixedSize(true);
        templateList.setLayoutManager(new LinearLayoutManager(this));

        recyclerList = new GameRecyclerListAdapter(this);
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

    private boolean createApiHandler() {
        PreferencesHandler preferencesHandler = new PreferencesHandler(this);
        try {
            //get Session if available
            apiHandler = new ApiHandler(preferencesHandler.getSessionId());
            return true;
        } catch (EmptyPreferencesException e) {
            try {
                apiHandler = new ApiHandler(preferencesHandler.getUsername(), preferencesHandler.getPassword());
                return true;
            } catch (NoConnectionException | DatabaseException e1) {
                //TODO Try again Later
                switchView(LoginActivity.class, true);
                return false;
            } catch (InvalidLoginException | EmptyPreferencesException e1) {
                //You shouldn't be logged in
                switchView(LoginActivity.class, true);
                return false;
            }
        }
    }

    private void loadGames() {
        new Thread() {
            @Override
            public void run() {
                if (!createApiHandler())
                    return;
                try {
                    final List<GameData> games = apiHandler.getGames(apiHandler.getYourself());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerList.getItems().addAll(games);
                            templateList.setAdapter(recyclerList);
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
