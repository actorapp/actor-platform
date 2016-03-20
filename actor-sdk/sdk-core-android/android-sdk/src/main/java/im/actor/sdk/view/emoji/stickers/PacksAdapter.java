package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import im.actor.core.api.ApiStickerDescriptor;
import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.runtime.Log;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.util.Screen;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class PacksAdapter extends BindedListAdapter<StickersPack, PacksAdapter.StickerHolder> {

    private Context context;
    private StickersAdapter stickersAdapter;
    LinearLayout packsSwitchContainer;
    int selectedPostion = -1;

    public PacksAdapter(BindedDisplayList<StickersPack> displayList, Context context, StickersAdapter stickersAdapter, LinearLayout stickerIndicatorContainer) {
        super(displayList);
        this.packsSwitchContainer = stickerIndicatorContainer;
        this.context = context;
        this.stickersAdapter = stickersAdapter;
        stickersAdapter.setPacksAdapter(this);
    }

    @Override
    public StickerHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new StickerHolder(context, new FrameLayout(context));

    }

    @Override
    public void onBindViewHolder(StickerHolder holder, int index, StickersPack item) {
        holder.bind(item, index);
    }

    @Override
    public void onViewRecycled(StickerHolder holder) {
        holder.unbind();
    }

    public class StickerHolder extends BindedViewHolder {

        private StickerView sv;
        private StickersPack sp;
        private Sticker s;
        private int position;
        FrameLayout fl;

        public StickerHolder(Context context, FrameLayout fl) {
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
                    if (sp != null) {
                        selectedPostion = position;
                        stickersAdapter.scrollToSticker(new Sticker(null, 0, sp.getLocalId(), 0, true));
                    }
                }
            });
        }


        public void bind(StickersPack sp, int position) {
            this.position = position;
            this.sp = sp;
            s = sp.getStickers().get(0);
            sv.bind(s, StickerView.STICKER_SMALL);
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
        for (int i = 0; i < getItemCount(); i++) {
            StickersPack p = getItem(i);
            if (p.getLocalId() == localPackId) {
                selectedPostion = i;
                notifyDataSetChanged();
                break;
            }
        }
    }


}
