package im.actor.messenger.app;

import java.io.File;

import im.actor.messenger.app.util.Randoms;
import im.actor.runtime.android.AndroidContext;

public class AppContext {

    public static String getExternalTempFile(String prefix, String postfix) {
        File externalFile = AndroidContext.getContext().getExternalFilesDir(null);
        if (externalFile == null) {
            return null;
        }
        String externalPath = externalFile.getAbsolutePath();

        File dest = new File(externalPath + "/actor/tmp/");
        dest.mkdirs();

        File outputFile = new File(dest, prefix + "_" + Randoms.randomId() + "" + postfix);

        return outputFile.getAbsolutePath();
    }

    public static String getInternalTempFile(String prefix, String postfix) {
        String externalPath;
        File externalFile = AndroidContext.getContext().getFilesDir();
        if (externalFile == null) {
            externalPath = "data/data/".concat(AndroidContext.getContext().getPackageName()).concat("/files");
        } else {
            externalPath = externalFile.getAbsolutePath();
        }

        File dest = new File(externalPath + "/actor/tmp/");
        dest.mkdirs();
        if (!dest.exists()) return null;

        File outputFile = new File(dest, prefix + "_" + Randoms.randomId() + "" + postfix);
        return outputFile.getAbsolutePath();
    }
}
