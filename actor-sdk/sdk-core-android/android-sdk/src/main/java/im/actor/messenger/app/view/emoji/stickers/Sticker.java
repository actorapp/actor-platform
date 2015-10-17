/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.messenger.app.view.emoji.stickers;

/**
 * Created by Jesus Christ. Amen.
 */
public class Sticker {
    private final String stickerId;
    private final String packId;

    public Sticker(String stickerId, String packId){
        this.stickerId = stickerId;
        this.packId = packId;
    }

    public String getId() {
        return stickerId;
    }

    public String getPackId() {
        return packId;
    }

    public static Sticker parse(String string) {
        String[] tokens = string.split("_");
        return new Sticker(tokens[1], tokens[0]);
    }

    @Override
    public String toString() {
        return  packId + "_" + stickerId;
    }
}
