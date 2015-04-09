package im.actor.messenger.app.fragment.media;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import im.actor.images.loading.tasks.RawFileTask;
import im.actor.messenger.app.util.Logger;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileLocalSource;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.files.FileSystemReference;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.DisplayList;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.viewmodel.DownloadCallback;

import static im.actor.messenger.app.Core.messenger;

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
            fragment = PictureActivity.PictureFragment.getInstance(location.getFileId(), senderId);

            // todo not loaded?
        } else if (document.getSource() instanceof FileLocalSource) {
            final String path = ((FileLocalSource) document.getSource()).getFileDescriptor();
            fragment = PictureActivity.PictureFragment.getInstance(path, senderId);

        }

        return fragment;
    }
}
