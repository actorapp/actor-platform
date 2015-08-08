/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.utils;

import com.google.gwt.storage.client.Storage;

import java.util.Random;

public class IdentityUtils {
    public native static String getClientName()/*-{ return navigator.userAgent; }-*/;

    public static String getUniqueId() {
        Storage storage = Storage.getLocalStorageIfSupported();
        String id = storage.getItem("tech_unique_id");
        if (id != null) {
            return id;
        }
        Random rnd = new Random();
        id = "";
        for (int i = 0; i < 128; i++) {
            id += ((char) ('a' + rnd.nextInt('z' - 'a')));
        }
        storage.setItem("tech_unique_id", id);
        return id;
    }
}
