package im.actor.messenger.app.keyboard.emoji.stickers;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

import im.actor.messenger.R;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final OnStickerClickListener onStickerClickListener;
    private final StickersPack stickerPack;
    private final int page;
    private final int itemHeight;

    public StickersAdapter(Context context, OnStickerClickListener onStickerClickListener, StickersPack stickerPack, int page, int itemHeight) {
        this.context = context;
        this.onStickerClickListener = onStickerClickListener;
        this.stickerPack = stickerPack;
        this.page = page;
        this.itemHeight = itemHeight;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SimpleDraweeView drawee = new SimpleDraweeView(context);
        drawee.setAdjustViewBounds(true);
        drawee.setBackgroundResource(R.drawable.clickable_background);
        return new RecyclerView.ViewHolder(drawee) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String packId = stickerPack.getId();
        final String stickerId = stickerPack.getStickerId(position + this.page * 8);
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
        int offset = page * 8;
        if (stickerPack.size() < offset + 8) {
            return stickerPack.size() - offset;
        }
        return 8;
    }
}