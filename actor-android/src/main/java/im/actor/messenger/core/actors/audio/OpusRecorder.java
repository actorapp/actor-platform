package im.actor.messenger.core.actors.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.os.Vibrator;
import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.opus.OpusLib;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.core.actors.send.MediaSenderActor;
import im.actor.messenger.core.actors.send.MessageDeliveryActor;
import im.actor.messenger.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by ex3ndr on 01.10.14.
 */
public class OpusRecorder extends Actor {

    public static ActorSelection recorder() {
        return new ActorSelection(Props.create(OpusRecorder.class).changeDispatcher("opus"), "opus_recorder");
    }

    private static final String TAG = "OpusRecorder";

    private static final int BUFFER_SIZE = 16 * 1024;

    private OpusLib opusLib;
    private AudioRecord audioRecord;
    private int bufferSize;
    private boolean isStarted = false;
    private String fileName;
    private long recordStartTime;
    private byte[] buffer = new byte[BUFFER_SIZE];

    private ByteBuffer fileBuffer = ByteBuffer.allocateDirect(1920);

    @Override
    public void preStart() {
        super.preStart();
        System.loadLibrary("opus");
        opusLib = new OpusLib();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof StartRecord) {
            Logger.d(TAG, "Starting record");

            stopRecord();

            try {
                fileName = File.createTempFile("opus_", ".opus", AppContext.getContext().getFilesDir()).getAbsolutePath();
            } catch (IOException e) {
                return;
            }

            if (opusLib.startRecord(fileName) == 0) {
                return;
            }

            int minBufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            bufferSize = 16 * minBufferSize;
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            audioRecord.startRecording();

            isStarted = true;
            recordStartTime = SystemClock.uptimeMillis();

            self().sendOnce(new IterateRecord());
            vibrate();
        } else if (message instanceof IterateRecord) {
            Logger.d(TAG, "Record iteration");
            if (!isStarted) {
                return;
            }
            int len = audioRecord.read(buffer, 0, buffer.length);
            if (len > 0) {
                ByteBuffer finalBuffer = ByteBuffer.allocateDirect(len);
                finalBuffer.put(buffer, 0, len);
                finalBuffer.rewind();
                boolean flush = false;

                while (finalBuffer.hasRemaining()) {
                    int oldLimit = -1;
                    if (finalBuffer.remaining() > fileBuffer.remaining()) {
                        oldLimit = finalBuffer.limit();
                        finalBuffer.limit(fileBuffer.remaining() + finalBuffer.position());
                    }
                    fileBuffer.put(finalBuffer);
                    if (fileBuffer.position() == fileBuffer.limit() || flush) {
                        int length = !flush ? fileBuffer.limit() : finalBuffer.position();
                        if (opusLib.writeFrame(fileBuffer, length) != 0) {
                            fileBuffer.rewind();
                        }
                    }
                    if (oldLimit != -1) {
                        finalBuffer.limit(oldLimit);
                    }
                }
            }
            self().sendOnce(new IterateRecord());
        } else if (message instanceof AbortRecord) {
            Logger.d(TAG, "Record abort");
            stopRecord();
        } else if (message instanceof SendAudio) {
            Logger.d(TAG, "Record success");
            if (!isStarted) {
                return;
            }
            stopRecord();

            long duration = SystemClock.uptimeMillis() - recordStartTime;
            if (duration < 1000) {
                return;
            }

            SendAudio sendAudio = (SendAudio) message;
            MessageDeliveryActor.messageSender().sendOpus(sendAudio.getType(), sendAudio.getId(), fileName, (int) (duration / 1000));
        }
    }

    private void vibrate() {
        try {
            Vibrator v = (Vibrator) AppContext.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(20);
        } catch (Exception e) {
        }
    }

    @Override
    public void finallyStop() {
        stopRecord();
    }

    private void stopRecord() {
        if (isStarted) {
            opusLib.stopRecord();
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            isStarted = false;
        }
    }

    public static class IterateRecord {

    }

    public static class StartRecord {

    }

    public static class AbortRecord {

    }

    public static class SendAudio {
        private int type;
        private int id;

        public SendAudio(int type, int id) {
            this.type = type;
            this.id = id;
        }

        public int getType() {
            return type;
        }

        public int getId() {
            return id;
        }
    }
}
