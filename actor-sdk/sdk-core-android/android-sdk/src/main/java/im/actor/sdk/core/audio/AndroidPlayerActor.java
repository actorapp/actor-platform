package im.actor.sdk.core.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;

import im.actor.runtime.actors.Actor;

/**
 * Created by ex3ndr on 18.03.14.
 */
public class AndroidPlayerActor extends Actor {

    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_PAUSED = 2;
    private final Context context;

    protected int state = STATE_NONE;

    protected MediaPlayer mplayer;

    protected String currentFileName;
    protected AudioPlayerActor.AudioPlayerCallback callback;

    public AndroidPlayerActor(Context context, AudioPlayerActor.AudioPlayerCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    protected void onPlayMessage(String fileName) {
        currentFileName = fileName;

        destroyPlayer();
        state = STATE_NONE;

        try {
            mplayer = new MediaPlayer();
            mplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mplayer.setDataSource(context, Uri.fromFile(new File(currentFileName)));
            mplayer.prepare();
            mplayer.setLooping(false);
            mplayer.start();
            mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    self().send(new Stop());
                }
            });
            mplayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    self().send(new Error());
                    return false;
                }
            });
        } catch (Exception e) {
            destroyPlayer();
            callback.onError(currentFileName);
            return;
        }

        callback.onStart(currentFileName);
        self().send(new Notify(), 500);
        state = STATE_STARTED;
    }

    protected void onNotifyMessage() {
        if (mplayer != null) {
            if (state == STATE_STARTED) {
                int duration = mplayer.getDuration();
                if (duration == 0) {
                    callback.onProgress(currentFileName, 0);
                } else {
                    float progress = ((float) mplayer.getCurrentPosition()) / duration;
                    callback.onProgress(currentFileName, progress);
                }
                self().send(new Notify(), 500);
            }
        }
    }

    protected void onPauseMessage() {
        if (mplayer != null) {
            if (mplayer.isPlaying()) {
                mplayer.pause();
            }
            state = STATE_PAUSED;
        }
    }

    protected void onResumeMessage() {
        if (mplayer != null) {
            if (!mplayer.isPlaying()) {
                mplayer.start();
            }
            state = STATE_STARTED;
        }
    }

    protected void onStopMessage() {
        destroyPlayer();
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

    protected void onRestartMessage() {
        onPlayMessage(currentFileName);
    }

    protected void onErrorMessage() {
        destroyPlayer();
        callback.onError(currentFileName);
    }

    private void destroyPlayer() {
        if (mplayer != null) {
            mplayer.stop();
            mplayer.reset();
            mplayer.release();
            mplayer = null;
        }
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Play) {
            onPlayMessage(((Play) message).getFilename());
        } else if (message instanceof Stop) {
            onStopMessage();
        } else if (message instanceof Notify) {
            onNotifyMessage();
        } else if (message instanceof Pause) {
            onPauseMessage();
        } else if (message instanceof Resume) {
            onResumeMessage();
        } else if (message instanceof Toggle) {
            onToggleMessage(((Toggle) message).getFilename());
        } else if (message instanceof Restart) {
            onRestartMessage();
        } else if (message instanceof Error) {
            onErrorMessage();
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

    public static class Notify {

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

    public static class Restart {

    }

    public static class Error {

    }
}

