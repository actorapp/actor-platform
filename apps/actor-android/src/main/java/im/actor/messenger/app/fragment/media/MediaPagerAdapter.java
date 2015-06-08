package im.actor.messenger.app.fragment.media;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import im.actor.model.entity.FileReference;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileLocalSource;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.DisplayList;

/**
 * Created by Jesus Christ. Amen.
 */
public class MediaPagerAdapter extends FragmentStatePagerAdapter {
    private final BindedDisplayList<Message> displayList;

    public MediaPagerAdapter(BindedDisplayList<Message> displayList, MediaActivity context) {
        super(context.getSupportFragmentManager());
        this.displayList = displayList;
        displayList.addListener(new DisplayList.Listener() {
            @Override
            public void onCollectionChanged() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return displayList.getSize();
    }


    @Override
    public Fragment getItem(int position) {
        Message item = displayList.getItem(position);
        int senderId = item.getSenderId();

        Fragment fragment = null;
        final DocumentContent document = (DocumentContent) item.getContent();
        if (document.getSource() instanceof FileRemoteSource) {
            FileRemoteSource remoteSource = (FileRemoteSource) document.getSource();
            final FileReference location = remoteSource.getFileReference();
            fragment = PictureActivity.PictureFragment.getInstance(location, senderId);

            // todo not loaded?
        } else if (document.getSource() instanceof FileLocalSource) {
            final String path = ((FileLocalSource) document.getSource()).getFileDescriptor();
            fragment = PictureActivity.PictureFragment.getInstance(path, senderId);

        }

        return fragment;
    }
}
