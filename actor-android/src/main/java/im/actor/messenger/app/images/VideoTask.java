package im.actor.messenger.app.images;

import com.droidkit.images.loading.AbsTask;

/**
 * Created by ex3ndr on 20.09.14.
 */
public class VideoTask extends AbsTask {
    private String fileName;

    public VideoTask(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public String getKey() {
        return "video:" + fileName;
    }
}
