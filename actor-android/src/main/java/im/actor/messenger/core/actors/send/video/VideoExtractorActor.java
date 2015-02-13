package im.actor.messenger.core.actors.send.video;

import android.annotation.TargetApi;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.Log;
import com.droidkit.actors.*;
import com.droidkit.actors.messages.PoisonPill;

import im.actor.messenger.util.io.StreamingUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by ex3ndr on 20.09.14.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VideoExtractorActor extends Actor {

    public static final ActorSelection extractor(final String fileName, final ActorRef next) {
        return new ActorSelection(Props.create(VideoExtractorActor.class, new ActorCreator<VideoExtractorActor>() {
            @Override
            public VideoExtractorActor create() {
                return new VideoExtractorActor(fileName, next);
            }
        }), "video_extractor_" + UUID.randomUUID());
    }

    private static final String TAG = "VideoExtractor";

    private String fileName;
    private ActorRef nextActor;

    private ByteBuffer inputBuffer;
    private MediaExtractor extractor;

    public VideoExtractorActor(String fileName, ActorRef nextActor) {
        this.fileName = fileName;
        this.nextActor = nextActor;
    }


    @Override
    public void preStart() {
        super.preStart();

        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(fileName);
        int height = Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int width = Integer.parseInt(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));

        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(fileName);
            int trackIndex = selectTrack(extractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + fileName);
            }
            extractor.selectTrack(trackIndex);

            nextActor.send(new NotifyMediaFormat(width, height, extractor.getTrackFormat(trackIndex)));

            inputBuffer = ByteBuffer.allocate(5 * 1024 * 1024);

            self().sendOnce(new DoIteration());
        } catch (IOException e) {
            e.printStackTrace();
            stop();
            return;
        }
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof DoIteration) {
            int size = 0;
            if ((size = extractor.readSampleData(inputBuffer, 0)) >= 0) {
                long presentationTimeUs = extractor.getSampleTime();
                int flags = extractor.getSampleFlags();
                byte[] data = new byte[size];
                inputBuffer.get(data, 0, size);
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    StreamingUtils.writeInt(size, outputStream);
                    StreamingUtils.writeLong(presentationTimeUs, outputStream);
                    StreamingUtils.writeInt(flags, outputStream);
                    StreamingUtils.writeBytes(data, outputStream);
                    nextActor.send(new FilePipelineActor.FilePart(outputStream.toByteArray()));
                } catch (IOException e) {
                    e.printStackTrace();
                    stop();
                    return;
                }
                self().sendOnce(new DoIteration());
            } else {
                nextActor.send(new FilePipelineActor.FileEnd());
            }
        }
    }

    private void stop() {
        nextActor.send(PoisonPill.INSTANCE);
        context().stopSelf();
    }

    /**
     * Selects the video track, if any.
     *
     * @return the track index, or -1 if no video track is found.
     */
    private static int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                return i;
            }
        }

        return -1;
    }

    private static class DoIteration {

    }

    public static class NotifyMediaFormat {
        private int width;
        private int height;
        private MediaFormat mediaFormat;

        public NotifyMediaFormat(int width, int height, MediaFormat mediaFormat) {
            this.width = width;
            this.height = height;
            this.mediaFormat = mediaFormat;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public MediaFormat getMediaFormat() {
            return mediaFormat;
        }
    }
}