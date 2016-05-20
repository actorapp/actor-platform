package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import im.actor.core.entity.FileReference;
import im.actor.core.entity.Sticker;
import im.actor.core.entity.StickerPack;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.util.Screen;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class PacksAdapter extends RecyclerView.Adapter<PacksAdapter.StickerViewHolder> {

    private LinearLayout packsSwitchContainer;
    ArrayList<Sticker> stickers = new ArrayList<>();
    private int selectedPostion = 0;
    Context context;
    StickersAdapter stickersAdapter;

    protected PacksAdapter(Context context, StickersAdapter stickersAdapter, LinearLayout stickerIndicatorContainer) {
        packsSwitchContainer = stickerIndicatorContainer;
        this.context = context;
        this.stickersAdapter = stickersAdapter;
        stickersAdapter.getBinder().bind(messenger().getAvailableStickersVM().getOwnStickerPacks(), new ValueChangedListener<ArrayList<StickerPack>>() {
            @Override
            public void onChanged(ArrayList<StickerPack> val, Value<ArrayList<StickerPack>> valueModel) {
                stickers.clear();
                for (StickerPack pack : val) {
                    if (pack.getStickers().size() > 0) {
                        stickers.add(pack.getStickers().get(0));
                    }
                }
            }
        });
    }


    @Override
    public StickerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new StickerViewHolder(context, new FrameLayout(context));

    }

    @Override
    public void onBindViewHolder(StickerViewHolder holder, int position) {
        holder.bind(stickers.get(position), position);
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }


    @Override
    public void onViewRecycled(StickerViewHolder holder) {
        holder.unbind();
    }

    public class StickerViewHolder extends RecyclerView.ViewHolder {

        private StickerView sv;
        private Sticker s;
        private int position;
        FrameLayout fl;

        public StickerViewHolder(Context context, FrameLayout fl) {
            super(fl);
            this.fl = fl;
            sv = new StickerView(context);
            int padding = Screen.dp(2);
            sv.setPadding(padding, padding, padding, padding);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(packsSwitchContainer.getHeight(), packsSwitchContainer.getHeight(), Gravity.CENTER);
            fl.addView(sv, params);

            fl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int oldSelected = selectedPostion;
                    selectedPostion = position;
                    notifyItemChanged(oldSelected);
                    notifyItemChanged(selectedPostion);
                    stickersAdapter.scrollToSticker(s);
                }
            });
        }


        public void bind(Sticker s, int position) {
            this.s = s;
            this.position = position;
            sv.bind(s.getImage128(), StickerView.STICKER_SMALL);
            if (selectedPostion == position) {
                fl.setBackgroundColor(Color.LTGRAY);
            } else {
                fl.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        public void unbind() {

        }


    }

    public void selectPack(int localPackId) {
        int oldSelected = selectedPostion;
        for (int i = 0; i < getItemCount(); i++) {
            Sticker p = stickers.get(i);
            Integer collectionId = p.getCollectionId();
            if (collectionId != null && collectionId == localPackId) {
                selectedPostion = i;
                notifyItemChanged(oldSelected);
                notifyItemChanged(selectedPostion);
//                notifyDataSetChanged();
                break;
            }
        }
    }
}
