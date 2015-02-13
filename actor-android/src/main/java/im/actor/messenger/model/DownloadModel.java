package im.actor.messenger.model;

import com.droidkit.mvvm.ValueModel;

import java.util.HashMap;

/**
 * Created by ex3ndr on 26.10.14.
 */
public class DownloadModel {
    private static final HashMap<Long, ValueModel<DownloadState>> uploadModels = new HashMap<Long, ValueModel<DownloadState>>();

    public static ValueModel<DownloadState> downloadState(long rid) {
        synchronized (uploadModels) {
            if (!uploadModels.containsKey(rid)) {
                uploadModels.put(rid, new ValueModel<DownloadState>("upload." + rid,
                        new DownloadState(DownloadState.State.NONE, 0)));
            }
            return uploadModels.get(rid);
        }
    }
}
