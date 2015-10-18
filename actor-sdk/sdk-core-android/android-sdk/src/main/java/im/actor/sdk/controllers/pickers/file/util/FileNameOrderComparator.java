package im.actor.sdk.controllers.pickers.file.util;

import im.actor.sdk.controllers.pickers.file.items.ExplorerItem;

/**
 * Created by kiolt_000 on 07/10/2014.
 */
public class FileNameOrderComparator extends FileOrderComparator {
    @Override
    protected int compareFiles(ExplorerItem explorerItem, ExplorerItem explorerItem2) {
        return (explorerItem.getTitle().compareToIgnoreCase(explorerItem2.getTitle()));
    }
}
