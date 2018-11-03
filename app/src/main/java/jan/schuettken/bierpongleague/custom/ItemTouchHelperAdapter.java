package jan.schuettken.bierpongleague.custom;

/**
 * Created by Jan Schüttken on 27.09.2018 at 17:57
 */
public interface ItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
