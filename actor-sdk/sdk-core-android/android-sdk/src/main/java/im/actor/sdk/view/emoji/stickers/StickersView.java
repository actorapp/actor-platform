package im.actor.sdk.view.emoji.stickers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import im.actor.core.api.ApiStickerCollection;
import im.actor.core.api.ApiStickerDescriptor;
import im.actor.core.api.rpc.ResponseLoadOwnStickers;
import im.actor.core.entity.content.internal.Sticker;
import im.actor.core.entity.content.internal.StickersPack;
import im.actor.core.viewmodel.CommandCallback;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class StickersView extends ListView {
    boolean disableWhileFastScroll = false;
    EmojiKeyboard keyboard;
    private StickerAdapter adapter;

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


        setDivider(new ColorDrawable(Color.TRANSPARENT));

        buildAdapter(context);
    }

    private void buildAdapter(Context context) {
        adapter = new StickerAdapter(context, keyboard, new StickerAdapter.ScrollTo() {
            @Override
            public void requestScroll(final int position) {

                disableWhileFastScroll = true;
                post(new Runnable() {
                    @Override
                    public void run() {
                        setSelection(position);
                        View v = getChildAt(position);
                        if (v != null) {
                            v.requestFocus();
                        }
                    }
                });
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        disableWhileFastScroll = false;
                    }
                }, 50);
            }
        });
        setAdapter(adapter);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (adapter != null && !disableWhileFastScroll) {
            adapter.onScroll(getChildAt(0));
        }
    }
}