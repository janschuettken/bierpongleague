package jan.schuettken.bierpongleague.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import jan.schuettken.bierpongleague.data.GameData;

/**
 * Created by Jan Schüttken on 28.09.2018 at 14:18
 */

public class GameRecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemTouchHelperAdapter {

    private LinkedList<GameData> items = new LinkedList<>();
    private ListAction action;
    private Context context;
    private View view;

    public GameRecyclerListAdapter(Context context) {
        this.context = context;
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

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {

        View vi = holder.view;


        GameData i = items.get(position);
        TextView player;
        player = vi.findViewById(R.id.name_player_a_first_name);
        player.setText(i.getParticipant(0).getFirstName());
        player = vi.findViewById(R.id.name_player_a_last_name);
        player.setText(i.getParticipant(0).getLastName());

        player = vi.findViewById(R.id.name_player_b_first_name);
        player.setText(i.getParticipant(1).getFirstName());
        player = vi.findViewById(R.id.name_player_b_last_name);
        player.setText(i.getParticipant(1).getLastName());

        player = vi.findViewById(R.id.name_player_c_first_name);
        player.setText(i.getParticipant(2).getFirstName());
        player = vi.findViewById(R.id.name_player_c_last_name);
        player.setText(i.getParticipant(2).getLastName());

        player = vi.findViewById(R.id.name_player_d_first_name);
        player.setText(i.getParticipant(3).getFirstName());
        player = vi.findViewById(R.id.name_player_d_last_name);
        player.setText(i.getParticipant(3).getLastName());

        player = vi.findViewById(R.id.text_score_team_a);
        player.setText(i.getScores()[0] + "");

        player = vi.findViewById(R.id.text_score_team_b);
        player.setText(i.getScores()[1] + "");

        if (i.getScores()[0] < i.getScores()[1]) {//the blue/green team is always the winner
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

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                action.action(position);
//            }
//        });
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



