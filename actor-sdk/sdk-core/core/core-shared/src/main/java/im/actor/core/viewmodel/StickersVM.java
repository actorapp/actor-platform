package im.actor.core.viewmodel;

import java.util.ArrayList;

import im.actor.core.entity.StickerPack;
import im.actor.runtime.mvvm.ValueModel;

public class StickersVM {

    private ValueModel<ArrayList<StickerPack>> ownStickerPacks;

    public StickersVM() {
        ownStickerPacks = new ValueModel<>("stickers.own",new ArrayList<StickerPack>());
    }

    public ValueModel<ArrayList<StickerPack>> getOwnStickerPacks() {
        return ownStickerPacks;
    }
}
