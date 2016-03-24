package im.actor.core.viewmodel;

import java.util.ArrayList;

import im.actor.core.entity.StickerPack;
import im.actor.runtime.mvvm.ValueModel;

public class GalleryVM {

    private ValueModel<ArrayList<String>> galleryMediaPath;

    public GalleryVM() {
        galleryMediaPath = new ValueModel<>("gallery.photo", new ArrayList<String>());
    }

    public ValueModel<ArrayList<String>> getGalleryMediaPath() {
        return galleryMediaPath;
    }
}
