package im.actor.sdk.view.emoji.stickers;

import im.actor.core.entity.Sticker;

public class StickerLine {
    Sticker[] line;
    int packCount;

    public StickerLine(Sticker[] line, int packCount) {
        this.line = line;
        this.packCount = packCount;
    }

    public int getPackCount() {
        return packCount;
    }

    public Sticker[] getLine() {
        return line;
    }
}
