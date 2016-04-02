package im.actor.sdk.view.emoji.keyboard.emoji.smiles;

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

import im.actor.runtime.mvvm.MVVMCollection;
import im.actor.sdk.R;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.view.emoji.SmileProcessor;
import im.actor.sdk.view.emoji.smiles.SmilesPack;
import im.actor.sdk.view.emoji.smiles.SmilesPackView;
import im.actor.sdk.view.emoji.keyboard.emoji.EmojiKeyboard;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.PagerSlidingTabStrip;
import im.actor.sdk.view.emoji.stickers.StickersView;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

/**
* Created by Jesus Christ. Amen.
*/
public class SmilePagerAdapter extends PagerAdapter implements PagerSlidingTabStrip.TabProvider {

    private EmojiKeyboard emojiKeyboard;
    private int count;
    private PagerSlidingTabStrip tabs;
    private StickersView stickersView;

    public SmilePagerAdapter(EmojiKeyboard emojiKeyboard) {
        this.emojiKeyboard = emojiKeyboard;
//        count = messenger().getOwnStickerPacks().getValuesMap().values().size();
//        if (emojiKeyboard.getActivity() instanceof BaseActivity) {
//            messenger().getOwnStickerPacks().addCallback(new MVVMCollection.OnChangedListener() {
//                @Override
//                public void onChanged() {
//                    count = messenger().getOwnStickerPacks().getValuesMap().values().size();
//                    if (tabs != null) {
//                        tabs.notifyDataSetChanged();
//                    }
//                    notifyDataSetChanged();
//                }
//            });
//        }

    }


    @Override
    public int getCount() {
        return 6;//count > 0 ? 6 : 5;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView;
        if (position <= 4) {

            itemView = LayoutInflater.from(container.getContext()).inflate(R.layout.emoji_smiles_page, null);
            ViewGroup emojicontainer = (ViewGroup) itemView.findViewById(R.id.emojiPackContainer);
            View noEmojiTV = itemView.findViewById(R.id.text);

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
            final SmilesPackView smilesPackView = new SmilesPackView(container.getContext(), SmileProcessor.emoji(), emojiPack, emojisMaxRowCount, emojiSize, emojiPadding);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            emojicontainer.addView(smilesPackView, params);
            if (!SmileProcessor.emoji().isLoaded()) {
                SmileProcessor.emoji().registerListener(new SmilesListener() {
                    @Override
                    public void onSmilesUpdated(boolean completed) {
                        smilesPackView.update();
                        SmileProcessor.emoji().unregisterListener(this);
                    }
                });
            }
            if (emojiPack.size() == 0) {
                noEmojiTV.setVisibility(View.VISIBLE);
            } else {
                noEmojiTV.setVisibility(View.GONE);
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
            SmilesPack.setOnRecentChangeListener(new OnRecentChangeListener() {

                @Override
                public void onRecentChange() {
                    smilesPackView.update();
                }
            });

        } else {
            if (stickersView == null) {
                stickersView = new StickersView(container.getContext(), emojiKeyboard);
            }
            itemView = stickersView;
        }
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
            case 5:
                icon = R.drawable.ic_smiles_sticker;
                break;
            default:
                icon = R.drawable.ic_smiles_smile;
        }
        tabView.setImageResource(icon);
        /*//} else{
                *//*tabView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                tabView.setAdjustViewBounds(true);
                //tabView.setCropToPadding(false);
                StickerPack pack = Stickers.getPacks()[position - 1];
                tabView.setImageURI(Uri.parse(Stickers.getFile(pack.getId(), pack.getLogoStickerId())));*//*
        //}*/
        return tabView;
    }

    public void setTabs(PagerSlidingTabStrip tabs) {
        this.tabs = tabs;
    }

    public void release() {
        if (stickersView != null) {
            stickersView.relesase();
        }
    }
}