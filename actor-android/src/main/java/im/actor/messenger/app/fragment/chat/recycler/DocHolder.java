package im.actor.messenger.app.fragment.chat.recycler;

import android.graphics.Bitmap;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.progress.CircularView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.util.FileTypes;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.files.FileReference;
import im.actor.model.viewmodel.FileVM;
import im.actor.model.viewmodel.FileVMCallback;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class DocHolder extends MessageHolder {

    // Basic bubble
    private View bubbleView;
    private View menu;

    // Content views
    private TextView fileName;
    private TextView fileSize;
    private TextView status;
    private ImageView fileIcon;

    // Progress views
    private TintImageView downloadIcon;
    private CircularView progressView;
    private TextView progressValue;

    // Binded model
    private FileVM downloadFileVM;

    public DocHolder(final MessagesFragment fragment, View itemView) {
        super(fragment, itemView, false);

        // Basic bubble
        bubbleView = itemView.findViewById(R.id.bubbleContainer);
        bubbleView.setBackgroundResource(R.drawable.conv_bubble_media_in);
        menu = itemView.findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(fragment.getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.doc_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
//                        if (message != null && message.getContent() instanceof DocumentMessage) {
//                            DocumentMessage documentMessage = (DocumentMessage) message.getContent();
//                            Downloaded downloaded = downloaded().get(documentMessage.getLocation().getFileId());
//                            if (downloaded != null) {
//                                context.startActivity(Intents.shareDoc(downloaded));
//                            }
//                        }

                        return true;
                    }
                });
                popup.show();
            }
        });

        // Content views
        fileName = (TextView) itemView.findViewById(R.id.fileName);
        fileSize = (TextView) itemView.findViewById(R.id.fileSize);
        status = (TextView) itemView.findViewById(R.id.status);
        fileIcon = (ImageView) itemView.findViewById(R.id.icon);

        // Progress views
        downloadIcon = (TintImageView) itemView.findViewById(R.id.downloading);
        progressView = (CircularView) itemView.findViewById(R.id.progressView);
        progressView.setColor(fragment.getActivity().getResources().getColor(R.color.primary));
        progressValue = (TextView) itemView.findViewById(R.id.progressValue);
    }

    @Override
    protected void bindData(Message message, boolean isUpdated) {
        DocumentContent document = (DocumentContent) message.getContent();

        // Content data
        fileName.setText(document.getName());
        // TODO Update file sizes
        // fileSize.setText(Formatter.formatFileSize(document.getSource().parse().getSize()) + " " + ext.toUpperCase());

        //region File icon

        if (isUpdated) {
            boolean isAppliedThumb = false;
            if (document.getFastThumb() != null) {
                try {
                    Bitmap img = ImageLoading.loadBitmap(document.getFastThumb().getImage());
                    fileIcon.setImageBitmap(img);
                    fileIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    isAppliedThumb = true;
                } catch (ImageLoadException e) {
                    e.printStackTrace();
                }
            }
            if (!isAppliedThumb) {
                int type = FileTypes.getType(document.getExt());
                switch (type) {
                    default:
                    case FileTypes.TYPE_UNKNOWN:
                        fileIcon.setImageResource(R.drawable.picker_unknown);
                        break;
                    case FileTypes.TYPE_APK:
                        fileIcon.setImageResource(R.drawable.picker_apk);
                        break;
                    case FileTypes.TYPE_MUSIC:
                        fileIcon.setImageResource(R.drawable.picker_music);
                        break;
                    case FileTypes.TYPE_PICTURE:
                        fileIcon.setImageResource(R.drawable.picker_unknown);
                        break;
                    case FileTypes.TYPE_DOC:
                        fileIcon.setImageResource(R.drawable.picker_doc);
                        break;
                    case FileTypes.TYPE_RAR:
                        fileIcon.setImageResource(R.drawable.picker_rar);
                        break;
                    case FileTypes.TYPE_VIDEO:
                        fileIcon.setImageResource(R.drawable.picker_video);
                        break;
                    case FileTypes.TYPE_ZIP:
                        fileIcon.setImageResource(R.drawable.picker_zip);
                        break;
                    case FileTypes.TYPE_XLS:
                        fileIcon.setImageResource(R.drawable.picker_xls);
                        break;
                    case FileTypes.TYPE_PPT:
                        fileIcon.setImageResource(R.drawable.picker_ppt);
                        break;
                    case FileTypes.TYPE_CSV:
                        fileIcon.setImageResource(R.drawable.picker_csv);
                        break;
                    case FileTypes.TYPE_HTM:
                        fileIcon.setImageResource(R.drawable.picker_htm);
                        break;
                    case FileTypes.TYPE_HTML:
                        fileIcon.setImageResource(R.drawable.picker_html);
                        break;
                    case FileTypes.TYPE_PDF:
                        fileIcon.setImageResource(R.drawable.picker_pdf);
                        break;
                }
                fileIcon.setScaleType(ImageView.ScaleType.CENTER);
            }
        }
        //endregion

        // Update view
        boolean needRebind = false;
        if (isUpdated) {
            // Resetting binding
            if (downloadFileVM != null) {
                downloadFileVM.detach();
                downloadFileVM = null;
            }
            needRebind = true;
        } else {

        }

        if (needRebind) {
            downloadIcon.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            progressValue.setVisibility(View.GONE);

//            if (document.getSource() instanceof FileRemoteSource) {
//
//            }
//            downloadFileVM = messenger().bindFile()
        }
    }

    private class DownloadVMCallback implements FileVMCallback {

        @Override
        public void onNotDownloaded() {

        }

        @Override
        public void onDownloading(float progress) {

        }

        @Override
        public void onDownloaded(FileReference reference) {

        }
    }
}
