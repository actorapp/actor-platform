package im.actor.sdk.controllers.calls.view;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import im.actor.sdk.R;
import im.actor.sdk.core.audio.AndroidPlayerActor;
import im.actor.sdk.core.audio.AudioPlayerActor;

public class AudioActorEx extends AndroidPlayerActor {

    Context context;

    public AudioActorEx(Context context, AudioPlayerActor.AudioPlayerCallback callback) {
        super(context, callback);
        this.context = context;
    }

    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_PAUSED = 2;

    @Override
    protected void onPlayMessage(String fileName) {
        currentFileName = fileName;

        destroyPlayer();
        state = STATE_NONE;

        try {
            if (mplayer == null) {
                mplayer = new MediaPlayer();
            }
            mplayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mplayer.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.tone));
            mplayer.prepare();
            mplayer.start();
            mplayer.setOnCompletionListener(mp -> self().send(new Play("")));
            mplayer.setOnErrorListener((mp, what, extra) -> {
                self().send(new Error());
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
            destroyPlayer();
            callback.onError(currentFileName);
            return;
        }

        callback.onStart(currentFileName);
        schedule(new Notify(), 500);
        state = STATE_STARTED;
    }

    private void destroyPlayer() {
        if (mplayer != null) {
            mplayer.stop();
            mplayer.reset();
            mplayer.release();
            mplayer = null;
        }
    }
}
