package im.actor.model.mvvm;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.engine.ListEngineCallback;
import im.actor.model.droidkit.engine.ListEngineDisplayExt;
import im.actor.model.droidkit.engine.ListEngineItem;

/**
 * Created by ex3ndr on 14.03.15.
 */
public class BindedDisplayList<T extends BserObject & ListEngineItem> extends DisplayList<T> {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final Comparator<ListEngineItem> COMPARATOR = new ListEngineComparator();

    private int pageSize = DEFAULT_PAGE_SIZE;

    private boolean isInited = false;

    private final ListEngineDisplayExt<T> listEngine;
    private final DisplayWindow window;

    public BindedDisplayList(ListEngineDisplayExt<T> listEngine) {
        super(new Hook<T>() {
            @Override
            public void beforeDisplay(List<T> list) {
                Collections.sort(list, COMPARATOR);
            }
        });

        this.listEngine = listEngine;
        this.window = new DisplayWindow();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (isInited) {
            throw new RuntimeException("Unable to change page size after initialization");
        }
        this.pageSize = pageSize;
    }

    public void initTop() {
        if (isInited) {
            throw new RuntimeException("Already initialized list");
        }
        isInited = true;

        window.startInitForward();
        listEngine.loadTop(pageSize, new ListEngineCallback<T>() {
            @Override
            public void onLoaded(List<T> items, long topSortKey, long bottomSortKey) {
                editList(DisplayModifications.addOrUpdate(items));
                window.stopInitForward(bottomSortKey);
            }
        });
    }

//    public void initCenter(long sortKey) {
//        if (isInited) {
//            throw new RuntimeException("Already initialized list");
//        }
//        isInited = true;
//    }

    private static class ListEngineComparator implements Comparator<ListEngineItem> {

        @Override
        public int compare(ListEngineItem o1, ListEngineItem o2) {
            long lKey = o1.getEngineSort();
            long rKey = o2.getEngineSort();

            if (lKey > rKey) {
                return -1;
            } else if (lKey < rKey) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}