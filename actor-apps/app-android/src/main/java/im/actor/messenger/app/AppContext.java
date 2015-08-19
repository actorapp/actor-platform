/**
 * File created on 27/06/14 at 23:03
 * Copyright Vyacheslav Krylov, 2014
 */
package im.actor.messenger.app;

import android.content.Context;

import java.io.File;

import im.actor.messenger.app.util.RandomUtil;

public class AppContext {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context c) {
        context = c;
    }

    public static String getExternalTempFile(String prefix, String postfix) {
        File externalFile = AppContext.getContext().getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();

        File dest = new File(externalPath + "/actor/tmp/");
        dest.mkdirs();

        File outputFile = new File(dest, prefix + "_" + RandomUtil.randomId() + "." + postfix);

        return outputFile.getAbsolutePath();
    }

    public static String getInternalTempFile(String prefix, String postfix) {
        String externalPath;
        File externalFile = AppContext.getContext().getFilesDir();
        if (externalFile == null) {
            externalPath = "data/data/".concat(context.getPackageName()).concat("/files");
        } else {
            externalPath = externalFile.getAbsolutePath();
        }

        File dest = new File(externalPath + "/actor/tmp/");
        dest.mkdirs();
        if (!dest.exists()) return null;

        File outputFile = new File(dest, prefix + "_" + RandomUtil.randomId() + "." + postfix);
        return outputFile.getAbsolutePath();
    }
}
