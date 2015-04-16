/*
 * Copyright 2014 Ankush Sachdeva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.actor.messenger.app.emoji.keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
* @author Daniele Ricci
*/
public class EmojiRecentsController extends ArrayList<Long> {

    private static final String PREFERENCE_NAME = "emojicon";
    private static final String PREF_RECENTS = "recent_emojis";
    private static final String PREF_PAGE = "recent_page";

    private static final Object LOCK = new Object();
    private static EmojiRecentsController sInstance;

    private Context mContext;

    private EmojiRecentsController(Context context) {
        mContext = context.getApplicationContext();
        loadRecents();
    }

    public static EmojiRecentsController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new EmojiRecentsController(context);
                }
            }
        }
        return sInstance;
    }

    public int getRecentPage() {
        return getPreferences().getInt(PREF_PAGE, 0);
    }

    public void setRecentPage(int page) {
        getPreferences().edit().putInt(PREF_PAGE, page).commit();
    }

    public void push(Long object) {
        // FIXME totally inefficient way of adding the emoji to the adapter
        // TODO this should be probably replaced by a deque
        if (contains(object)) {
            super.remove(object);
        }
        add(0, object);
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