package im.actor.messenger.app.fragment.media;

import android.view.View;

import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.model.entity.Message;

/**
 * Created by Jesus Christ. Amen.
 */
public interface OnMediaClickListener {
    void onClick(MediaAdapter.MediaHolder holder, Message item);
}
