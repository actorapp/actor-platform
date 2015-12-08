/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.sdk.view.emoji.smiles;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.StringTokenizer;

import im.actor.runtime.android.AndroidContext;

/**
 * Created by Jesus Christ. Amen.
 */
public class SmilesRecentsController extends ArrayList<Long> {

    private static final String PREFERENCE_NAME = "smiles";
    private static final String PREF_RECENTS = "recent_smiles";

    private static final Object LOCK = new Object();
    private static SmilesRecentsController sInstance;

    private Context mContext;

    private SmilesRecentsController(Context context) {
        mContext = context.getApplicationContext();
        loadRecents();
    }

    public static SmilesRecentsController getInstance() {
        return getInstance(AndroidContext.getContext());
    }

    public static SmilesRecentsController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new SmilesRecentsController(context);
                }
            }
        }
        return sInstance;
    }


    public void push(Long object) {
        if (contains(object)) {
            super.remove(object);
        }
        add(0, object);
        while(size()>32){
            remove(32);
        }
    }

    @Override
    public boolean add(Long object) {
        boolean ret = super.add(object);
        return ret;
    }

    @Override
    public void add(int index, Long object) {
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
                add(Long.valueOf(tokenizer.nextToken()));
            } catch (NumberFormatException e) {
                // ignored
            }
        }
    }

    public void saveRecents() {
        StringBuilder str = new StringBuilder();
        int c = size();
        for (int i = 0; i < c; i++) {
            Long e = get(i);
            str.append(e);
            if (i < (c - 1)) {
                str.append('~');
            }
        }
        SharedPreferences prefs = getPreferences();
        prefs.edit().putString(PREF_RECENTS, str.toString()).apply();
    }

}