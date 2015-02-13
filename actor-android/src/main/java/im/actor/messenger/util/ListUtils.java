package im.actor.messenger.util;

import android.view.View;
import android.widget.ListView;

/**
 * Created by ex3ndr on 09.09.14.
 */
public class ListUtils {
    public static VisibleViewItem[] dumpState(ListView listView) {
        int childCount = listView.getChildCount();

        int idCount = 0;
        int headerCount = 0;
        for (int i = 0; i < childCount; i++) {
            int index = listView.getFirstVisiblePosition() + i;
            long id = listView.getItemIdAtPosition(index);
            if (id > 0) {
                idCount++;
            } else {
                headerCount++;
            }
        }

        VisibleViewItem[] res = new VisibleViewItem[idCount];
        int resIndex = 0;
        for (int i = 0; i < childCount; i++) {
            View v = listView.getChildAt(i);
            int index = listView.getFirstVisiblePosition() + i;
            long id = listView.getItemIdAtPosition(index);
            if (id > 0) {
                int top = ((v == null) ? 0 : v.getTop()) - listView.getPaddingTop();
                res[resIndex++] = new VisibleViewItem(index + headerCount, top, id);
            }
        }

        return res;
    }
}
