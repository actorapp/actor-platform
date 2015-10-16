package im.actor.messenger.app.view.keyboard.emoji.smiles;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Arrays;

import im.actor.messenger.R;
import im.actor.messenger.app.view.emoji.smiles.SmilesPack;
import im.actor.messenger.app.view.emoji.smiles.SmilesPackView;
import im.actor.messenger.app.view.keyboard.emoji.EmojiKeyboard;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.PagerSlidingTabStrip;

import static im.actor.messenger.app.core.Core.getSmileProcessor;

/**
* Created by Jesus Christ. Amen.
*/
public class SmilePagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.TabProvider {

    private EmojiKeyboard emojiKeyboard;

    public SmilePagerAdapter(EmojiKeyboard emojiKeyboard) {
        this.emojiKeyboard = emojiKeyboard;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.emoji_smiles_page, null);
        ViewGroup emojicontainer = (ViewGroup) itemView.findViewById(R.id.emojiPackContainer);

        ArrayList<Long> emojiPack = new ArrayList<Long>();
        switch (position) {
            case 0:
                emojiPack = SmilesPack.getRecent();
                break;
            case 1:
                emojiPack = new ArrayList<Long>(Arrays.asList(SmilesPack.STANDART));
                break;
            case 2:
                emojiPack = new ArrayList<Long>(Arrays.asList(SmilesPack.NATURE));
                break;
            case 3:
                emojiPack = new ArrayList<Long>(Arrays.asList(SmilesPack.TRANSPORT));
                break;
            case 4:
                emojiPack = new ArrayList<Long>(Arrays.asList(SmilesPack.UNSORTED));
                break;

        }

        int emojisMaxRowCount = 8;
        int emojiSize = Screen.dp(45);
        int emojiPadding = emojiSize / 5;
        if (Screen.getWidth() / emojiSize < emojisMaxRowCount) {
            emojisMaxRowCount = Screen.getWidth() / emojiSize;
        }
        final SmilesPackView smilesPackView = new SmilesPackView(container.getContext(), getSmileProcessor(), emojiPack, emojisMaxRowCount, emojiSize, emojiPadding);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        emojicontainer.addView(smilesPackView, params);
        if(!getSmileProcessor().isLoaded()){
            getSmileProcessor().registerListener(new SmilesListener() {
                @Override
                public void onSmilesUpdated(boolean completed) {
                    smilesPackView.update();
                    getSmileProcessor().unregisterListener(this);
                }
            });
        }
        // is this necessary?
        /*if(position==0){
            getSmileProcessor().setRecentUpdateListener(new SmilesRecentListener() {
                @Override
                public void onSmilesUpdated() {
                    smilesPackView.update();
                }
            });
        }*/
        smilesPackView.setOnSmileClickListener(new OnSmileClickListener() {
            @Override
            public void onEmojiClicked(String smile) {
                emojiKeyboard.onEmojiClicked(smile);
            }
        });
        SmilesPack.setOnRecentChangeListener(new OnRecentChangeListener(){

            @Override
            public void onRecentChange() {
                smilesPackView.update();
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
        int icon;
        switch (position) {
            case 0:
                icon = R.drawable.ic_smiles_recent;
                break;
            case 1:
                icon = R.drawable.ic_smiles_smile;
                break;
            case 2:
                icon = R.drawable.ic_smiles_bell;//R.drawable.ic_smiles_flower;
                break;
            /*case 3:
                icon = R.drawable.ic_smiles_bell;
                break;*/
            case  3://4:
                icon = R.drawable.ic_smiles_car;
                break;
            case 4://5:
                icon = R.drawable.ic_smiles_grid;
                break;
            default:
                icon = R.drawable.ic_smiles_smile;
        }
        tabView.setImageResource(icon);
        /*//} else{
                *//*tabView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                tabView.setAdjustViewBounds(true);
                //tabView.setCropToPadding(false);
                StickersPack pack = Stickers.getPacks()[position - 1];
                tabView.setImageURI(Uri.parse(Stickers.getFile(pack.getId(), pack.getLogoStickerId())));*//*
        //}*/
        return tabView;
    }

}