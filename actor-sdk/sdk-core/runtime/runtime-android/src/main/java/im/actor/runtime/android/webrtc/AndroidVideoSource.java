package im.actor.runtime.android.webrtc;

import org.webrtc.MediaConstraints;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

import im.actor.runtime.android.AndroidWebRTCRuntimeProvider;

public class AndroidVideoSource {

    private static final Object LOCK = new Object();
    private static AndroidVideoSource currentSource;

    public static AndroidVideoSource pickVideoSource() {
        synchronized (LOCK) {
            if (currentSource != null) {
                currentSource.count++;
                return currentSource;
            }
            currentSource = new AndroidVideoSource();
            return currentSource;
        }
    }

    private int count;
    private boolean isReleased;

    private VideoCapturer videoCapturer;
    private VideoSource videoSource;

    public AndroidVideoSource() {
        this.count = 1;
        this.isReleased = false;
        this.videoCapturer = getVideoCapturer();
        this.videoSource = AndroidWebRTCRuntimeProvider.FACTORY.createVideoSource(this.videoCapturer, new MediaConstraints());
    }

    public int getCount() {
        return count;
    }

    public boolean isReleased() {
        return isReleased;
    }

    public VideoSource getVideoSource() {
        return videoSource;
    }

    public void unlink() {
        synchronized (LOCK) {
            count--;
            if (count == 0) {
                if (AndroidVideoSource.currentSource == this) {
                    AndroidVideoSource.currentSource = null;
                    videoSource.dispose();
                    // videoSource.dispose();
                    // videoCapturer.dispose();
                }
            }
        }
    }

    // Cycle through likely device names for the camera and return the first
    // capturer that works, or crash if none do.
    private VideoCapturer getVideoCapturer() {
        String[] cameraFacing = {"front", "back"};
        int[] cameraIndex = {0, 1};
        int[] cameraOrientation = {0, 90, 180, 270};
        for (String facing : cameraFacing) {
            for (int index : cameraIndex) {
                for (int orientation : cameraOrientation) {
                    String name = "Camera " + index + ", Facing " + facing +
                            ", Orientation " + orientation;
                    VideoCapturer capturer = VideoCapturer.create(name);
                    if (capturer != null) {
                        return capturer;
                    }
                }
            }
        }
        throw new RuntimeException("Failed to open capturer");
    }
}
