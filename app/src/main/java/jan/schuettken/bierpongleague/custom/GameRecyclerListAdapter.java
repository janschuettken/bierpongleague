package jan.schuettken.bierpongleague.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        player = vi.findViewById(R.id.name_player_a);
        player.setText(i.getParticipant(0).getFullName());

        player = vi.findViewById(R.id.name_player_b);
        player.setText(i.getParticipant(1).getFullName());

        player = vi.findViewById(R.id.name_player_c);
        player.setText(i.getParticipant(2).getFullName());

        player = vi.findViewById(R.id.name_player_d);
        player.setText(i.getParticipant(3).getFullName());

        player = vi.findViewById(R.id.text_score_team_a);
        player.setText(i.getScores()[0]+"");

        player = vi.findViewById(R.id.text_score_team_b);
        player.setText(i.getScores()[1]+"");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.action(position);
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



