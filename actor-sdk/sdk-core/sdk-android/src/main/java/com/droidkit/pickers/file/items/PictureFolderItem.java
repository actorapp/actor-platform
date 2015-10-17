package com.droidkit.pickers.file.items;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import im.actor.messenger.R;

/**
 * Created by kiolt_000 on 16/09/2014.
 */
public class PictureFolderItem extends FolderItem {
    private final String bucketName;
    private final int bucketId;
    String albumImage = null;
    private int imgCounter = 0;

    public PictureFolderItem(int bucketId, String bucketName) {
        super("");
        this.bucketId = bucketId;
        this.bucketName = bucketName;
    }

    @Override
    public String getTitle() {
        return bucketName;
    }

    @Override
    public String getSubtitle(Context context) {
        return "" + imgCounter;
    }

    @Override
    public String getPath() {
        return "" + bucketId;
    }

    @Override
    public void bindData(View itemView) {
        super.bindData(itemView);

        TextView subTitleView = (TextView) itemView.findViewById(R.id.subtitle);

        subTitleView.setVisibility(View.VISIBLE);


    }

    @Override
    public void bindImage(View itemView) {

        final ImageView holder = (ImageView) itemView.findViewById(R.id.image);
        if (holder != null) {
            holder.setImageResource(R.drawable.example_user_placeholder);


            //ImageLoader.getInstance().displayImage("file://"+ albumImage, holder);

        }
        // super.bindImage(itemView);
    }

    public void putImage(String imgUri) {
        if (albumImage == null)
            albumImage = imgUri;
        imgCounter++;
    }
}
