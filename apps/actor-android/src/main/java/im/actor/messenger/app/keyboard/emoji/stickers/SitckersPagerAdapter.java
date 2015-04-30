package im.actor.messenger.app.keyboard.emoji.stickers;

import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.messenger.R;
import im.actor.messenger.app.keyboard.emoji.EmojiKeyboard;
import im.actor.messenger.app.util.Screen;

/**
* Created by Jesus Christ. Amen.
*/
public class SitckersPagerAdapter extends PagerAdapter {

    private EmojiKeyboard emojiKeyboard;
    private final StickersPack stickersPack;

    public SitckersPagerAdapter(EmojiKeyboard emojiKeyboard, StickersPack stickersPack) {
        this.emojiKeyboard = emojiKeyboard;
        this.stickersPack = stickersPack;
    }

    @Override
    public int getCount() {
        int pages = 0;
        pages = stickersPack.size() / 8;
        if (stickersPack.size() % 8 > 0) {
            pages++;
        }
        return pages;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View page = LayoutInflater.from(container.getContext()).inflate(R.layout.sticker_item_page, null);
        RecyclerView recycler = (RecyclerView) page.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(container.getContext(), 4));
        recycler.setAdapter(new StickersAdapter(container.getContext(), emojiKeyboard, stickersPack, position, (emojiKeyboard.getHeight() - Screen.dp(50)) / 2));
        container.addView(page);
        return page;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object key) {
        return key == view;
    }

}