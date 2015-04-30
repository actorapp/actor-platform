package im.actor.messenger.app.keyboard.emoji.stickers;

import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

import im.actor.messenger.R;
import im.actor.messenger.app.util.Screen;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersFullpackAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final OnStickerClickListener onStickerClickListener;
    private final StickersPack stickerPack;
    private final int itemHeight;

    public StickersFullpackAdapter(Context context, OnStickerClickListener onStickerClickListener, StickersPack stickerPack, int itemHeight) {
        this.context = context;
        this.onStickerClickListener = onStickerClickListener;
        this.stickerPack = stickerPack;
        this.itemHeight = itemHeight;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // SimpleDraweeView drawee = new SimpleDraweeView(context);
        SimpleDraweeView drawee = new SimpleDraweeView(context, new GenericDraweeHierarchyBuilder(context.getResources()).setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE).build());
        drawee.setAdjustViewBounds(true);
        drawee.setBackgroundResource(R.drawable.clickable_background);
        return new RecyclerView.ViewHolder(drawee) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String packId = stickerPack.getId();
        final String stickerId = stickerPack.getStickerId(position);
        Uri uri = Uri.parse("file://" + Stickers.getFile(packId, stickerId));
        ((SimpleDraweeView) holder.itemView).setImageURI(uri);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStickerClickListener.onStickerClick(packId, stickerId);
            }
        });
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        ((SimpleDraweeView)holder.itemView).setImageURI(null);
    }

    @Override
    public int getItemCount() {
        return stickerPack.size();
    }
}