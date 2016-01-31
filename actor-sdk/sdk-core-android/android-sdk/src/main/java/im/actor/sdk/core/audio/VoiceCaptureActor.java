package im.actor.sdk.core.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.os.Vibrator;

import java.util.concurrent.atomic.AtomicInteger;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.Props;

/**
 * Created by root on 11/5/15.
 */
public class VoiceCaptureActor extends Actor {


    public static final AtomicInteger LAST_ID = new AtomicInteger(0);

    private static final int BUFFER_SIZE = 16 * 1024;

    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTED = 1;

    private int state = STATE_STOPPED;

    private AudioRecord audioRecord;
    private ActorRef opusActor;
    private int bufferSize;
    private long playStartTime;
    Context context;
    VoiceCaptureCallback callback;

    public VoiceCaptureActor(Context context, VoiceCaptureCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    protected void onStartMessage(String fileName) {
        if (state == STATE_STARTED) {
            return;
        }

        int minBufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        bufferSize = 16 * minBufferSize;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        audioRecord.startRecording();
        opusActor = system().actorOf(Props.create(new ActorCreator() {
            @Override
            public OpusEncoderActor create() {
                return new OpusEncoderActor();
            }
        }), "actor/opus_encoder");
        opusActor.send(new OpusEncoderActor.Start(fileName));
        state = STATE_STARTED;
        playStartTime = SystemClock.uptimeMillis();
        vibrate(context);
        self().send(new Iterate());
    }

    protected void onIterateMessage() {
        if (state != STATE_STARTED) {
            return;
        }

        byte[] buffer = VoiceBuffers.getInstance().obtainBuffer(BUFFER_SIZE);
        int len = audioRecord.read(buffer, 0, buffer.length);
        if (len > 0) {
            opusActor.send(new OpusEncoderActor.Write(buffer, len));
        } else {
            VoiceBuffers.getInstance().releaseBuffer(buffer);
        }

        callback.onRecordProgress(SystemClock.uptimeMillis() - playStartTime);

        self().send(new Iterate());
    }

    protected void onStopMessage(boolean cancel) {
        if (state != STATE_STARTED) {
            return;
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        opusActor.send(new OpusEncoderActor.Stop());
        if (!cancel) {
            callback.onRecordStop(SystemClock.uptimeMillis() - playStartTime);
        }
        state = STATE_STOPPED;
    }

    protected void onCrashMessage() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (opusActor != null) {
            opusActor.send(new OpusEncoderActor.Stop());
        }

        callback.onRecordCrash();

        state = STATE_STOPPED;
    }

    private void vibrate(Context context) {
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(20);
        } catch (Exception e) {
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Start) {
            onStartMessage(((Start) message).getFilename());
        } else if (message instanceof Iterate) {
            onIterateMessage();
        } else if (message instanceof Stop) {
            onStopMessage(((Stop) message).isCancel());
        } else if (message instanceof Crash) {
            onCrashMessage();
        }
    }

    public static class Start {
        String filename;

        public Start(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }
    }

    public static class Iterate {

    }

    public static class Crash {

    }

    public static class Stop {
        private boolean cancel;

        public Stop(boolean cancel) {
            this.cancel = cancel;
        }

        public boolean isCancel() {
            return cancel;
        }
    }

    public interface VoiceCaptureCallback {
        void onRecordProgress(long time);

        void onRecordCrash();

        void onRecordStop(long time);
    }
}
