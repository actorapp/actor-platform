package im.actor.messenger.app.keyboard.emoji.stickers;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersPack {
    private final String[] idsOrder;
    private final String packId;
    private final String logoId;

    public StickersPack(String packId, String logoId, String[] idsOrder) {
        this.idsOrder = idsOrder;
        this.packId = packId;
        this.logoId = logoId;
    }


    public int size() {
        return idsOrder.length;
    }

    public String getId() {
        return packId;
    }

    public String getStickerId(int position) {
        return idsOrder[position];
    }

    public String getLogoStickerId() {
        return logoId;
    }
}