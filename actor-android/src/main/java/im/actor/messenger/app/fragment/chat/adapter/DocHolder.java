package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;

import com.droidkit.engine.uilist.UiList;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.mvvm.ValueChangeListener;
import com.droidkit.progress.CircularView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.model.DownloadState;
import im.actor.messenger.model.UploadState;
import im.actor.messenger.util.FileTypes;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.DocumentContent;


/**
 * Created by ex3ndr on 20.09.14.
 */
public class DocHolder extends BubbleHolder {

    private TextView fileName;
    private TextView fileSize;

    private ImageView fileIcon;

    private TintImageView downloadIcon;

    // private View iconContainer;
    private CircularView progressView;
    private TextView progressValue;

    private TextView status;

    private View bubbleView;

    private View menu;

    private Context context;

    private DocumentContent documentMessage;

    private ValueChangeListener<UploadState> uploadStateListener = new ValueChangeListener<UploadState>() {
        @Override
        public void onChanged(UploadState value) {
            onUploadChanged(value);
        }
    };

    private ValueChangeListener<DownloadState> downloadStateListener = new ValueChangeListener<DownloadState>() {
        @Override
        public void onChanged(DownloadState value) {
            onDownloadChanged(value);
        }
    };

    protected DocHolder(Peer peer, MessagesFragment fragment, UiList<Message> uiList) {
        super(peer, fragment, uiList);
    }

    @Override
    public View init(Message data, ViewGroup viewGroup, final Context context) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        BubbleContainer v = (BubbleContainer) inflater.inflate(R.layout.adapter_dialog_doc, viewGroup, false);
        bubbleView = v.findViewById(R.id.bubbleContainer);
        bubbleView.setBackgroundResource(R.drawable.conv_bubble_media_in);
        fileName = (TextView) v.findViewById(R.id.fileName);
        fileSize = (TextView) v.findViewById(R.id.fileSize);
        fileIcon = (ImageView) v.findViewById(R.id.icon);
        status = (TextView) v.findViewById(R.id.status);
        downloadIcon = (TintImageView) v.findViewById(R.id.downloading);
        // iconContainer = v.findViewById(R.id.iconContainer);
        progressView = (CircularView) v.findViewById(R.id.progressView);
        progressView.setColor(context.getResources().getColor(R.color.primary));
        progressValue = (TextView) v.findViewById(R.id.progressValue);
        menu = v.findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
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
        initBubbleHolder(v, false);
        return v;
    }

    @Override
    public void update(final Message message, int pos, boolean isUpdated, Context context) {
        super.update(message, pos, isUpdated, context);
        this.documentMessage = (DocumentContent) message.getContent();

        String ext = "";
        int dotIndex = documentMessage.getName().lastIndexOf('.');
        if (dotIndex >= 0) {
            ext = documentMessage.getName().substring(dotIndex + 1);
        }

        fileName.setText(documentMessage.getName());
        // fileSize.setText(Formatter.formatFileSize(documentMessage.getSize()) + " " + ext.toUpperCase());

        boolean isAppliedThumb = false;
        if (documentMessage.getFastThumb() != null) {
            try {
                Bitmap img = ImageLoading.loadBitmap(documentMessage.getFastThumb().getImage());
                fileIcon.setImageBitmap(img);
                fileIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                isAppliedThumb = true;
            } catch (ImageLoadException e) {
                e.printStackTrace();
            }
        }
        if (!isAppliedThumb) {
            int type = FileTypes.getType(ext);
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

//        if (documentMessage.getUploadPath() != null) {
//            UploadModel.uploadState(message.getRid()).addUiSubscriber(uploadStateListener);
//        } else {
//            if (documentMessage.isDownloaded()) {
//                status.setText(R.string.chat_doc_open);
//                fileIcon.setVisibility(View.VISIBLE);
//                menu.setVisibility(View.VISIBLE);
//                downloadIcon.setVisibility(View.GONE);
//                progressValue.setVisibility(View.GONE);
//                progressView.setVisibility(View.GONE);
//            } else {
//                DownloadModel.downloadState(documentMessage.getLocation().getFileId()).addUiSubscriber(downloadStateListener);
//            }
//        }
    }

    public void onUploadChanged(UploadState value) {
        switch (value.getState()) {
            default:
            case NONE:
                status.setText(R.string.chat_doc_send);
                downloadIcon.setVisibility(View.VISIBLE);
                downloadIcon.setResource(R.drawable.ic_cloud_upload_white_36dp);
                fileIcon.setVisibility(View.GONE);
                progressValue.setVisibility(View.GONE);
                progressView.setVisibility(View.GONE);
                break;
            case UPLOADED:
            case UPLOADING:
                status.setText(R.string.chat_doc_stop);
                downloadIcon.setVisibility(View.GONE);
                fileIcon.setVisibility(View.GONE);
                progressValue.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.VISIBLE);
                progressView.setValue(value.getProgress());
                progressValue.setText("" + value.getProgress());
                break;
        }
    }

    public void onDownloadChanged(DownloadState value) {
        switch (value.getState()) {
            default:
            case NONE:
                status.setText(R.string.chat_doc_download);
                menu.setVisibility(View.GONE);
                break;
            case DOWNLOADING:
                status.setText(R.string.chat_doc_stop);
                menu.setVisibility(View.GONE);
                break;
            case DOWNLOADED:
                status.setText(R.string.chat_doc_open);
                menu.setVisibility(View.VISIBLE);
                break;
        }

        if (value.getState() == DownloadState.State.DOWNLOADING) {
            fileIcon.setVisibility(View.GONE);
            downloadIcon.setVisibility(View.GONE);
            progressValue.setVisibility(View.VISIBLE);
            progressView.setVisibility(View.VISIBLE);
            progressView.setValue(value.getProgress());
            progressValue.setText("" + value.getProgress());
        } else {
            if (value.getState() == DownloadState.State.DOWNLOADED) {
                fileIcon.setVisibility(View.VISIBLE);
                downloadIcon.setVisibility(View.GONE);
            } else {
                fileIcon.setVisibility(View.GONE);
                downloadIcon.setVisibility(View.VISIBLE);
                downloadIcon.setResource(R.drawable.ic_cloud_download_white_36dp);
            }

            progressValue.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClicked() {
//        if (documentMessage.getUploadPath() != null) {
//            switch (UploadModel.uploadState(message.getRid()).getValue().getState()) {
//                default:
//                case NONE:
//                    MessageDeliveryActor.messageSender().mediaTryAgain(type, id, message.getRid());
//                    break;
//                case UPLOADING:
//                    MessageDeliveryActor.messageSender().mediaPause(type, id, message.getRid());
//                    break;
//                case UPLOADED:
//                    break;
//            }
//        } else {
//            if (documentMessage.isDownloaded()) {
//                Downloaded d = KeyValueEngines.downloaded().get(documentMessage.getLocation().getFileId());
//                if (d == null) {
//                    return;
//                }
//                context.startActivity(Intents.openDoc(d));
//            } else {
//                switch (DownloadModel.downloadState(documentMessage.getLocation().getFileId()).getValue().getState()) {
//                    case NONE:
//                        DownloadManager.downloader().request(type, id, message.getRid(), documentMessage, false);
//                        break;
//                    case DOWNLOADING:
//                        DownloadManager.downloader().pause(documentMessage);
//                        break;
//                    case DOWNLOADED:
//                        break;
//                }
//            }
//        }
    }

    @Override
    public void unbind() {
        super.unbind();
//        UploadModel.uploadState(message.getRid()).removeUiSubscriber(uploadStateListener);
//        if (documentMessage.getLocation() != null) {
//            DownloadModel.downloadState(documentMessage.getLocation().getFileId()).removeUiSubscriber(downloadStateListener);
//        }
    }
}
