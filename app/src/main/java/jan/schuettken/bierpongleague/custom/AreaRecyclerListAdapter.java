package jan.schuettken.bierpongleague.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.data.AreaData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.handler.ApiHandler;

/**
 * Created by Jan Schüttken on 28.09.2018 at 14:18
 */

public class AreaRecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemTouchHelperAdapter {

    private List<AreaData> items = new LinkedList<>();
    private ListAction action;
    private BasicDrawerPage context;
    private View view;
    private ApiHandler apiHandler;
    private UserData currentUser;


    public AreaRecyclerListAdapter(BasicDrawerPage context) {
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

    public List<AreaData> getItems() {
        return items;
    }

    public void setItems(List<AreaData> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_area, parent, false);
        view = parent;
        return new ItemViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, final int position) {

        View vi = holder.view;


        final AreaData area = items.get(position);
        TextView name;

        name = vi.findViewById(R.id.area_name);
        name.setText(area.getName());
//        if(position<3)
//            name.setTextColor(Color.BLACK);
//        else
//            name.setTextColor(Color.WHITE);
        name = vi.findViewById(R.id.are_info);
        name.setText("");
//        if(position<3)
//            name.setTextColor(Color.BLACK);
//        else
//            name.setTextColor(Color.WHITE);
        name = vi.findViewById(R.id.user_in_area);

        vi.findViewById(R.id.imageView_admin).setVisibility(
                area.isAdmin(currentUser)
                        ? View.VISIBLE : View.GONE);
        ImageView iv = vi.findViewById(R.id.imageView_type);
        RelativeLayout rl = vi.findViewById(R.id.color_region);
        Drawable d,bg;
        if (area.getType() == AreaData.TYPE_PUBLIC){
            d = context.getDrawable(R.drawable.ic_public_black_24dp);
            bg = context.getDrawable(R.drawable.color_green_gradient);
            name.setText("");
        }
        else if (area.getType() == AreaData.TYPE_PRIVATE){
            d = context.getDrawable(R.drawable.ic_private_black_24dp);
            bg = context.getDrawable(R.drawable.color_blue_gradient);
            name.setText(area.getUsers().size()+"");
        }
        else{
            d = context.getDrawable(R.drawable.ic_public_black_24dp);
            bg = context.getDrawable(R.drawable.color_green_gradient);
            name.setText("");
        }
        ColorFilter filter = new LightingColorFilter( Color.WHITE, Color.WHITE);
        assert d != null;
        d.setColorFilter(filter);
        iv.setImageDrawable(d);
        rl.setBackground(bg);

        //active click action
        holder.itemView.setOnClickListener(v -> action.action(position));
    }

    private void showConfirmInfoAndReload(Handler handler) {
        handler.post(() -> {
            context.showToast(R.string.game_confirmed);
            showProgress(false);
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
}



