package im.actor.messenger.core.actors.send.video.mux;

import android.media.MediaMetadataRetriever;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl;
import com.googlecode.mp4parser.util.Matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by ex3ndr on 17.02.14.
 */
public class CompatMuxer implements Muxer {
    @Override
    public synchronized void muxVideo(String sourceVideo, String videoTrack, String destFile) throws IOException {
        ArrayList<Track> otherTracks = new ArrayList<Track>();
        int angle = 0;
        if (sourceVideo != null) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(sourceVideo);
            angle = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            retriever.release();

            Movie m = new MovieCreator().build(sourceVideo);
            for (Track track : m.getTracks()) {
                if (!track.getHandler().equals("vide")) {
                    otherTracks.add(track);
                }
            }
        }
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(videoTrack));
        Movie m = new Movie();
        Matrix matrix;
        switch (angle) {
            default:
            case 0:
                matrix = Matrix.ROTATE_0;
                break;
            case 90:
                matrix = Matrix.ROTATE_90;
                break;
            case 180:
                matrix = Matrix.ROTATE_180;
                break;
            case 270:
                matrix = Matrix.ROTATE_270;
                break;
        }
        h264Track.getTrackMetaData().setMatrix(matrix);

        m.addTrack(h264Track);
        for (Track other : otherTracks) {
            m.addTrack(other);
        }
        Container out = new DefaultMp4Builder().build(m);
        FileOutputStream fos = new FileOutputStream(new File(destFile));
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);
        fos.close();
    }

    @Override
    public synchronized void writeRawVideo(String videoTrack, String destFile) throws IOException {
        muxVideo(null, videoTrack, destFile);
    }
}