package im.actor.messenger.app.emoji.stickers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import im.actor.messenger.R;
import im.actor.messenger.app.emoji.keyboard.OnStickerClickListener;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final OnStickerClickListener onStickerClickListener;
    private final int[] stickerPack;

    public StickersAdapter(Context context, OnStickerClickListener onStickerClickListener, int[] stickerPack) {
        this.context = context;
        this.onStickerClickListener = onStickerClickListener;
        this.stickerPack = stickerPack;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.adapter_sticker, parent, false);
        return new RecyclerView.ViewHolder(itemView) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ImageView)holder.itemView).setImageResource(stickerPack[position]);
    }

    @Override
    public int getItemCount() {
        return stickerPack.length;
    }
}
