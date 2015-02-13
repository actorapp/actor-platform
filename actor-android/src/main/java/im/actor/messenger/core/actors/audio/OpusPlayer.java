package im.actor.messenger.core.actors.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.droidkit.actors.Actor;
import com.droidkit.actors.ActorSelection;
import com.droidkit.actors.Props;
import com.droidkit.opus.OpusLib;

import im.actor.messenger.model.AudioState;
import im.actor.messenger.model.MessageModel;
import im.actor.messenger.model.DialogUids;
import im.actor.messenger.storage.ListEngines;
import im.actor.messenger.storage.scheme.media.Downloaded;
import im.actor.messenger.storage.scheme.messages.types.AbsFileMessage;

import java.nio.ByteBuffer;

import static im.actor.messenger.storage.KeyValueEngines.downloaded;

/**
 * Created by ex3ndr on 02.10.14.
 */
public class OpusPlayer extends Actor {

    public static ActorSelection player() {
        return new ActorSelection(Props.create(OpusPlayer.class), "opus_player");
    }

    private OpusLib opusLib;

    private AudioTrack audioTrack;
    private int bufferSize;
    private long duration;
    private long offset;

    private String currentFileName;

    private int type;
    private int id;
    private MessageModel messageModel;
    private boolean isStarted = false;

    @Override
    public void preStart() {
        super.preStart();
        opusLib = new OpusLib();
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof StartPlay) {
            StartPlay start = (StartPlay) message;
            stopPlay();
            messageModel = ListEngines.getMessagesListEngine(DialogUids.getDialogUid(start.type, start.id)).getValue(start.rid);
            if (messageModel == null || messageModel.getAudioState() == null) {
                return;
            }

            Downloaded downloaded = downloaded().get(((AbsFileMessage) messageModel.getContent()).getLocation().getFileId());
            if (downloaded == null) {
                return;
            }
            currentFileName = downloaded.getDownloadedPath();

            try {
                bufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
                audioTrack.play();
            } catch (Exception e) {
                e.printStackTrace();
                audioTrack = null;
                return;
            }

            int res = opusLib.openOpusFile(currentFileName);
            if (res == 0) {
                audioTrack.release();
                audioTrack = null;
                return;
            }

            duration = opusLib.getTotalPcmDuration();
            offset = 0;

            isStarted = true;
            type = start.type;
            id = start.id;
            messageModel.getAudioState().change(new AudioState(AudioState.State.PLAYING));
            self().sendOnce(new Iterate());
        } else if (message instanceof Iterate) {
            if (!isStarted) {
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

            messageModel.getAudioState().change(new AudioState(AudioState.State.PLAYING, (int) (scale * 100)));

            if (!isFinished) {
                self().sendOnce(new Iterate());
            } else {
                self().send(new StopPlay(type, id, messageModel.getRid()));
            }
        } else if (message instanceof StopPlay) {
            stopPlay();
        }
    }

    private void stopPlay() {
        if (isStarted) {
            messageModel.getAudioState().change(new AudioState(AudioState.State.STOPPED));
            opusLib.closeOpusFile();
            audioTrack.release();
            audioTrack = null;
            isStarted = false;
        }
    }

    private static class Iterate {

    }

    public static class StartPlay {
        private int type;
        private int id;
        private long rid;

        public StartPlay(int type, int id, long rid) {
            this.type = type;
            this.id = id;
            this.rid = rid;
        }
    }

    public static class StopPlay {
        private int type;
        private int id;
        private long rid;

        public StopPlay(int type, int id, long rid) {
            this.type = type;
            this.id = id;
            this.rid = rid;
        }
    }
}
