package im.actor.runtime.android.webrtc;

import org.webrtc.AudioSource;
import org.webrtc.MediaConstraints;

import im.actor.runtime.android.AndroidWebRTCRuntimeProvider;

public class AndroidAudioSource {

    private static final Object LOCK = new Object();
    private static AndroidAudioSource currentSource;

    public static AndroidAudioSource pickAudioSource() {
        synchronized (LOCK) {
            if (currentSource != null) {
                currentSource.count++;
                return currentSource;
            }
            currentSource = new AndroidAudioSource();
            return currentSource;
        }
    }

    private int count;
    private boolean isReleased;
    private AudioSource audioSource;

    public AndroidAudioSource() {
        this.count = 1;
        this.isReleased = false;
        MediaConstraints audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        this.audioSource = AndroidWebRTCRuntimeProvider.FACTORY.createAudioSource(audioConstraints);
    }

    public int getCount() {
        return count;
    }

    public boolean isReleased() {
        return isReleased;
    }

    public AudioSource getAudioSource() {
        return audioSource;
    }

    public void unlink() {
        synchronized (LOCK) {
            count--;
            if (count == 0) {
                if (AndroidAudioSource.currentSource == this) {
                    AndroidAudioSource.currentSource = null;
                    audioSource.dispose();
                }
            }
        }
    }
}
