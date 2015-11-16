package com.droidkit.opus;

import java.nio.ByteBuffer;

/**
 * OpusLib native binding
 */
public class OpusLib {

    static {
        System.loadLibrary("droidkitopus");
    }

    /**
     * Starting opus recording
     *
     * @param path path to file
     * @return non zero if started player
     */
    public native int startRecord(String path);

    /**
     * Writing audio frame to encoder
     *
     * @param frame buffer with sound in 16 bit mono PCM 16000 format
     * @param len   len of data
     * @return not null if successful
     */
    public native int writeFrame(ByteBuffer frame, int len);

    /**
     * Stopping record
     */
    public native void stopRecord();

    /**
     * Checking Opus File format
     *
     * @param path path to file
     * @return non zero if opus file
     */
    public native int isOpusFile(String path);

    /**
     * Opening file
     *
     * @param path path to file
     * @return non zero if successful
     */
    public native int openOpusFile(String path);

    /**
     * Seeking in opus file
     *
     * @param position position in file
     * @return non zero if successful
     */
    public native int seekOpusFile(float position);

    /**
     * Closing opus file
     */
    public native void closeOpusFile();

    /**
     * Reading from opus file
     *
     * @param buffer
     * @param capacity
     */
    public native void readOpusFile(ByteBuffer buffer, int capacity);

    /**
     * Is playback finished
     *
     * @return non zero if playback is finished
     */
    public native int getFinished();

    /**
     * Read block size in readOpusFile
     *
     * @return block size in bytes
     */
    public native int getSize();

    /**
     * Offset of actual sound for playback
     *
     * @return offset
     */
    public native long getPcmOffset();

    /**
     * Total opus pcm duration
     *
     * @return pcm duration
     */
    public native long getTotalPcmDuration();
}