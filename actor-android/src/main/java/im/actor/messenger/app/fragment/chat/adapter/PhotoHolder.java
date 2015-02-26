package im.actor.messenger.app.fragment.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidkit.engine.uilist.UiList;
import com.droidkit.images.common.ImageLoadException;
import com.droidkit.images.ops.ImageLoading;
import com.droidkit.progress.CircularView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.BubbleContainer;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.Formatter;
import im.actor.messenger.app.view.PhotoPreview;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.model.DownloadState;
import im.actor.messenger.model.UploadState;
import im.actor.messenger.util.Screen;
import im.actor.messenger.util.TextUtils;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.VideoContent;
import im.actor.model.files.FileReference;
import im.actor.model.viewmodel.FileVM;

import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;
import static im.actor.messenger.core.Core.messenger;
import static im.actor.messenger.core.Core.myUid;

/**
 * Created by ex3ndr on 10.09.14.
 */
class PhotoHolder extends BubbleHolder {

    PhotoHolder(Peer peer, MessagesFragment fragment, UiList<Message> uiList) {
        super(fragment, uiList);

    }

    private FrameLayout messageBubble;
    private View overlay;

    private PhotoPreview imageKitView;

    private View progressContainer;
    private TextView progressValue;
    private CircularView progressView;
    private ImageView progressIcon;

    private TextView time;
    private TextView duration;

    private Bitmap smallPreview;

    private DocumentContent fileMessage;
    private Context context;

    private TintImageView stateIcon;

    private int pendingColor;
    private int sentColor;
    private int receivedColor;
    private int readColor;
    private int errorColor;

    private FileVM removeFileVM;

    @Override
    public View init(Message data, ViewGroup viewGroup, Context context) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        BubbleContainer v = (BubbleContainer) inflater.inflate(R.layout.adapter_dialog_photo, viewGroup, false);

        messageBubble = (FrameLayout) v.findViewById(R.id.bubbleContainer);
        imageKitView = (PhotoPreview) v.findViewById(R.id.image);
        time = (TextView) v.findViewById(R.id.time);
        overlay = v.findViewById(R.id.photoOverlay);
        duration = (TextView) v.findViewById(R.id.duration);
        stateIcon = (TintImageView) v.findViewById(R.id.stateIcon);

        progressContainer = v.findViewById(R.id.progressBg);
        progressContainer.setVisibility(View.GONE);
        progressValue = (TextView) v.findViewById(R.id.progressValue);
        progressView = (CircularView) v.findViewById(R.id.progressView);
        progressView.setColor(Color.WHITE);
        progressIcon = (ImageView) v.findViewById(R.id.contentIcon);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            smallPreview = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888);
        }

        pendingColor = context.getResources().getColor(R.color.conv_media_state_pending);
        sentColor = context.getResources().getColor(R.color.conv_media_state_sent);
        receivedColor = context.getResources().getColor(R.color.conv_media_state_delivered);
        readColor = context.getResources().getColor(R.color.conv_media_state_read);
        errorColor = context.getResources().getColor(R.color.conv_media_state_error);

        initBubbleHolder(v, false);
        return v;
    }

    @Override
    public void update(final Message data, final int position, boolean isUpdated, final Context context) {
        super.update(data, position, isUpdated, context);

        this.fileMessage = (DocumentContent) data.getContent();

        if (data.getSenderId() == myUid()) {
            messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_out);
        } else {
            messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_in);
        }

        if (data.getSenderId() == myUid()) {
            stateIcon.setVisibility(View.VISIBLE);
            switch (data.getMessageState()) {
                case ERROR:
                    stateIcon.setResource(R.drawable.msg_error);
                    stateIcon.setTint(errorColor);
                    break;
                default:
                case PENDING:
                    stateIcon.setResource(R.drawable.msg_clock);
                    stateIcon.setTint(pendingColor);
                    break;
                case READ:
                    stateIcon.setResource(R.drawable.msg_check_2);
                    stateIcon.setTint(readColor);
                    break;
                case RECEIVED:
                    stateIcon.setResource(R.drawable.msg_check_2);
                    stateIcon.setTint(receivedColor);
                    break;
                case SENT:
                    stateIcon.setResource(R.drawable.msg_check_1);
                    stateIcon.setTint(sentColor);
                    break;
            }
        } else {
            stateIcon.setVisibility(View.GONE);
        }

        int w, h;
        if (data.getContent() instanceof PhotoContent) {
            w = ((PhotoContent) data.getContent()).getW();
            h = ((PhotoContent) data.getContent()).getH();
            duration.setVisibility(View.GONE);
        } else if (data.getContent() instanceof VideoContent) {
            w = ((VideoContent) data.getContent()).getW();
            h = ((VideoContent) data.getContent()).getH();
            duration.setVisibility(View.VISIBLE);
            duration.setText(Formatter.duration(((VideoContent) data.getContent()).getDuration()));
        } else {
            throw new RuntimeException("Unsupported content");
        }

        int maxHeight = context.getResources().getDisplayMetrics().heightPixels - Screen.dp(96 + 32);
        maxHeight = Math.min(Screen.dp(360), maxHeight);
        int maxWidth = context.getResources().getDisplayMetrics().widthPixels - Screen.dp(32 + 48);
        maxWidth = Math.min(Screen.dp(360), maxWidth);

        float scale = Math.min(maxWidth / (float) w, maxHeight / (float) h);

        int bubbleW = (int) (scale * w);
        int bubbleH = (int) (scale * h);
        imageKitView.setLayoutParams(new FrameLayout.LayoutParams(bubbleW, bubbleH));
        overlay.setLayoutParams(new FrameLayout.LayoutParams(bubbleW, bubbleH));

        time.setText(TextUtils.formatTime(data.getDate()));

        if (isUpdated) {
            imageKitView.setSrc(null);

            if (removeFileVM != null) {
                removeFileVM.detach();
                removeFileVM = null;
            }

            if (fileMessage.getSource() instanceof FileRemoteSource) {
                final DocumentContent doc = fileMessage;
                FileRemoteSource remoteSource = (FileRemoteSource) fileMessage.getSource();
                removeFileVM = new FileVM(remoteSource.getFileLocation(), false, messenger()) {

                    private boolean isFastThumbLoaded = false;

                    private void loadFastThumb() {
                        if (isFastThumbLoaded) {
                            return;
                        }
                        if (doc.getFastThumb() != null) {
                            try {
                                post(ImageLoading.loadBitmap(doc.getFastThumb().getImage()));
                            } catch (ImageLoadException e) {
                                e.printStackTrace();
                            }
                        }
                        isFastThumbLoaded = true;
                    }

                    @Override
                    protected void onObjectReceived(Object obj) {
                        if (obj instanceof Bitmap) {
                            imageKitView.setSrc((Bitmap) obj);
                        } else if (obj instanceof DownloadProgress) {
                            showProgress((int) (100 * ((DownloadProgress) obj).getProgress()));
                        } else if (obj instanceof DownloadStopped) {
                            showIcon(R.drawable.conv_media_download);
                        } else if (obj instanceof DownloadCompleted) {
                            hideProgress(true);
                        }
                    }

                    @Override
                    public void onNotDownloaded() {
                        loadFastThumb();
                        post(new DownloadStopped());
                    }

                    @Override
                    public void onDownloading(float progress) {
                        loadFastThumb();
                        post(new DownloadProgress(progress));
                    }

                    @Override
                    public void onDownloaded(FileReference reference) {
                        post(new DownloadCompleted());
                    }
                };
            }
        } else {

        }

//        if (isUpdated) {
//            if (thumb != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    try {
//                        imageKitView.setSrc(ImageLoading.loadReuse(thumb.getImage(), smallPreview).getRes());
//                    } catch (ImageLoadException e) {
//                        e.printStackTrace();
//                        imageKitView.setSrc(null);
//                    }
//                } else {
//                    try {
//                        imageKitView.setSrc(ImageLoading.loadBitmap(thumb.getImage()));
//                    } catch (ImageLoadException e) {
//                        e.printStackTrace();
//                        imageKitView.setSrc(null);
//                    }
//                }
//            } else {
//                imageKitView.setSrc(null);
//            }
//
//            imageKitView.clear();
//            isInitialBind = true;
//            hideProgress(false);
//            isInitialBind = false;
//        }
//        isInitialBind = true;
//        if (fileMessage.getSource() instanceof FileLocalSource) {
//            FileLocalSource localSource = (FileLocalSource) fileMessage.getSource();
//            if (currentMessage.getContent() instanceof PhotoContent) {
//                imageKitView.requestPhoto(localSource.getFileName());
//            } else if (currentMessage.getContent() instanceof VideoContent) {
//                imageKitView.requestVideo(localSource.getFileName());
//            }
//            // UploadModel.uploadState(message.getRid()).addUiSubscriber(uploadStateListener);
//        } else if (fileMessage.getSource() instanceof FileRemoteSource) {
//            FileRemoteSource remoteSource = (FileRemoteSource) fileMessage.getSource();
//
////            if (fileMessage.isDownloaded()) {
////                if (message.getContent() instanceof PhotoMessage) {
////                    imageKitView.requestPhoto(type, id, message);
////                    hideProgress(true);
////                } else if (message.getContent() instanceof VideoMessage) {
////                    imageKitView.requestVideo(type, id, message);
////                    showIcon(R.drawable.conv_video_play);
////                }
////            } else {
////                DownloadModel.downloadState(fileMessage.getLocation().getFileId()).addUiSubscriber(downloadStateListener);
////            }
//        }
//        isInitialBind = false;
    }

//    private boolean isInitialBind = false;

    private void showIcon(int resId) {
        progressIcon.setImageResource(resId);
        showView(progressContainer, false);
        showView(progressIcon);
        goneView(progressView);
        goneView(progressValue);
    }

    private void hideProgress(boolean success) {
        if (success) {
            progressView.setValue(100);
            progressValue.setText("100");
        }
        goneView(progressContainer);
        goneView(progressView);
        goneView(progressValue);
        goneView(progressIcon);
    }

    private void showProgress(int progress) {
        progressView.setValue(progress);
        progressValue.setText("" + progress);
        showView(progressContainer);
        showView(progressView);
        showView(progressValue);
        goneView(progressIcon);
    }

    private void onUploadChanged(UploadState uploadState) {
        switch (uploadState.getState()) {
            default:
            case NONE:
                showIcon(R.drawable.conv_media_upload);
                break;
            case UPLOADED:
            case UPLOADING:
                showProgress(uploadState.getProgress());
                break;
        }
    }

    private void onDownloadChanged(DownloadState downloadState) {
        switch (downloadState.getState()) {
            default:
            case NONE:
                showIcon(R.drawable.conv_media_download);
//                if (message.getContent() instanceof PhotoMessage) {
//                    DownloadManager.downloader().request(type, id, message.getRid(), fileMessage, true);
//                }
                break;
            case DOWNLOADED:
                hideProgress(true);
                break;
            case DOWNLOADING:
                showProgress(downloadState.getProgress());
                break;
        }
    }

    @Override
    public void onClicked() {
//        if (fileMessage.getUploadPath() != null) {
//            switch (UploadModel.uploadState(message.getRid()).getValue().getAuthState()) {
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
//            if (fileMessage.isDownloaded()) {
//                if (message.getContent() instanceof PhotoMessage) {
//                    Downloaded d = KeyValueEngines.downloaded().get(((PhotoMessage) message.getContent()).getLocation().getFileId());
//                    if (d == null) {
//                        return;
//                    }
//                    context.startActivity(Intents.openPhoto(d));
//                } else if (message.getContent() instanceof VideoMessage) {
//                    Downloaded d = KeyValueEngines.downloaded().get(((VideoMessage) message.getContent()).getLocation().getFileId());
//                    if (d == null) {
//                        return;
//                    }
//                    context.startActivity(Intents.openVideo(d));
//                }
//            } else {
//                switch (DownloadModel.downloadState(fileMessage.getLocation().getFileId()).getValue().getAuthState()) {
//                    case NONE:
//                        DownloadManager.downloader().request(type, id, message.getRid(), fileMessage, false);
//                        break;
//                    case DOWNLOADING:
//                        DownloadManager.downloader().pause(fileMessage);
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
//        if (fileMessage.getLocation() != null) {
//            DownloadModel.downloadState(fileMessage.getLocation().getFileId()).removeUiSubscriber(downloadStateListener);
//        }
        imageKitView.noRequest();
    }

    @Override
    public void dispose() {
        super.dispose();
        imageKitView.noRequest();
        imageKitView.setSrc(null);
        imageKitView.close();
    }

    private class DownloadProgress {
        private float progress;

        private DownloadProgress(float progress) {
            this.progress = progress;
        }

        public float getProgress() {
            return progress;
        }
    }

    private class DownloadStopped {

    }

    private class DownloadCompleted {

    }
}
