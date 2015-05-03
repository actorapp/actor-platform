package im.actor.messenger.app.keyboard.emoji.stickers;

/**
 * Created by Jesus Christ. Amen.
 */
public class StickersPack {
    private final String[] order;
    private final String packId;
    private final String logoId;
    private final String title;

    public StickersPack(String packId, String packTitle, String logoId, String[] order) {
        this.order = order;
        this.packId = packId;
        this.logoId = logoId;
        this.title = packTitle;
    }


    public int size() {
        return order.length;
    }

    public String getId() {
        return packId;
    }

    public String getStickerId(int position) {
        return order[position];
    }

    public String getLogoStickerId() {
        return logoId;
    }
}