package im.actor.messenger.model;

import com.droidkit.mvvm.ValueModel;

import java.util.HashMap;

/**
 * Created by ex3ndr on 26.10.14.
 */
public class UploadModel {
    private static final HashMap<Long, ValueModel<UploadState>> uploadModels = new HashMap<Long, ValueModel<UploadState>>();

    public static ValueModel<UploadState> uploadState(long rid) {
        synchronized (uploadModels) {
            if (!uploadModels.containsKey(rid)) {
                uploadModels.put(rid, new ValueModel<UploadState>("upload." + rid,
                        new UploadState(UploadState.State.NONE, 0)));
            }
            return uploadModels.get(rid);
        }
    }
}
