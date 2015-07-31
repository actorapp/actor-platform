/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.messenger.app.view.emoji.stickers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.StringTokenizer;

import im.actor.messenger.app.AppContext;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickerRecentController extends ArrayList<Sticker> {
    
    
    



    private static final String PREFERENCE_NAME = "Sticker";
    private static final String PREF_RECENTS = "recent_Sticker";

    private static final Object LOCK = new Object();
    private static StickerRecentController sInstance;

    private Context mContext;

    private StickerRecentController(Context context) {
        mContext = context.getApplicationContext();
        loadRecents();
    }

    public static StickerRecentController getInstance() {
        return getInstance(AppContext.getContext());
    }

    public static StickerRecentController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new StickerRecentController(context);
                }
            }
        }
        return sInstance;
    }


    public void push(Sticker object) {
        if (contains(object)) {
            super.remove(object);
        }
        add(0, object);
        while(size()>32){
            remove(32);
        }
    }

    @Override
    public boolean add(Sticker object) {
        boolean ret = super.add(object);
        return ret;
    }

    @Override
    public void add(int index, Sticker object) {
        super.add(index, object);
    }

    @Override
    public boolean remove(Object object) {
        boolean ret = super.remove(object);
        return ret;
    }

    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    private void loadRecents() {
        SharedPreferences prefs = getPreferences();
        String str = prefs.getString(PREF_RECENTS, "");
        StringTokenizer tokenizer = new StringTokenizer(str, "~");
        while (tokenizer.hasMoreTokens()) {
            try {
                add(Sticker.parse(tokenizer.nextToken()));
            } catch (NumberFormatException e) {
                // ignored
            }
        }
    }

    public void saveRecents() {
        StringBuilder str = new StringBuilder();
        int c = size();
        for (int i = 0; i < c; i++) {
            Sticker e = get(i);
            str.append(e);
            if (i < (c - 1)) {
                str.append('~');
            }
        }
        SharedPreferences prefs = getPreferences();
        prefs.edit().putString(PREF_RECENTS, str.toString()).apply();
    }

    public StickersPack getPack() {
        Sticker[] array = new Sticker[size()];
        this.toArray(array);
        return new StickersPack(array);
    }
}
