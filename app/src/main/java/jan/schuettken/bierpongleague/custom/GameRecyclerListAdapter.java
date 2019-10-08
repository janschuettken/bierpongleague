package jan.schuettken.bierpongleague.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
import java.util.List;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.activities.LoginActivity;
import jan.schuettken.bierpongleague.activities.PlayedGamesActivity;
import jan.schuettken.bierpongleague.data.AreaData;
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

    private List<GameData> items = new LinkedList<>();
    private List<AreaData> areas = new LinkedList<>();
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

    public List<GameData> getItems() {
        return items;
    }

    public void setItems(List<GameData> items) {
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
        if (game.getDate() != null) {
            StringBuilder head = new StringBuilder(format.format(game.getDate()));
            head.append(" @");
            for (AreaData ad : areas)
                if (ad.getId() == game.getAreaId())
                    head.append(ad.getName());
            date.setText(head.toString());
        } else
            date.setText("Date Error");
        setPlayerText(vi.findViewById(R.id.name_player_first_name), vi.findViewById(R.id.name_player_last_name), game, 0);
        setPlayerText(vi.findViewById(R.id.name_player_b_first_name), vi.findViewById(R.id.name_player_b_last_name), game, 1);
        setPlayerText(vi.findViewById(R.id.name_player_c_first_name), vi.findViewById(R.id.name_player_c_last_name), game, 2);
        setPlayerText(vi.findViewById(R.id.name_player_d_first_name), vi.findViewById(R.id.name_player_d_last_name), game, 3);
//        player = vi.findViewById(R.id.name_player_first_name);
//        player.setText(game.getParticipant(0).getFirstName());
//        player.setTextColor(context.getColor(R.color.colorGrey));
//        player.setTextColor(context.getColor(R.color.colorWhite));
//        player = vi.findViewById(R.id.name_player_last_name);
//        player.setText(game.getParticipant(0).getLastName());

//        player = vi.findViewById(R.id.name_player_b_first_name);
//        player.setText(game.getParticipant(1).getFirstName());
//        player = vi.findViewById(R.id.name_player_b_last_name);
//        player.setText(game.getParticipant(1).getLastName());
//
//        player = vi.findViewById(R.id.name_player_c_first_name);
//        player.setText(game.getParticipant(2).getFirstName());
//        player = vi.findViewById(R.id.name_player_c_last_name);
//        player.setText(game.getParticipant(2).getLastName());
//
//        player = vi.findViewById(R.id.name_player_d_first_name);
//        player.setText(game.getParticipant(3).getFirstName());
//        player = vi.findViewById(R.id.name_player_d_last_name);
//        player.setText(game.getParticipant(3).getLastName());

        player = vi.findViewById(R.id.text_score_team_a);
        player.setText(game.getScores()[0] + "");

        player = vi.findViewById(R.id.text_score_team_b);
        player.setText(game.getScores()[1] + "");

        if (game.getScores()[0] < game.getScores()[1]) {//the blue/green team is always the winner
            RelativeLayout rl = vi.findViewById(R.id.color_region_player);
            Drawable d = context.getDrawable(R.drawable.color_red_gradient);
            rl.setBackground(d);

            rl = vi.findViewById(R.id.color_region_b);
            d = context.getDrawable(R.drawable.color_green_gradient);
            rl.setBackground(d);
            vi.findViewById(R.id.imageView_crone).setVisibility(View.INVISIBLE);
            vi.findViewById(R.id.imageView_crone_team_b).setVisibility(View.VISIBLE);
        } else {
            RelativeLayout rl = vi.findViewById(R.id.color_region_player);
            Drawable d = context.getDrawable(R.drawable.color_green_gradient);
            rl.setBackground(d);

            rl = vi.findViewById(R.id.color_region_b);
            d = context.getDrawable(R.drawable.color_red_gradient);
            rl.setBackground(d);
            vi.findViewById(R.id.imageView_crone).setVisibility(View.VISIBLE);
            vi.findViewById(R.id.imageView_crone_team_b).setVisibility(View.INVISIBLE);
        }
        if (currentUser == null || game.hasConfirmed(currentUser.getId()))
            vi.findViewById(R.id.region_confirmGame).setVisibility(View.GONE);
        else {
            vi.findViewById(R.id.region_confirmGame).setVisibility(View.VISIBLE);
            Button right = vi.findViewById(R.id.button_confirm_yes);
            right.setOnClickListener(v -> confirmGame(game.getGameId(), true));
            Button wrong = vi.findViewById(R.id.button_confirm_no);
            wrong.setOnClickListener(v -> confirmGame(game.getGameId(), false));
        }
    }

    private void setPlayerText(TextView firstName, TextView lastName, GameData game, int participant) {
        firstName.setText(game.getParticipant(participant).getFirstName());
        lastName.setText(game.getParticipant(participant).getLastName());

        if (game.getConfirmedUser(participant) != -1) {
            firstName.setTextColor(context.getColor(R.color.colorWhite));
            lastName.setTextColor(context.getColor(R.color.colorWhite));
        } else {
            firstName.setTextColor(context.getColor(R.color.colorGreyLight));
            lastName.setTextColor(context.getColor(R.color.colorGreyLight));
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
                        apiHandler = new ApiHandler(prefHandler.getUsername(), prefHandler.getPassword(), context);
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
        handler.post(() -> {
            context.showToast(R.string.game_confirmed);
            showProgress(false);
            context.loadGames();
        });
    }

    private void showProgress(final boolean show, Handler handler) {
        handler.post(() -> showProgress(show));
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

    public List<AreaData> getAreas() {
        return areas;
    }

    public void setAreas(List<AreaData> areas) {
        this.areas = areas;
    }
}



