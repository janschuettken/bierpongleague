package jan.schuettken.bierpongleague.custom;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

import jan.schuettken.bierpongleague.handler.ColorFunctionHandler;

/**
 * Created by Jan Schüttken on 27.09.2018 at 17:56
 */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private boolean longPressDragEnabled = true, itemViewSwipeEnabled = true;
    private Drawable drawableSwipeLeft, drawableSwipeRight;
    private int dragFlags = Integer.MAX_VALUE, swipeFlags = Integer.MAX_VALUE;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * @param dragFlags  if disabled use ItemTouchHelper.START
     * @param swipeFlags if disabled use ItemTouchHelper.UP
     */
    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter, int dragFlags, int swipeFlags) {
        this(adapter);
        this.dragFlags = dragFlags;
        this.swipeFlags = swipeFlags;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return longPressDragEnabled;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return itemViewSwipeEnabled;
    }

    public void setLongPressDragEnabled(boolean longPressDragEnabled) {
        this.longPressDragEnabled = longPressDragEnabled;
    }

    public void setItemViewSwipeEnabled(boolean itemViewSwipeEnabled) {
        this.itemViewSwipeEnabled = itemViewSwipeEnabled;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags, swipeFlags;
        if (this.dragFlags == Integer.MAX_VALUE)
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        else
            dragFlags = this.dragFlags;
        if (this.swipeFlags == Integer.MAX_VALUE)
            swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        else
            swipeFlags = this.swipeFlags;

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder
            , float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get RecyclerView item from the ViewHolder
            View itemView = viewHolder.itemView;

            Paint p = new Paint();
            if (dX > 0) {
                /* Set your color for positive/left displacement */
                p.setColor(ColorFunctionHandler.setColor(0.5, 0, 1, 0));
                // Draw Rect with varying right side, equal to displacement dX
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), p);
                if (drawableSwipeLeft != null) {
                    Drawable d = drawableSwipeLeft;
                    int size = itemView.getBottom() - itemView.getTop() - 10;
                    int xStart = (int) ((itemView.getLeft() - size) + dX);
                    if (xStart > itemView.getLeft())
                        xStart = itemView.getLeft();
                    d.setBounds(xStart, itemView.getTop(), xStart + size, itemView.getTop() + size);
                    d.draw(c);
                }
            } else {
                /* Set your color for negative/right displacement */
                p.setColor(ColorFunctionHandler.setColor(0.5, 1, 0, 0));
                // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);
                if (drawableSwipeRight != null) {
                    Drawable d = drawableSwipeRight;
                    int size = itemView.getBottom() - itemView.getTop() - 10;
                    int xStart = (int) ((itemView.getRight())+dX);
                    if (xStart < itemView.getRight() - size)
                        xStart = itemView.getRight() - size;
                    d.setBounds(xStart, itemView.getTop(), xStart + size, itemView.getTop() + size);
                    d.draw(c);
                }
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    public void setSwipeLeftIcon(Drawable drawable) {
        drawableSwipeLeft = drawable;
    }

    public void setSwipeRightIcon(Drawable drawable) {
        drawableSwipeRight = drawable;
    }

}
