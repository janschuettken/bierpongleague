package jan.schuettken.bierpongleague.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.LinkedList;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.activities.ScoreboardActivity;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.handler.ApiHandler;

/**
 * Created by Jan Schüttken on 28.09.2018 at 14:18
 */

public class ScoreboardRecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemTouchHelperAdapter {

    private LinkedList<UserData> items = new LinkedList<>();
    private ListAction action;
    private ScoreboardActivity context;
    private View view;
    private ApiHandler apiHandler;
    private UserData currentUser;


    public ScoreboardRecyclerListAdapter(ScoreboardActivity context) {
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

    public LinkedList<UserData> getItems() {
        return items;
    }

    public void setItems(LinkedList<UserData> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_scoreboard, parent, false);
        view = parent;
        return new ItemViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {

        View vi = holder.view;


        final UserData user = items.get(position);
        TextView player;

        player = vi.findViewById(R.id.name_player_first_name);
        player.setText(user.getFirstName());
        if(position<3)
            player.setTextColor(Color.BLACK);
        else
            player.setTextColor(Color.WHITE);
        player = vi.findViewById(R.id.name_player_last_name);
        player.setText(user.getLastName());
        if(position<3)
            player.setTextColor(Color.BLACK);
        else
            player.setTextColor(Color.WHITE);
        player = vi.findViewById(R.id.text_elo_player);
        player.setText(((int) user.getElo()) + " ");

        vi.findViewById(R.id.imageView_crone).setVisibility(View.INVISIBLE);
        RelativeLayout rl = vi.findViewById(R.id.color_region_player);

        if (position == 0) {//the blue/green team is always the winner
            Drawable d = context.getDrawable(R.drawable.color_gold_gradient);
            rl.setBackground(d);
            vi.findViewById(R.id.imageView_crone).setVisibility(View.VISIBLE);
        } else if (position == 1) {
            Drawable d = context.getDrawable(R.drawable.color_silver_gradient);
            rl.setBackground(d);
        } else if (position == 2) {
            Drawable d = context.getDrawable(R.drawable.color_bronze_gradient);
            rl.setBackground(d);
        }else if (position < 10) {
            Drawable d = context.getDrawable(R.drawable.color_green_gradient);
            rl.setBackground(d);
        } else if (user.getElo()<0){
            Drawable d = context.getDrawable(R.drawable.color_red_gradient);
            rl.setBackground(d);
        }else{
            Drawable d = context.getDrawable(R.drawable.color_prime_gradient);
            rl.setBackground(d);
        }
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



