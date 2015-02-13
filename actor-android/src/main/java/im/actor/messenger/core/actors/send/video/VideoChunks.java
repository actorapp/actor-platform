package im.actor.messenger.core.actors.send.video;

import android.media.MediaFormat;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * The elementary stream coming out of the "video/avc" encoder needs to be fed back into
 * the decoder one chunk at a time.  If we just wrote the data to a file, we would lose
 * the information about chunk boundaries.  This class stores the encoded data in memory,
 * retaining the chunk organization.
 */
public class VideoChunks {

    private static final String TAG = "VideoChunks";

    private MediaFormat mMediaFormat;
    private ArrayList<byte[]> mChunks = new ArrayList<byte[]>();
    private ArrayList<Integer> mFlags = new ArrayList<Integer>();
    private ArrayList<Long> mTimes = new ArrayList<Long>();

    /**
     * Sets the MediaFormat, for the benefit of a future decoder.
     */
    public void setMediaFormat(MediaFormat format) {
        mMediaFormat = format;
    }

    /**
     * Gets the MediaFormat that was used by the encoder.
     */
    public MediaFormat getMediaFormat() {
        return mMediaFormat;
    }

    /**
     * Adds a new chunk.  Advances buf.position to buf.limit.
     */
    public void addChunk(ByteBuffer buf, int size, int flags, long time) {
        byte[] data = new byte[size];
        buf.get(data);
        mChunks.add(data);
        mFlags.add(flags);
        mTimes.add(time);
    }

    /**
     * Returns the number of chunks currently held.
     */
    public int getNumChunks() {
        return mChunks.size();
    }

    /**
     * Copies the data from chunk N into "dest".  Advances dest.position.
     */
    public void getChunkData(int chunk, ByteBuffer dest) {
        byte[] data = mChunks.get(chunk);
        dest.put(data);
        dest.position(0);
    }

    public int getChunkSize(int chunk) {
        return mChunks.get(chunk).length;
    }

    /**
     * Copies the data from chunk N into "dest".  Advances dest.position.
     */
    public byte[] getChunkData(int chunk) {
        return mChunks.get(chunk);
    }

    /**
     * Returns the flags associated with chunk N.
     */
    public int getChunkFlags(int chunk) {
        return mFlags.get(chunk);
    }

    /**
     * Returns the timestamp associated with chunk N.
     */
    public long getChunkTime(int chunk) {
        return mTimes.get(chunk);
    }

    /**
     * Writes the chunks to a file as a contiguous stream.  Useful for debugging.
     */
    public void saveToFile(File file) {
        Log.d(TAG, "saving chunk data to file " + file);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            fos = null;     // closing bos will also close fos

            int numChunks = getNumChunks();
            for (int i = 0; i < numChunks; i++) {
                byte[] chunk = mChunks.get(i);
                bos.write(chunk);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}