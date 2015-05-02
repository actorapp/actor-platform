package im.actor.messenger.app.keyboard.emoji.stickers;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.util.io.IOUtils;

/**
 * Created by Jesus Christ. Amen.
 */
public class Stickers {
    public final static StickersPack animalPack = new StickersPack("animals", "panda", new String[]{
            "panda",
            "doge",
            "bear",
            "koala",
            "grumpy",
            "dove",
            "chicken",
            "monkey",
            "elephant",
            "rat",
            "frog",
            "ping",
            "goat"

    });
    private static StickersPack steewiePack = new StickersPack("steewie", "ass", new String[]{
            "1",
            "2",
            "happy",
            "4",
            "ass",
            "fuck",
            "6",
            "7",
            "8",
            "9",
            "10",
            "ass",
            "brian"
    });
    private static StickersPack giraffePack = new StickersPack("giraffe", "flirt", new String[]{
            "approve",
            "cry",
            "earl",
            "flirt",
            "fly",
            "happy",
            "hugs",
            "ill",
            "kiss",
            "laugh",
            "love",
            "malicious",
            "sad",
            "steep",
            "sleep",
            "wrath"
    });

    public final static StickersPack[] getPacks() {
        // todo extrnal sticker packs??
        return new StickersPack[]{animalPack, steewiePack, giraffePack};
    }

    public static String getFile(String packId, String stickerId) {
        long timer = System.currentTimeMillis();
        Context context = AppContext.getContext();
        File sourceFile = context.getFileStreamPath("sticker_" + packId + "_" + stickerId + ".png");
        try {
            InputStream fileIS = AppContext.getContext().getAssets().open("stickers_" + packId + "/" + stickerId + ".png");
            IOUtils.copy(fileIS, sourceFile);
            fileIS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Sticker saving", "Starting time: " + (System.currentTimeMillis() - timer));
        return sourceFile.getAbsolutePath();
    }
}