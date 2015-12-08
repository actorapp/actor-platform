package im.actor.sdk.controllers.pickers.file.items;

import im.actor.sdk.controllers.pickers.file.ExploreItemViewHolder;

import java.io.File;

import im.actor.sdk.R;

/**
 * Created by kiolt_000 on 14/09/2014.
 */
public class FolderItem extends ExplorerItem {

    private String name;

    public FolderItem(File file) {
        super(file, false, null, R.drawable.picker_folder, true);

    }

    public FolderItem(String path) {
        super(new File(path), false, null, R.drawable.picker_folder, true);
    }


    public FolderItem(File file, int imageId) {

        super(file, false, null, imageId, true);
    }

    public FolderItem(File file, int imageId, boolean locked) {
        super(file, false, null, imageId, !locked);
    }

    public FolderItem(File file, int imageId, String name) {
        super(file, false, "", imageId, true);
        this.name = name;
    }


    @Override
    public String getTitle() {
        if (name != null) {
            return name;
        }
        return super.getTitle();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }


    @Override
    public void bindData(ExploreItemViewHolder holder) {
        holder.setTitle(getTitle());
        holder.disableSubtitle();
    }
}