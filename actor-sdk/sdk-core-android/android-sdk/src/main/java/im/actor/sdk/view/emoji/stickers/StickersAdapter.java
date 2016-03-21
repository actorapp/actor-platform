package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import im.actor.core.entity.Sticker;
import im.actor.runtime.android.view.BindedListAdapter;
import im.actor.runtime.android.view.BindedViewHolder;
import im.actor.runtime.generic.mvvm.BindedDisplayList;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

public class StickersAdapter extends BindedListAdapter<Sticker, StickersAdapter.StickerHolder> {

    private Context context;
    private EmojiKeyboard keyboard;
    RecyclerView recyclerView;
    BindedDisplayList<Sticker> displayList;
    int topPack = -1;
    PacksAdapter packsAdapter;

    public StickersAdapter(BindedDisplayList<Sticker> displayList, Context context, final RecyclerView recyclerView, EmojiKeyboard keyboard) {
        super(displayList);
        this.displayList = displayList;
        this.context = context;
        this.keyboard = keyboard;
        this.recyclerView = recyclerView;
        final GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (getItemViewType(position) == 1) {
                    return layoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            Sticker s;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                s = getItem(firstVisiblePosition);
                int newTopPack = s.getStickerCollectionId();
                if (newTopPack != topPack) {
                    topPack = newTopPack;
                    if (packsAdapter != null) {
                        packsAdapter.selectPack(newTopPack);
                    }
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        Sticker item = getItem(position);
//        if (item.isHeader()) {
//            return 1;
//        } else {
//            return 0;
//        }
        return 0;
    }

    @Override
    public StickerHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            default:
            case 0:
                return ActorSDK.sharedActor().getDelegatedViewHolder(StickerHolder.class, new ActorSDK.OnDelegateViewHolder<StickerHolder>() {
                    @Override
                    public StickerHolder onNotDelegated() {
                        return new StickerHolder(context, new FrameLayout(context));
                    }
                }, context, new FrameLayout(context));
            case 1:
                return new StickerPackHeaderHolder(context, new FrameLayout(context));
        }

    }

    @Override
    public void onBindViewHolder(StickerHolder holder, int index, Sticker item) {
        holder.bind(item);
    }

    @Override
    public void onViewRecycled(StickerHolder holder) {
        holder.unbind();
    }

    public void scrollToSticker(Sticker s) {
        int position = displayList.getPosition(s);

        if (position != -1) {
            ((GridLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
        } else {
            displayList.initCenter(s.getEngineSort(), true);
        }
    }

    public class StickerHolder extends BindedViewHolder {

        private StickerView sv;
        private Sticker s;

        public StickerHolder(Context context, FrameLayout fl) {
            super(fl);
            sv = new StickerView(context);
            int padding = Screen.dp(5);
            sv.setPadding(padding, padding, padding, padding);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(Screen.dp(70), Screen.dp(70), Gravity.CENTER);
            fl.addView(sv, params);
            fl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (s != null) {
                        s.setThumb(sv.getThumb());
                        keyboard.onStickerClicked(s);
                    }
                }
            });
        }


        public void bind(Sticker s) {
            this.s = s;
            sv.bind(s, StickerView.STICKER_SMALL);

        }

        public void unbind() {

        }


    }

    public class StickerPackHeaderHolder extends StickerHolder {

        public StickerPackHeaderHolder(Context context, FrameLayout fl) {
            super(context, fl);
            fl.removeAllViews();
        }

        @Override
        public void bind(Sticker s) {

        }
    }

    public void setPacksAdapter(PacksAdapter packsAdapter) {
        this.packsAdapter = packsAdapter;
    }
}
