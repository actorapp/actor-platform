/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.android;

import im.actor.android.crypto.PRNGFixes;
import im.actor.model.jvm.JavaRandomProvider;

public class AndroidRandomProvider extends JavaRandomProvider {
    private static boolean isFixApplied = false;

    public AndroidRandomProvider() {
        if (isFixApplied) {
            isFixApplied = true;
            PRNGFixes.apply();
        }
    }
}
