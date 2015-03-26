package im.actor.images.loading.tasks;

import android.net.Uri;
import android.provider.MediaStore;
import im.actor.images.loading.AbsTask;
import im.actor.images.util.HashUtil;

/**
 * Preview task for images from Media Store
 */
public class PreviewContentTask extends AbsTask {
    private Uri uri;
    private int kind;

    /**
     * @param uri  Media Store uri
     * @param kind MediaStore.Images.Thumbnails.MICRO_KIND or MediaStore.Images.Thumbnails.MINI_KIND
     */
    public PreviewContentTask(Uri uri, int kind) {
        if (kind != MediaStore.Images.Thumbnails.MICRO_KIND && kind != MediaStore.Images.Thumbnails.MINI_KIND) {
            throw new RuntimeException("Kind must be MICRO_KIND or MINI_KIND");
        }
        this.uri = uri;
        this.kind = kind;
    }

    public Uri getUri() {
        return uri;
    }

    public int getKind() {
        return kind;
    }

    @Override
    public String getKey() {
        return "uri:" + ((kind == MediaStore.Images.Thumbnails.MICRO_KIND) ? "micro" : "mini") + ":" + HashUtil.md5(uri.toString());
    }
}
