package im.actor.sdk.core.audio;


import com.droidkit.opus.OpusLib;

import java.nio.ByteBuffer;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.messages.PoisonPill;

/**
 * Created by ex3ndr on 18.03.14.
 */
public class OpusEncoderActor extends Actor {
    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_COMPLETED = 2;

    private int state = STATE_NONE;

    private OpusLib opusLib = new OpusLib();

    private ByteBuffer fileBuffer = ByteBuffer.allocateDirect(1920);


    protected void onStartMessage(String fileName) {
        if (state != STATE_NONE) {
            return;
        }
        int result = opusLib.startRecord(fileName);
        state = STATE_STARTED;
    }

    protected void onWriteMessage(byte[] buffer, int size) {
        if (state != STATE_STARTED) {
            return;
        }
        ByteBuffer finalBuffer = ByteBuffer.allocateDirect(size);
        finalBuffer.put(buffer, 0, size);
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

    protected void onStopMessage() {
        if (state != STATE_STARTED) {
            return;
        }

        opusLib.stopRecord();

        state = STATE_COMPLETED;
        self().send(PoisonPill.INSTANCE);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Start) {
            onStartMessage(((Start) message).getFileName());
        } else if (message instanceof Write) {
            onWriteMessage(((Write) message).getBuffer(), ((Write) message).getSize());
        } else if (message instanceof Stop) {
            onStopMessage();
        }
    }

    public static class Start {
        String fileName;

        public Start(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    public static class Write {
        byte[] buffer;
        int size;

        public Write(byte[] buffer, int size) {
            this.buffer = buffer;
            this.size = size;
        }

        public byte[] getBuffer() {
            return buffer;
        }

        public int getSize() {
            return size;
        }
    }

    public static class Stop {

    }


}
