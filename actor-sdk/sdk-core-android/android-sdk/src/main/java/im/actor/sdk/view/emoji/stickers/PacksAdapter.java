package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.actor.core.api.ApiStickerDescriptor;
import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.util.Screen;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class PacksAdapter extends BindedListAdapter<StickersPack, PacksAdapter.StickerHolder> {

    private Context context;
    private StickersAdapter stickersAdapter;

    public PacksAdapter(BindedDisplayList<StickersPack> displayList, Context context, StickersAdapter stickersAdapter) {
        super(displayList);
        this.context = context;
        this.stickersAdapter = stickersAdapter;

    }

    @Override
    public StickerHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new StickerHolder(context, new FrameLayout(context));

    }

    @Override
    public void onBindViewHolder(StickerHolder holder, int index, StickersPack item) {
        holder.bind(item);
    }

    @Override
    public void onViewRecycled(StickerHolder holder) {
        holder.unbind();
    }

    public class StickerHolder extends BindedViewHolder {

        private StickerView sv;
        private StickersPack sp;
        private Sticker s;

        public StickerHolder(Context context, FrameLayout fl) {
            super(fl);
            sv = new StickerView(context);
            int padding = Screen.dp(2);
            sv.setPadding(padding, padding, padding, padding);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Screen.dp(20), Screen.dp(20), Gravity.CENTER);
            fl.addView(sv, params);
            fl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sp != null) {
                        stickersAdapter.scrollToSticker(new Sticker(null, 0, s.getLocalCollectionId(), 0, true));
                    }
                }
            });
        }


        public void bind(StickersPack sp) {
            this.sp = sp;
            s = sp.getStickers().get(0);
            sv.bind(s, StickerView.STICKER_SMALL);

        }

        public void unbind() {

        }


    }


}
