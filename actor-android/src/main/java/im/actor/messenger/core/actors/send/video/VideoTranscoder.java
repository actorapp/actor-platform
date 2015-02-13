package im.actor.messenger.core.actors.send.video;

import android.os.Build;
import im.actor.messenger.core.actors.send.video.gl.GLTranscoder;

import java.io.IOException;

/**
 * Created by ex3ndr on 15.02.14.
 */
public class VideoTranscoder {

    public static boolean transcodeVideo(String source, String dest) throws IOException {
        if (Build.VERSION.SDK_INT >= 18) {
            return GLTranscoder.transcode(source, dest);
        }

        return false;
    }
}