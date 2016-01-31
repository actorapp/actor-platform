package im.actor.sdk.core.audio;

import android.content.Context;

import com.droidkit.opus.OpusLib;

import java.util.ArrayList;

import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;

public class AudioPlayerActor extends Actor {

    private ActorRef androidPlayerActor;
    private ActorRef opusPlayerActor;

    private OpusLib opusLib;

    private boolean isInited;
    private AudioPlayerCallback callback;
    private boolean usedAndroid;
    private Context context;
    private String currenFile;
    private ArrayList<AudioPlayerCallback> callbacks;

    @Override
    public void preStart() {
        androidPlayerActor = ActorSystem.system().actorOf(Props.create(new ActorCreator() {
            @Override
            public AndroidPlayerActor create() {
                return new AndroidPlayerActor(context, callback);
            }
        }), "actor/android_player");
        opusPlayerActor = ActorSystem.system().actorOf(Props.create(new ActorCreator() {
            @Override
            public OpusPlayerActor create() {
                return new OpusPlayerActor(callback);
            }
        }), "actor/opus_player");
    }

    public AudioPlayerActor(Context context) {
        this.context = context;
        opusLib = new OpusLib();
        callbacks = new ArrayList<AudioPlayerCallback>();
        this.callback = new AudioPlayerCallback() {
            @Override
            public void onStart(String fileName) {
                for (AudioPlayerCallback callback : callbacks) {
                    callback.onStart(fileName);
                }
            }

            @Override
            public void onStop(String fileName) {
                for (AudioPlayerCallback callback : callbacks) {
                    callback.onStop(fileName);
                }
            }

            @Override
            public void onPause(String fileName, float progress) {
                for (AudioPlayerCallback callback : callbacks) {
                    callback.onPause(fileName, progress);
                }
            }

            @Override
            public void onProgress(String fileName, float progress) {
                for (AudioPlayerCallback callback : callbacks) {
                    callback.onProgress(fileName, progress);
                }
            }

            @Override
            public void onError(String fileName) {
                for (AudioPlayerCallback callback : callbacks) {
                    callback.onError(fileName);
                }
            }
        };
    }

    protected void onPlayMessage(String fileName) {
        ;
        currenFile = fileName;
        if (isInited) {
            onStopMessage();
        }

        this.usedAndroid = opusLib.isOpusFile(fileName) <= 0;
        this.isInited = true;

        if (usedAndroid) {
            androidPlayerActor.send(new AndroidPlayerActor.Play(fileName));
            opusPlayerActor.send(new OpusPlayerActor.Stop());
        } else {
            androidPlayerActor.send(new AndroidPlayerActor.Stop());
            opusPlayerActor.send(new OpusPlayerActor.Play(fileName));
        }
    }

    protected void onStopMessage() {
        if (isInited) {
            if (usedAndroid) {
                androidPlayerActor.send(new AndroidPlayerActor.Stop());
            } else {
                opusPlayerActor.send(new OpusPlayerActor.Stop());
            }
        } else {
            for (AudioPlayerCallback callback : callbacks) {
                callback.onStop(null);
            }
        }
        isInited = false;
    }

    protected void onToggleMessage(String fileName) {
        if (isInited) {
            if (currenFile == fileName) {
                if (usedAndroid) {
                    androidPlayerActor.send(new AndroidPlayerActor.Toggle(fileName));
                } else {
                    opusPlayerActor.send(new OpusPlayerActor.Toggle(fileName));
                }
            } else {
                onStopMessage();
            }
        }

        if (!isInited) {
            onPlayMessage(fileName);
        }
    }

    protected void addCallback(AudioPlayerActor.AudioPlayerCallback callback) {
        callbacks.add(callback);
    }

    protected void removeCallback(AudioPlayerActor.AudioPlayerCallback callback) {
        callbacks.remove(callback);
    }

    protected void onSeek(float position, String fileName) {
        currenFile = fileName;
        this.isInited = true;
        if (usedAndroid) {
            //androidPlayerActor.send(new AndroidPlayerActor.Toggle(fileName));
        } else {
            opusPlayerActor.send(new OpusPlayerActor.Seek(position, fileName));
        }

    }


    public interface AudioPlayerCallback {
        void onStart(String fileName);

        void onStop(String fileName);

        void onPause(String fileName, float progress);

        void onProgress(String fileName, float progress);

        void onError(String fileName);
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof Play) {
            onPlayMessage(((Play) message).getFilename());
        } else if (message instanceof Stop) {
            onStopMessage();
        } else if (message instanceof Toggle) {
            onToggleMessage(((Toggle) message).getFilename());
        } else if (message instanceof RegisterCallback) {
            addCallback(((RegisterCallback) message).getCallback());
        } else if (message instanceof RemoveCallback) {
            removeCallback(((RemoveCallback) message).getCallback());
        } else if (message instanceof Seek) {
            onSeek(((Seek) message).getPosition(), ((Seek) message).getFilename());
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

    public static class Stop {

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

    public static class RegisterCallback {
        AudioPlayerActor.AudioPlayerCallback callback;

        public RegisterCallback(AudioPlayerActor.AudioPlayerCallback callback) {
            this.callback = callback;
        }

        public AudioPlayerActor.AudioPlayerCallback getCallback() {
            return callback;
        }
    }

    public static class RemoveCallback {
        AudioPlayerActor.AudioPlayerCallback callback;

        public RemoveCallback(AudioPlayerActor.AudioPlayerCallback callback) {
            this.callback = callback;
        }

        public AudioPlayerActor.AudioPlayerCallback getCallback() {
            return callback;
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