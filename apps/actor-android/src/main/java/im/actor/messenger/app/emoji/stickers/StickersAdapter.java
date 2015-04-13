package im.actor.messenger.app.emoji.stickers;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import im.actor.messenger.R;
import im.actor.messenger.app.emoji.keyboard.OnStickerClickListener;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final OnStickerClickListener onStickerClickListener;
    private final StickersPack stickerPack;

    public StickersAdapter(Context context, OnStickerClickListener onStickerClickListener, StickersPack stickerPack) {
        this.context = context;
        this.onStickerClickListener = onStickerClickListener;
        this.stickerPack = stickerPack;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SimpleDraweeView itemView = new SimpleDraweeView(context);
        itemView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        itemView.setAdjustViewBounds(true);
        itemView.setBackgroundResource(R.drawable.md_btn_selector_ripple);
        return new RecyclerView.ViewHolder(itemView){};
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String packId = stickerPack.getId();
        final String stickerId = stickerPack.getStickerId(position);
        Uri uri = Uri.parse("file://"+Stickers.getFile(packId, stickerId));
        ((ImageView)holder.itemView).setImageURI(uri);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStickerClickListener.onStickerClick(packId, stickerId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stickerPack.size();
    }
}
