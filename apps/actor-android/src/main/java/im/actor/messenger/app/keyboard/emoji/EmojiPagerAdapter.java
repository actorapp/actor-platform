package im.actor.messenger.app.keyboard.emoji;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import im.actor.messenger.R;
import im.actor.messenger.app.keyboard.emoji.smiles.SmilesPack;
import im.actor.messenger.app.keyboard.emoji.smiles.SmilesPackView;
import im.actor.messenger.app.keyboard.emoji.smiles.OnSmileClickListener;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.PagerSlidingTabStrip;

import static im.actor.messenger.app.Core.core;

/**
* Created by Jesus Christ. Amen.
*/
class EmojiPagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.TabProvider {

    private EmojiKeyboard emojiKeyboard;

    public EmojiPagerAdapter(EmojiKeyboard emojiKeyboard) {
        this.emojiKeyboard = emojiKeyboard;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.emoji_smiles_page, null);
        ViewGroup emojicontainer = (ViewGroup) itemView.findViewById(R.id.emojiPackContainer);

        long[] emojiPack = new long[0];
        switch (position) {
            case 0:
                emojiPack = SmilesPack.STANDART;
                break;
            case 1:
                emojiPack = SmilesPack.NATURE;
                break;
            case 2:
                emojiPack = SmilesPack.TRANSPORT;
                break;
            case 3:
                emojiPack = SmilesPack.UNSORTED;
                break;
        }

        int emojisMaxRowCount = 8;
        int emojiSize = Screen.dp(45);
        int emojiPadding = emojiSize / 5;
        if (Screen.getWidth() / emojiSize < emojisMaxRowCount) {
            emojisMaxRowCount = Screen.getWidth() / emojiSize;
        }
        SmilesPackView smilesPackView = new SmilesPackView(container.getContext(), core().getSmileProcessor(), emojiPack, emojisMaxRowCount, emojiSize, emojiPadding);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        emojicontainer.addView(smilesPackView, params);

        smilesPackView.setOnSmileClickListener(new OnSmileClickListener() {
            @Override
            public void onEmojiClicked(String smile) {
                emojiKeyboard.onEmojiClicked(smile);
            }
        });

        container.addView(itemView, 0);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(View view, Object key) {
        return key == view;
    }

    @Override
    public View getTab(int position, Context context) {

        ImageButton tabView = new ImageButton(context);
        //if(position==0){
        tabView.setImageResource(R.drawable.ic_emoji);
        tabView.setPadding(24, 0, 24, 0);
        //} else{
                /*tabView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                tabView.setAdjustViewBounds(true);
                //tabView.setCropToPadding(false);
                StickersPack pack = Stickers.getPacks()[position - 1];
                tabView.setImageURI(Uri.parse(Stickers.getFile(pack.getId(), pack.getLogoStickerId())));*/
        //}
        return tabView;
    }

}