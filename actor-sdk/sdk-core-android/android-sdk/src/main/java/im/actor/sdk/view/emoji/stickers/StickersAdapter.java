package im.actor.sdk.view.emoji.stickers;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.api.ApiStickerDescriptor;
import im.actor.core.entity.Sticker;
import im.actor.core.entity.StickerPack;
import im.actor.core.viewmodel.StickersVM;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.controllers.fragment.ActorBinder;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class StickersAdapter extends RecyclerView.Adapter<StickersAdapter.StickerViewHolder> {

    private ArrayList<Sticker> stickers = new ArrayList<>();
    private EmojiKeyboard keyboard;
    private PacksAdapter packsAdapter;
    private RecyclerView recyclerView;
    private ActorBinder binder;
    private boolean updatePackSelector = true;

    int topPack = -1;

    public StickersAdapter(EmojiKeyboard keyboard, RecyclerView recyclerView) {
        this.keyboard = keyboard;
        this.recyclerView = recyclerView;
        binder = new ActorBinder();

        binder.bind(messenger().getAvailableStickersVM().getOwnStickerPacks(), new ValueChangedListener<ArrayList<StickerPack>>() {
            @Override
            public void onChanged(ArrayList<StickerPack> val, Value<ArrayList<StickerPack>> valueModel) {
                stickers.clear();
                for (StickerPack pack : val) {
                    List<Sticker> stickers = pack.getStickers();
                    if (stickers.size() > 0) {
                        Sticker sticker = stickers.get(0);
                        StickersAdapter.this.stickers.add(new StickerCat(sticker.toApi(), sticker.getCollectionId(), sticker.getCollectionAccessHash()));
                    }
                    StickersAdapter.this.stickers.addAll(stickers);
                }
                notifyDataSetChanged();
            }
        });


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
                s = stickers.get(firstVisiblePosition);
                Integer newTopPack = s.getCollectionId();
                if (newTopPack != null && newTopPack != topPack) {
                    topPack = newTopPack;
                    if (packsAdapter != null && updatePackSelector) {
                        packsAdapter.selectPack(newTopPack);
                    } else {
                        updatePackSelector = true;
                    }
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return stickers.get(position) instanceof StickerCat ? 1 : 0;
    }

    @Override
    public StickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                StickerView itemView = new StickerView(parent.getContext());
                int stSize = Screen.dp(70);
                itemView.setLayoutParams(new FrameLayout.LayoutParams(stSize, stSize));
                return new StickerViewHolder(itemView);

            default:
                View cat = new View(parent.getContext());
                cat.setLayoutParams(new FrameLayout.LayoutParams(1, 1));
                return new StickerCatHolder(cat);
        }

    }

    @Override
    public void onBindViewHolder(StickerViewHolder holder, int position) {
        holder.bind(stickers.get(position));
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    public class StickerViewHolder extends RecyclerView.ViewHolder {

        Sticker s;
        StickerView sv;

        public StickerViewHolder(View itemView) {
            super(itemView);
            if (itemView instanceof StickerView) {
                int padding = Screen.dp(5);
                sv = (StickerView) itemView;
                sv.setPadding(padding, padding, padding, padding);
                sv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (s != null) {
//                        s.setThumb(sv.getThumb());
                            keyboard.onStickerClicked(s);
                        }
                    }
                });
            }
        }

        public void bind(Sticker s) {
            this.s = s;
            sv.bind(s.getImage256(), StickerView.STICKER_SMALL);
        }
    }

    private class StickerCatHolder extends StickerViewHolder {

        public StickerCatHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Sticker s) {

        }
    }

    private class StickerCat extends Sticker {

        public StickerCat(ApiStickerDescriptor descriptor, Integer collectionId, Long collectionAccessHash) {
            super(descriptor, collectionId, collectionAccessHash);
        }
    }

    public void setPacksAdapter(PacksAdapter packsAdapter) {
        this.packsAdapter = packsAdapter;
    }

    public void scrollToSticker(Sticker s) {
        updatePackSelector = false;
        int position = 0;
        for (Sticker st : stickers) {
            if (st.getId() == s.getId()) {
                break;
            }
            position++;
        }

        ((GridLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);

    }

    public ActorBinder getBinder() {
        return binder;
    }

    public void release() {
        if (binder != null) {
            binder.unbindAll();
        }
    }
}
