package im.actor.sdk.controllers.conversation.messages;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import android.widget.Toast;
import com.droidkit.progress.CircularView;

import im.actor.core.entity.FileReference;
import im.actor.core.entity.Message;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileLocalSource;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.viewmodel.FileCallback;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.core.viewmodel.UploadFileCallback;
import im.actor.core.viewmodel.UploadFileVM;
import im.actor.core.viewmodel.UploadFileVMCallback;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.conversation.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.preprocessor.PreprocessedData;
import im.actor.sdk.util.FileTypes;
import im.actor.sdk.util.images.common.ImageLoadException;
import im.actor.sdk.util.images.ops.ImageLoading;
import im.actor.sdk.view.TintImageView;
import im.actor.runtime.files.FileSystemReference;

import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ViewUtils.goneView;
import static im.actor.sdk.util.ViewUtils.showView;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class DocHolder extends MessageHolder {

    private int waitColor;
    private int sentColor;
    private int deliveredColor;
    private int readColor;
    private int errorColor;
    private final TintImageView stateIcon;
    protected final TextView time;

    // Basic bubble
    protected View menu;

    // Content views
    protected TextView fileName;
    protected TextView fileSize;
    protected TextView status;
    protected ImageView fileIcon;

    // Progress views
    protected TintImageView downloadIcon;
    protected CircularView progressView;
    protected TextView progressValue;

    // Binded model
    protected FileVM downloadFileVM;
    protected UploadFileVM uploadFileVM;
    protected DocumentContent document;

    public DocHolder(final MessagesAdapter fragment, View itemView) {
        this(fragment, itemView, false);
    }

    public DocHolder(final MessagesAdapter fragment, View itemView, boolean isFullSize) {
        super(fragment, itemView, isFullSize);
        waitColor = ActorSDK.sharedActor().style.getConvStatePendingColor();
        sentColor = ActorSDK.sharedActor().style.getConvStateSentColor();
        deliveredColor = ActorSDK.sharedActor().style.getConvStateDeliveredColor();
        readColor = ActorSDK.sharedActor().style.getConvStateReadColor();
        errorColor = ActorSDK.sharedActor().style.getConvStateErrorColor();

        stateIcon = (TintImageView) itemView.findViewById(R.id.stateIcon);
        time = (TextView) itemView.findViewById(R.id.time);
        time.setTextColor(ActorSDK.sharedActor().style.getConvTimeColor());

        // Basic bubble
        View bubbleView = itemView.findViewById(R.id.bubbleContainer);
        bubbleView.setBackgroundResource(R.drawable.conv_bubble_media_in);
        menu = itemView.findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(fragment.getMessagesFragment().getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.doc_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (currentMessage != null && currentMessage.getContent() instanceof DocumentContent) {
                            final DocumentContent documentContent = (DocumentContent) currentMessage.getContent();
                            if (documentContent.getSource() instanceof FileRemoteSource) {
                                FileRemoteSource remoteSource = (FileRemoteSource) documentContent.getSource();
                                messenger().requestState(remoteSource.getFileReference().getFileId(), new FileCallback() {
                                    @Override
                                    public void onNotDownloaded() {

                                    }

                                    @Override
                                    public void onDownloading(float progress) {

                                    }

                                    @Override
                                    public void onDownloaded(FileSystemReference reference) {
                                        Activity activity = getAdapter().getMessagesFragment().getActivity();
                                        activity.startActivity(Intents.shareDoc(documentContent.getName(),
                                                reference.getDescriptor()));
                                    }
                                });
                            }

                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        // Content views
        fileName = (TextView) itemView.findViewById(R.id.fileName);
        fileName.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        fileSize = (TextView) itemView.findViewById(R.id.fileSize);
        fileSize.setTextColor(ActorSDK.sharedActor().style.getTextHintColor());
        status = (TextView) itemView.findViewById(R.id.status);
        status.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        fileIcon = (ImageView) itemView.findViewById(R.id.icon);

        // Progress views
        downloadIcon = (TintImageView) itemView.findViewById(R.id.downloading);
        downloadIcon.setTint(ActorSDK.sharedActor().style.getMainColor());
        progressView = (CircularView) itemView.findViewById(R.id.progressView);
        progressView.setColor(ActorSDK.sharedActor().style.getMainColor());
        progressValue = (TextView) itemView.findViewById(R.id.progressValue);
        progressValue.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
        onConfigureViewHolder();
    }

    @Override
    protected void bindData(Message message, long readDate, long receiveDate, boolean isUpdated, PreprocessedData preprocessedData) {
        document = (DocumentContent) message.getContent();

        // Update state
        if (message.getSenderId() == myUid()) {
            stateIcon.setVisibility(View.VISIBLE);
            switch (message.getMessageState()) {
                case ERROR:
                    stateIcon.setResource(R.drawable.msg_error);
                    stateIcon.setTint(errorColor);
                    break;
                default:
                case PENDING:
                    stateIcon.setResource(R.drawable.msg_clock);
                    stateIcon.setTint(waitColor);
                    break;
                case SENT:
                    if (message.getSortDate() <= readDate) {
                        stateIcon.setResource(R.drawable.msg_check_2);
                        stateIcon.setTint(readColor);
                    } else if (message.getSortDate() <= receiveDate) {
                        stateIcon.setResource(R.drawable.msg_check_2);
                        stateIcon.setTint(deliveredColor);
                    } else {
                        stateIcon.setResource(R.drawable.msg_check_1);
                        stateIcon.setTint(sentColor);
                    }
                    break;
            }
        } else {
            stateIcon.setVisibility(View.GONE);
        }

        // Update time
        setTimeAndReactions(time);

        // Content data
        fileName.setText(document.getName());
        fileSize.setText(messenger().getFormatter().formatFileSize(document.getSource().getSize())
                + " " + document.getExt().toUpperCase());

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
            if (uploadFileVM != null) {
                uploadFileVM.detach();
                uploadFileVM = null;
            }
            needRebind = true;
        } else {
            if (document.getSource() instanceof FileLocalSource) {
                if (uploadFileVM == null && downloadFileVM != null) {
                    downloadFileVM.detach();
                    downloadFileVM = null;
                    needRebind = true;
                }
            } else if (document.getSource() instanceof FileRemoteSource) {
                if (uploadFileVM != null && downloadFileVM == null) {
                    uploadFileVM.detach();
                    uploadFileVM = null;
                    needRebind = true;
                }
            }
        }

        if (downloadFileVM == null && uploadFileVM == null) {
            needRebind = true;
        }

        if (needRebind) {
            downloadIcon.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            progressValue.setVisibility(View.GONE);
            fileIcon.setVisibility(View.GONE);
            menu.setVisibility(View.GONE);
            status.setVisibility(View.GONE);

            if (document.getSource() instanceof FileRemoteSource) {
                FileRemoteSource remoteSource = (FileRemoteSource) document.getSource();
                boolean autoDownload = remoteSource.getFileReference().getFileSize() <= 1024 * 1024;// < 1MB
                downloadFileVM = messenger().bindFile(remoteSource.getFileReference(),
                        autoDownload, new DownloadVMCallback());
            } else if (document.getSource() instanceof FileLocalSource) {
                uploadFileVM = messenger().bindUpload(message.getRid(), new UploadVMCallback());
            }
        }
    }

    @Override
    public void onClick(final Message currentMessage) {
        if (document.getSource() instanceof FileRemoteSource) {
            FileRemoteSource remoteSource = (FileRemoteSource) document.getSource();
            final FileReference location = remoteSource.getFileReference();
            messenger().requestState(location.getFileId(), new FileCallback() {
                @Override
                public void onNotDownloaded() {
                    messenger().startDownloading(location);
                }

                @Override
                public void onDownloading(float progress) {
                    messenger().cancelDownloading(location.getFileId());
                }

                @Override
                public void onDownloaded(final FileSystemReference reference) {
                    im.actor.runtime.Runtime.postToMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (document instanceof PhotoContent) {
                                Intents.openMedia(getAdapter().getMessagesFragment().getActivity(), fileIcon, reference.getDescriptor(), currentMessage.getSenderId());
                            } else {
                                try {
                                    Activity activity = getAdapter().getMessagesFragment().getActivity();
                                    activity.startActivity(Intents.openDoc(document.getName(), reference.getDescriptor()));
                                } catch (Exception e) {
                                    Toast.makeText(getAdapter().getMessagesFragment().getActivity(), R.string.toast_unable_open, Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            });
        } else if (document.getSource() instanceof FileLocalSource) {
            messenger().requestUploadState(currentMessage.getRid(), new UploadFileCallback() {
                @Override
                public void onNotUploading() {
                    messenger().resumeUpload(currentMessage.getRid());
                }

                @Override
                public void onUploading(float progress) {
                    messenger().pauseUpload(currentMessage.getRid());
                }

                @Override
                public void onUploaded() {
                    // Nothing to do
                }
            });
        }
    }

    @Override
    public void unbind() {
        super.unbind();

        // Unbinding model
        if (downloadFileVM != null) {
            downloadFileVM.detach();
            downloadFileVM = null;
        }
        if (uploadFileVM != null) {
            uploadFileVM.detach();
            uploadFileVM = null;
        }
    }

    private class DownloadVMCallback implements FileVMCallback {

        @Override
        public void onNotDownloaded() {
            status.setText(R.string.chat_doc_download);
            showView(status);

            goneView(menu);

            // File Icon
            goneView(fileIcon);

            downloadIcon.setResource(R.drawable.ic_cloud_download_white_36dp);
            showView(downloadIcon);
            progressView.setValue(0);
            goneView(progressValue);
            goneView(progressView);
        }

        @Override
        public void onDownloading(float progress) {
            status.setText(R.string.chat_doc_stop);
            showView(status);

            goneView(menu);

            goneView(fileIcon);

            goneView(downloadIcon);
            int val = (int) (progress * 100);
            progressView.setValue(val);
            progressValue.setText("" + val);
            showView(progressView);
            showView(progressValue);
        }

        @Override
        public void onDownloaded(FileSystemReference reference) {
            status.setText(R.string.chat_doc_open);
            showView(status);

            showView(menu);

            showView(fileIcon);

            goneView(downloadIcon);
            goneView(progressValue);
            goneView(progressView);
        }
    }

    private class UploadVMCallback implements UploadFileVMCallback {

        @Override
        public void onNotUploaded() {
            status.setText(R.string.chat_doc_send);
            showView(status);

            goneView(menu);

            // File Icon
            goneView(fileIcon);

            downloadIcon.setResource(R.drawable.ic_cloud_upload_white_36dp);
            showView(downloadIcon);
            progressView.setValue(0);
            goneView(progressValue);
            goneView(progressView);
        }

        @Override
        public void onUploading(float progress) {
            status.setText(R.string.chat_doc_stop);
            showView(status);

            goneView(menu);

            goneView(fileIcon);

            goneView(downloadIcon);
            int val = (int) (progress * 100);
            progressView.setValue(val);
            progressValue.setText("" + val);
            showView(progressView);
            showView(progressValue);
        }

        @Override
        public void onUploaded() {
            onUploading(1);
        }
    }
}
