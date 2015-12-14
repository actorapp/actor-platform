/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.entity.content.internal;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersPack {
    private Sticker[] order;
    private String packId;
    private Sticker logoId;
    private String title;

    public StickersPack(String packId, String packTitle, Sticker logoId, Sticker[] order) {
        this.order = order;
        this.packId = packId;
        this.logoId = logoId;
        this.title = packTitle;
    }

    public StickersPack(Sticker[] array) {
        this("", "", null, array);
    }


    public int size() {
        return order.length;
    }

    public String getId() {
        return packId;
    }

    public Sticker getLogoStickerId() {
        return logoId;
    }

    public Sticker get(int position) {
        return order[position];
    }
}