package im.actor.sdk.controllers.pickers.file.util;

import im.actor.sdk.controllers.pickers.file.items.ExplorerItem;

import java.util.Comparator;

/**
 * Created by kiolt_000 on 14/09/2014.
 */
public abstract class FileOrderComparator implements Comparator<ExplorerItem> {
    @Override
    public int compare(ExplorerItem explorerItem, ExplorerItem explorerItem2) {
        if (explorerItem.isDirectory()) {
            if (explorerItem2.isDirectory()) {
                return compareFiles(explorerItem, explorerItem2);
            } else {
                return -1;
            }
        }

        if (!explorerItem2.isDirectory()) {
            return compareFiles(explorerItem, explorerItem2);
        }
        return 1;
    }

    protected abstract int compareFiles(ExplorerItem explorerItem, ExplorerItem explorerItem2);
}
