package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import im.actor.sdk.util.Screen;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class StickersView extends RecyclerView {
    boolean disableWhileFastScroll = false;
    EmojiKeyboard keyboard;

    public StickersView(Context context, EmojiKeyboard keyboard) {
        super(context);
        this.keyboard = keyboard;
        init(context);
    }

    public StickersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickersView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    public void init(final Context context) {



        buildAdapter(context);
    }

    private void buildAdapter(Context context) {
//        adapter = new StickerAdapter(context, keyboard, new StickerAdapter.ScrollTo() {
//            @Override
//            public void requestScroll(final int position) {
//
//                disableWhileFastScroll = true;
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        scrollToPosition(position);
////                        setSelection(position);
//                        View v = getChildAt(position);
//                        if (v != null) {
//                            v.requestFocus();
//                        }
//                    }
//                });
//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        disableWhileFastScroll = false;
//                    }
//                }, 50);
//            }
//        });


//        setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(context, Screen.getWidth() / Screen.dp(70));
        setLayoutManager(layoutManager);


        StickersAdapter stickersAdapter = new StickersAdapter(messenger().getStickersDisplayList(), context, this, keyboard);
        setAdapter(stickersAdapter);

        RecyclerView packSwitch = new RecyclerView(context);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, HORIZONTAL, false);
        packSwitch.setLayoutManager(linearLayoutManager);
        PacksAdapter packsAdapter = new PacksAdapter(messenger().getStickersPacksDisplayList(), context, stickersAdapter, keyboard.getStickerIndicatorContainer());
        packSwitch.setAdapter(packsAdapter);
        keyboard.getStickerIndicatorContainer().removeAllViews();
        keyboard.getStickerIndicatorContainer().addView(packSwitch, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        if (adapter != null && !disableWhileFastScroll) {
//            adapter.onScroll(getChildAt(0));
//        }
    }
}