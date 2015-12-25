package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.viewmodel.StickerPackVM;
import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.runtime.mvvm.ValueModel;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.adapters.HolderAdapter;
import im.actor.sdk.view.adapters.ViewHolder;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

class StickerAdapter extends HolderAdapter<StickerLine> {
    private static final int TAG_KEY = R.id.sticker_pager;
    ValueModel<ArrayList<StickerPackVM>> packs;
    StickerLine[] stickerLines;
    EmojiKeyboard keyboard;
    private int stickerInLine;
    private final int STICKER_SIZE = Screen.dp(80);
    private final int STICKER_PADDING = Screen.dp(5);
    private int leftPadding;
    //TODO limit cache
    static HashMap<Long, StickerView> stickersCache;
    private int totalLines = 0;
    ScrollTo scrollTo;
    private LinearLayout stickerIndicatorContainer;
    private Object position;
    private Context context;
    private final int padding;
    private final HashMap<Integer, Integer> packFirstLineMap;
    private final LinearLayout.LayoutParams stickerSwitchLp;

    public StickerAdapter(Context context, EmojiKeyboard keyboard, final ScrollTo scrollTo) {
        super(context);
        this.context = context;
        this.keyboard = keyboard;
        if (stickersCache == null) {
            stickersCache = new HashMap<Long, StickerView>();
        }
        this.scrollTo = scrollTo;

        //Build sticker lines
        stickerIndicatorContainer = keyboard.getStickerIndicatorContainer();
        stickerIndicatorContainer.setTag(TAG_KEY, 0);

        stickerInLine = (Screen.getWidth() / STICKER_SIZE);
        leftPadding = (Screen.getWidth() - (stickerInLine * STICKER_SIZE)) / 2;

        packFirstLineMap = new HashMap<Integer, Integer>();

        stickerSwitchLp = new LinearLayout.LayoutParams(Screen.dp(48), Screen.dp(48));
        padding = Screen.dp(8);

        packs = messenger().getOwnStickerPacks();

        ((BaseActivity) context).bind(packs, new ValueChangedListener<ArrayList<StickerPackVM>>() {
            @Override
            public void onChanged(ArrayList<StickerPackVM> val, Value<ArrayList<StickerPackVM>> valueModel) {
                buildStickerLines(scrollTo);
                notifyDataSetChanged();
            }
        });


    }

    private void buildStickerLines(final ScrollTo scrollTo) {

        //BUILD SWITCH
        keyboard.getStickerIndicatorContainer().removeAllViews();

        //Add pack switch buttons
        int packCount = 0;
        totalLines = 0;
        for (final StickerPackVM pack : packs.get()) {
            ((BaseActivity) context).bind(pack.getStickers(), new ValueChangedListener<ArrayList<Sticker>>() {
                @Override
                public void onChanged(ArrayList<Sticker> val, Value<ArrayList<Sticker>> valueModel) {
                    buildStickerLines(scrollTo);
                    notifyDataSetChanged();
                }
            }, false);
            if (pack.getStickers().get().size() < 1) {
                continue;
            }
            //Count lines
            int linesInPack = (int) Math.ceil((double) pack.getStickers().get().size() / stickerInLine);
            totalLines += linesInPack;

            //Build packs buttons
            final StickerView sv = new StickerView(context);

            sv.bind(pack.getStickers().get().get(0), StickerView.STICKER_SMALL);
            sv.setPadding(padding, padding, padding, padding);
            sv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollTo.requestScroll(packFirstLineMap.get(pack.getId()));
                    selectCurrentPack((Integer) sv.getTag(TAG_KEY));
                }
            });
            sv.setTag(TAG_KEY, packCount);
            keyboard.getStickerIndicatorContainer().addView(sv, stickerSwitchLp);
            packCount++;
        }
        if (stickerIndicatorContainer.getChildAt(0) != null) {
            stickerIndicatorContainer.getChildAt(0).setBackgroundColor(context.getResources().getColor(R.color.selector_selected));
        }

        //BUILD LINES
        //All lines stored here
        stickerLines = new StickerLine[totalLines];


        //Fill lines with packs stickers
        int allLinesCount = 0;
        int linePackCount = 0;
        for (StickerPackVM pack : packs.get()) {
            if (pack.getStickers().get().size() < 1) {
                continue;
            }
            int stickerInPack = 0;
            int linesInPack = (int) Math.ceil((double) pack.getStickers().get().size() / stickerInLine);

            //Loop pack lines
            for (int lineInPackCount = 0; lineInPackCount < linesInPack; lineInPackCount++, allLinesCount++) {
                StickerLine line = new StickerLine(new Sticker[stickerInLine], linePackCount);
                //Remember pack first line position
                if (lineInPackCount == 0) {
                    packFirstLineMap.put(pack.getId(), allLinesCount);
                }
                //Fill line with stickers
                for (int stickerInLine = 0; stickerInLine < this.stickerInLine; stickerInLine++, stickerInPack++) {
                    if (stickerInPack < pack.getStickers().get().size()) {
                        line.getLine()[stickerInLine] = pack.getStickers().get().get(stickerInPack);
                    } else {
                        break;
                    }
                }
                stickerLines[allLinesCount] = line;
            }
            linePackCount++;
        }
    }

    @Override
    public int getCount() {
        return totalLines;
    }

    @Override
    public StickerLine getItem(int position) {
        return stickerLines[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    protected ViewHolder<StickerLine> createHolder(StickerLine obj) {
        return new StickerLineViewHolder();
    }


    class StickerLineViewHolder extends ViewHolder<StickerLine> {
        LinearLayout ll;
        LinearLayout.LayoutParams stikerlp = new LinearLayout.LayoutParams(STICKER_SIZE, STICKER_SIZE);


        @Override
        public View init(StickerLine data, ViewGroup viewGroup, Context context) {
            ll = new LinearLayout(context);
            ll.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, STICKER_SIZE));
            ll.setPadding(leftPadding, 0, 0, 0);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            return ll;
        }

        @Override
        public void bind(StickerLine data, int position, Context context) {
            ll.removeAllViews();
            ll.setTag(TAG_KEY, data.getPackCount());
            for (final Sticker s : data.getLine()) {
                StickerView sv;
                if (s != null && s.getFileReference128() != null) {

                    sv = stickersCache.get(s.getFileReference128().getFileId());
                    if (sv == null) {
                        sv = new StickerView(context);
                        sv.setPadding(STICKER_PADDING, STICKER_PADDING, STICKER_PADDING, STICKER_PADDING);
                        sv.bind(s, StickerView.STICKER_SMALL);
                        stickersCache.put(s.getFileReference128().getFileId(), sv);

                    } else if (sv.isLoaded()) {
                        sv.shortenFade();
                    }

                    if (sv.getParent() != null) {
                        ((LinearLayout) sv.getParent()).removeView(sv);
                    }

                    final StickerView finalSv = sv;
                    sv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (s.getThumb() == null && finalSv.isLoaded()) {
                                s.setThumb(finalSv.getThumb());
                            }
                            keyboard.onStickerClicked(s);
                        }
                    });
                    ll.addView(sv, stikerlp);
                }
            }


        }

        @Override
        public void unbind() {
            super.unbind();
            ll.removeAllViews();
        }
    }

    public void onScroll(View view) {
        if (view != null) {
            if (stickerIndicatorContainer.getTag(TAG_KEY) != null && !stickerIndicatorContainer.getTag(TAG_KEY).equals(view.getTag(TAG_KEY))) {
                stickerIndicatorContainer.setTag(TAG_KEY, view.getTag(TAG_KEY));
                position = view.getTag(TAG_KEY);
                selectCurrentPack((Integer) position);
            }
        }
    }

    private void selectCurrentPack(int position) {
        for (int i = 0; i < stickerIndicatorContainer.getChildCount(); i++) {
            if (stickerIndicatorContainer.getChildAt(i) != null) {
                stickerIndicatorContainer.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            }
        }
        if (stickerIndicatorContainer.getChildAt(position) != null) {
            stickerIndicatorContainer.getChildAt(position).setBackgroundColor(stickerIndicatorContainer.getContext().getResources().getColor(R.color.selector_selected));
        }
    }

    public interface ScrollTo {
        void requestScroll(int position);
    }

}
