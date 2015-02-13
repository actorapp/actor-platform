package im.actor.messenger.core.actors.profile;

import com.droidkit.mvvm.ValueModel;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class AvatarChangeState {

    private static ValueModel<State> uploadingState = new ValueModel<State>("avatars.my", State.NONE);

    private static String fileName;

    public static ValueModel<State> uploadingState() {
        return uploadingState;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        AvatarChangeState.fileName = fileName;
    }

    public enum State {
        NONE,
        UPLOADING,
        ERROR
    }
}