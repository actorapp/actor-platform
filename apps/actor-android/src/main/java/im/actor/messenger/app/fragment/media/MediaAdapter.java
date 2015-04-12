package im.actor.messenger.app.fragment.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;


import java.io.File;

import im.actor.android.view.BindedListAdapter;
import im.actor.android.view.BindedViewHolder;
import im.actor.images.common.ImageLoadException;
import im.actor.images.ops.ImageLoading;
import im.actor.messenger.R;
import im.actor.messenger.app.AppContext;
import im.actor.messenger.app.fragment.chat.view.FastThumbLoader;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.OnItemClickedListener;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileLocalSource;
import im.actor.model.entity.content.VideoContent;
import im.actor.model.mvvm.BindedDisplayList;

import static im.actor.messenger.app.Core.messenger;


/**
 * Created by Jesus Christ. Amen.
 */
public class MediaAdapter extends BindedListAdapter<Message, MediaAdapter.MediaHolder> {
    private static final int tileSize = Screen.getWidth() / AppContext.getContext().getResources().getInteger(R.integer.gallery_items_count);
    private final Context context;
    private final OnMediaClickListener onItemClickListener;

    public MediaAdapter(BindedDisplayList<Message> mediaDisplayList,
                        OnMediaClickListener onItemClickedListener,
                        Context context) {
        super(mediaDisplayList);
        this.context = context;
        this.onItemClickListener = onItemClickedListener;


    }

    @Override
    public MediaHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new MediaHolder(LayoutInflater.from(context).inflate(R.layout.adapter_media, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final MediaHolder mediaHolder, int index, final Message item) {
        mediaHolder.bind(item, index, context);
        mediaHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onClick(mediaHolder, item);
            }
        });
    }


    public static class MediaHolder extends BindedViewHolder {
        private final FastThumbLoader fastThumbLoader;
        private MediaPreview previewView;
        private TextView descView;
        private TextView durationView;
        private View videoIndicator;

        public MediaHolder(View itemView) {
            super(itemView);
            previewView = (MediaPreview) itemView.findViewById(R.id.image);
            durationView = (TextView) itemView.findViewById(R.id.duration);
            videoIndicator = itemView.findViewById(R.id.video);
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            params.height = tileSize;
            params.width = tileSize;
            itemView.setLayoutParams(params);
            fastThumbLoader = new FastThumbLoader(previewView);
            fastThumbLoader.setBlur(5);

        }

        public void bind(Message message, int position, Context context) {
            Bitmap img = null;
            if(!(message.getContent() instanceof DocumentContent)){
                previewView.setImageBitmap(null);
                return;
            }
            DocumentContent media = ((DocumentContent) message.getContent());
            if(media.getFastThumb()!=null)
                fastThumbLoader.request(media.getFastThumb().getImage());
            if(media instanceof VideoContent){
                durationView.setText(messenger().getFormatter().formatDuration(((VideoContent)media).getDuration()));
                videoIndicator.setVisibility(View.VISIBLE);
                durationView.setVisibility(View.VISIBLE);
            } else {
                videoIndicator.setVisibility(View.GONE);
                durationView.setVisibility(View.GONE);
            }
            if(media.getSource() instanceof FileLocalSource) {
                // todo дичайший outOfMemory неясно где остаётся битмап
                // previewView.requestPhoto(downloaded.getDownloadedPath(), tileSize);

                /*previewView.setImageURI(Uri.fromFile(
                        new File(((FileLocalSource) media.getSource()).getFileDescriptor())));*/
            }
        }




    }
}
