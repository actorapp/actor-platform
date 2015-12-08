package im.actor.sdk.core.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.droidkit.opus.OpusLib;

import java.nio.ByteBuffer;

import im.actor.runtime.actors.Actor;

/**
 * Created by ex3ndr on 18.03.14.
 */
public class OpusPlayerActor extends Actor {

    private OpusLib opusLib;

    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_PAUSED = 2;

    private int state = STATE_NONE;
    private AudioTrack audioTrack;
    private int bufferSize;
    private long duration;
    private long offset;
    private AudioPlayerActor.AudioPlayerCallback callback;
    private String currentFileName;

    public OpusPlayerActor(AudioPlayerActor.AudioPlayerCallback callback) {
        this.callback = callback;
        this.opusLib = new OpusLib();
    }

    protected void onPlayMessage(String fileName) {
        onPlayMessage(fileName, 0);
    }

    protected void onPlayMessage(String fileName, float seek) {
        if (state != STATE_NONE) {
            destroyPlayer();
        }
        state = STATE_NONE;
        currentFileName = fileName;

        int res = opusLib.openOpusFile(currentFileName);
        if (res == 0) {
            callback.onError(currentFileName);
            return;
        }

        duration = opusLib.getTotalPcmDuration();
        offset = 0;

        try {
            bufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
            audioTrack.play();
        } catch (Exception e) {
            e.printStackTrace();
            destroyPlayer();
            callback.onError(currentFileName);
            return;
        }

        state = STATE_STARTED;
        if (seek != 0) {
            opusLib.seekOpusFile(seek);
        }
        callback.onStart(fileName);
        self().send(new Iterate());
    }

    protected void onIterateMessage() {
        if (state != STATE_STARTED) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        opusLib.readOpusFile(buffer, bufferSize);
        int size = opusLib.getSize();
        long pmcOffset = opusLib.getPcmOffset();
        boolean isFinished = opusLib.getFinished() == 1;
        if (size != 0) {
            buffer.rewind();
            byte[] data = new byte[size];
            buffer.get(data);
            audioTrack.write(data, 0, size);
        }
        offset = pmcOffset;
        float scale = 0;
        if (duration != 0) {
            scale = offset / (float) duration;
        }

        callback.onProgress(currentFileName, scale);

        if (!isFinished) {
            self().send(new Iterate());
        } else {
            self().send(new Stop());

        }
    }

    protected void onPauseMessage() {
        if (state == STATE_STARTED) {
            audioTrack.pause();
            state = STATE_PAUSED;
        }
        float scale = 0;
        if (duration != 0) {
            scale = offset / (float) duration;
        }
        callback.onPause(currentFileName, scale);
    }

    protected void onResumeMessage() {
        if (state == STATE_PAUSED) {
            audioTrack.play();
            state = STATE_STARTED;
            onIterateMessage();
        }
    }

    protected void onStopMessage() {
        destroyPlayer();
        state = STATE_NONE;
        callback.onStop(currentFileName);
    }

    protected void onToggleMessage(String fileName) {
        if (state == STATE_PAUSED) {
            onResumeMessage();
        } else if (state == STATE_STARTED) {
            onPauseMessage();
        } else {
            onPlayMessage(fileName);
        }
    }

    private void destroyPlayer() {
        opusLib.closeOpusFile();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Play) {
            onPlayMessage(((Play) message).getFilename());
        } else if (message instanceof Stop) {
            onStopMessage();
        } else if (message instanceof Iterate) {
            onIterateMessage();
        } else if (message instanceof Pause) {
            onPauseMessage();
        } else if (message instanceof Resume) {
            onResumeMessage();
        } else if (message instanceof Toggle) {
            onToggleMessage(((Toggle) message).getFilename());
        } else if (message instanceof Seek) {
            onPlayMessage(((Seek) message).getFilename(), ((Seek) message).getPosition());
        }
    }


    public static class Play {
        String filename;

        public Play(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }
    }

    public static class Iterate {

    }

    public static class Pause {

    }

    public static class Stop {

    }

    public static class Resume {

    }

    public static class Toggle {
        String filename;

        public Toggle(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }
    }

    public static class Seek {
        float position;
        String filename;

        public Seek(float position, String filename) {
            this.position = position;
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public float getPosition() {
            return position;
        }
    }
}
