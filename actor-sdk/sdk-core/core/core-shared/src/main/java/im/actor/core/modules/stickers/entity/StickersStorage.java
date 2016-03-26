package im.actor.core.modules.stickers.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.actor.core.entity.StickerPack;
import im.actor.runtime.bser.BserObject;
import im.actor.runtime.bser.BserValues;
import im.actor.runtime.bser.BserWriter;

public class StickersStorage extends BserObject {

    private List<StickerPack> stickerPacks = new ArrayList<>();

    public StickersStorage() {
    }

    public StickersStorage(byte[] data) throws IOException {
        load(data);
    }

    public List<StickerPack> getStickerPacks() {
        return stickerPacks;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        stickerPacks.clear();
        for (byte[] b : values.getRepeatedBytes(1)) {
            stickerPacks.add(new StickerPack(b));
        }
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeRepeatedObj(1, stickerPacks);
    }
}