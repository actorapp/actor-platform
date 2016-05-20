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
    private StickersAdapter stickersAdapter;

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

        GridLayoutManager layoutManager = new GridLayoutManager(context, Screen.getWidth() / Screen.dp(70));
        setLayoutManager(layoutManager);


        stickersAdapter = new StickersAdapter(keyboard, this);
        setAdapter(stickersAdapter);

        RecyclerView packSwitch = new RecyclerView(context);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, HORIZONTAL, false);
        packSwitch.setLayoutManager(linearLayoutManager);
        packSwitch.setItemAnimator(null);
        packSwitch.setHasFixedSize(true);
        PacksAdapter packsAdapter = new PacksAdapter(context, stickersAdapter, keyboard.getStickerIndicatorContainer());
        packSwitch.setAdapter(packsAdapter);
        stickersAdapter.setPacksAdapter(packsAdapter);
        keyboard.getStickerIndicatorContainer().removeAllViews();
        keyboard.getStickerIndicatorContainer().addView(packSwitch, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    public void relesase() {
        if (stickersAdapter != null) {
            stickersAdapter.release();
        }
    }

}