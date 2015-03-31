package im.actor.android;

import im.actor.android.crypto.PRNGFixes;
import im.actor.model.jvm.JavaRandomProvider;

/**
 * Created by ex3ndr on 29.03.15.
 */
public class AndroidRandomProvider extends JavaRandomProvider {
    private static boolean isFixApplied = false;

    public AndroidRandomProvider() {
        if (isFixApplied) {
            isFixApplied = true;
            PRNGFixes.apply();
        }
    }
}
