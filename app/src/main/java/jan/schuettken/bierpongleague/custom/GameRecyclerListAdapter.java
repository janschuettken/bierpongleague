package jan.schuettken.bierpongleague.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.activities.LoginActivity;
import jan.schuettken.bierpongleague.activities.PlayedGamesActivity;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.DatabaseException;
import jan.schuettken.bierpongleague.exceptions.EmptyPreferencesException;
import jan.schuettken.bierpongleague.exceptions.InvalidLoginException;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.PreferencesHandler;

/**
 * Created by Jan Schüttken on 28.09.2018 at 14:18
 */

public class GameRecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemTouchHelperAdapter {

    private LinkedList<GameData> items = new LinkedList<>();
    private ListAction action;
    private PlayedGamesActivity context;
    private View view;
    private ApiHandler apiHandler;
    private UserData currentUser;


    public GameRecyclerListAdapter(PlayedGamesActivity context) {
        this.context = context;
        this.apiHandler = context.createApiHandler();
    }

    @Override
    public void onItemDismiss(int position) {
        //Do nothing
//        GameData game = items.get(position);
//        items.remove(position);
//        notifyItemRemoved(position);

    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        itemMoved(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void setCurrentUser(UserData currentUser) {
        this.currentUser = currentUser;
    }

    public LinkedList<GameData> getItems() {
        return items;
    }

    public void setItems(LinkedList<GameData> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_game, parent, false);
        view = parent;
        return new ItemViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {

        View vi = holder.view;


        final GameData game = items.get(position);
        TextView player, date;
        date = vi.findViewById(R.id.text_game_date);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if (game.getDate() != null)
            date.setText(format.format(game.getDate()));
        else
            date.setText("Date Error");

        player = vi.findViewById(R.id.name_player_a_first_name);
        player.setText(game.getParticipant(0).getFirstName());
        player = vi.findViewById(R.id.name_player_a_last_name);
        player.setText(game.getParticipant(0).getLastName());

        player = vi.findViewById(R.id.name_player_b_first_name);
        player.setText(game.getParticipant(1).getFirstName());
        player = vi.findViewById(R.id.name_player_b_last_name);
        player.setText(game.getParticipant(1).getLastName());

        player = vi.findViewById(R.id.name_player_c_first_name);
        player.setText(game.getParticipant(2).getFirstName());
        player = vi.findViewById(R.id.name_player_c_last_name);
        player.setText(game.getParticipant(2).getLastName());

        player = vi.findViewById(R.id.name_player_d_first_name);
        player.setText(game.getParticipant(3).getFirstName());
        player = vi.findViewById(R.id.name_player_d_last_name);
        player.setText(game.getParticipant(3).getLastName());

        player = vi.findViewById(R.id.text_score_team_a);
        player.setText(game.getScores()[0] + "");

        player = vi.findViewById(R.id.text_score_team_b);
        player.setText(game.getScores()[1] + "");

        if (game.getScores()[0] < game.getScores()[1]) {//the blue/green team is always the winner
            RelativeLayout rl = vi.findViewById(R.id.color_region_a);
            Drawable d = context.getDrawable(R.drawable.color_team_b_gradient);
            rl.setBackground(d);

            rl = vi.findViewById(R.id.color_region_b);
            d = context.getDrawable(R.drawable.color_team_a_gradient);
            rl.setBackground(d);
            vi.findViewById(R.id.imageView_crone_team_a).setVisibility(View.INVISIBLE);
            vi.findViewById(R.id.imageView_crone_team_b).setVisibility(View.VISIBLE);
        } else {
            RelativeLayout rl = vi.findViewById(R.id.color_region_a);
            Drawable d = context.getDrawable(R.drawable.color_team_a_gradient);
            rl.setBackground(d);

            rl = vi.findViewById(R.id.color_region_b);
            d = context.getDrawable(R.drawable.color_team_b_gradient);
            rl.setBackground(d);
            vi.findViewById(R.id.imageView_crone_team_a).setVisibility(View.VISIBLE);
            vi.findViewById(R.id.imageView_crone_team_b).setVisibility(View.INVISIBLE);
        }
        if (currentUser == null || game.hasConfirmed(currentUser.getId()))
            vi.findViewById(R.id.region_confirmGame).setVisibility(View.GONE);
        else {
            vi.findViewById(R.id.region_confirmGame).setVisibility(View.VISIBLE);
            Button right = vi.findViewById(R.id.button_confirm_yes);
            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmGame(game.getGameId(), true);
                }
            });
            Button wrong = vi.findViewById(R.id.button_confirm_no);
            wrong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmGame(game.getGameId(), false);
                }
            });
        }
    }

    private void confirmGame(final int gameId, final boolean confirm) {
        showProgress(true);
        final Handler handler = new Handler();
        final PreferencesHandler prefHandler = new PreferencesHandler(context);

        new Thread() {
            @Override
            public void run() {

                try {//TODO test case
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
                if (apiHandler == null) {
                    Log.e("CONFIRM", "apiHandler == null");
                    return;
                }
                try {
                    if (apiHandler.confirmGame(gameId, confirm)) {
                        showConfirmInfoAndReload(handler);
                    }
                } catch (SessionErrorException | NoConnectionException e) {
                    e.printStackTrace();
                    try {
                        apiHandler = new ApiHandler(prefHandler.getUsername(), prefHandler.getPassword());
                        if (apiHandler.confirmGame(gameId, confirm)) {
                            showConfirmInfoAndReload(handler);
                        }
                    } catch (SessionErrorException | InvalidLoginException | NoConnectionException | DatabaseException | EmptyPreferencesException e1) {
                        e1.printStackTrace();
                        showProgress(false, handler);
                        context.switchView(LoginActivity.class, true, handler);
                    }
                }

            }
        }.start();

    }

    private void showConfirmInfoAndReload(Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                context.showToast(R.string.game_confirmed);
                showProgress(false);
                context.loadGames();
            }
        });
    }

    private void showProgress(final boolean show, Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showProgress(show);
            }
        });
    }

    private void showProgress(final boolean show) {
        try {
            showProgress(show, context.findViewById(R.id.progress_confirm_region), context.findViewById(R.id.progress_confirm_bg));
        } catch (NullPointerException ignored) {
        }
    }

    private void showProgress(final boolean show, final View foreground, final View background) {

        int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
        int longAnimTime = context.getResources().getInteger(android.R.integer.config_longAnimTime);

//        enableEditing(!show);
        foreground.setVisibility(show ? View.VISIBLE : View.GONE);
        background.animate().setDuration(longAnimTime).alpha(
                show ? 0.5f : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                foreground.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void itemMoved(int fromPosition, int toPosition) {
        //disable here
    }

    public void setOnItemClickListener(ListAction action) {
        setAction(action);
    }

    public void setAction(ListAction action) {
        this.action = action;
    }

    public void scroll(int index) {

    }
}



