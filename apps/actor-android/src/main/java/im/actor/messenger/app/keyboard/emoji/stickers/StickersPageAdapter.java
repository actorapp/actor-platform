package im.actor.messenger.app.keyboard.emoji.stickers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;

import im.actor.messenger.R;
import im.actor.messenger.app.emoji.stickers.StickersPack;

import static im.actor.messenger.app.Core.getStickerProcessor;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersPageAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final OnStickerClickListener onStickerClickListener;
    private final StickersPack stickerPack;
    private final int page;
    private final int itemHeight;

    public StickersPageAdapter(Context context, OnStickerClickListener onStickerClickListener, StickersPack stickerPack, int page, int itemHeight) {
        this.context = context;
        this.onStickerClickListener = onStickerClickListener;
        this.stickerPack = stickerPack;
        this.page = page;
        this.itemHeight = itemHeight;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SimpleDraweeView drawee = new SimpleDraweeView(context, new GenericDraweeHierarchyBuilder(context.getResources()).setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE).build());
        drawee.setAdjustViewBounds(true);
        drawee.setBackgroundResource(R.drawable.clickable_background);
        return new RecyclerView.ViewHolder(drawee) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String packId = stickerPack.getId();
        final String stickerId = stickerPack.getStickerId(position + this.page * 8);
        getStickerProcessor().bindSticker((SimpleDraweeView) holder.itemView, packId, stickerId);
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
        int offset = page * 8;
        if (stickerPack.size() < offset + 8) {
            return stickerPack.size() - offset;
        }
        return 8;
    }
}