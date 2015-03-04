package im.actor.messenger.app.fragment.chat.recycler;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidkit.progress.CircularView;

import im.actor.messenger.R;
import im.actor.messenger.app.fragment.chat.MessagesFragment;
import im.actor.messenger.app.view.Formatter;
import im.actor.messenger.app.view.PhotoPreview;
import im.actor.messenger.app.view.TintImageView;
import im.actor.messenger.util.Screen;
import im.actor.messenger.util.TextUtils;
import im.actor.model.entity.Message;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileLocalSource;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.entity.content.PhotoContent;
import im.actor.model.entity.content.VideoContent;
import im.actor.model.files.FileReference;
import im.actor.model.viewmodel.FileVM;
import im.actor.model.viewmodel.FileVMCallback;

import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;
import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.myUid;

/**
 * Created by ex3ndr on 27.02.15.
 */
public class PhotoHolder extends MessageHolder {

    private final int COLOR_PENDING;
    private final int COLOR_SENT;
    private final int COLOR_RECEIVED;
    private final int COLOR_READ;
    private final int COLOR_ERROR;

    private Context context;

    // Basic bubble
    private FrameLayout messageBubble;
    private View overlay;

    // Content Views
    private PhotoPreview imageKitView;
    private FastThumbLoader fastThumbLoader;

    private TextView time;
    private TextView duration;
    private TintImageView stateIcon;

    // Progress
    private View progressContainer;
    private TextView progressValue;
    private CircularView progressView;
    private ImageView progressIcon;

    // Binded model
    private FileVM downloadFileVM;

    public PhotoHolder(MessagesFragment fragment, View itemView) {
        super(fragment, itemView, false);
        this.context = fragment.getActivity();

        COLOR_PENDING = context.getResources().getColor(R.color.conv_media_state_pending);
        COLOR_SENT = context.getResources().getColor(R.color.conv_media_state_sent);
        COLOR_RECEIVED = context.getResources().getColor(R.color.conv_media_state_delivered);
        COLOR_READ = context.getResources().getColor(R.color.conv_media_state_read);
        COLOR_ERROR = context.getResources().getColor(R.color.conv_media_state_error);

        messageBubble = (FrameLayout) itemView.findViewById(R.id.bubbleContainer);
        overlay = itemView.findViewById(R.id.photoOverlay);

        // Content
        imageKitView = (PhotoPreview) itemView.findViewById(R.id.image);
        fastThumbLoader = new FastThumbLoader(imageKitView);
        time = (TextView) itemView.findViewById(R.id.time);
        duration = (TextView) itemView.findViewById(R.id.duration);

        stateIcon = (TintImageView) itemView.findViewById(R.id.stateIcon);

        progressContainer = itemView.findViewById(R.id.progressBg);
        progressValue = (TextView) itemView.findViewById(R.id.progressValue);
        progressView = (CircularView) itemView.findViewById(R.id.progressView);
        progressView.setColor(Color.WHITE);
        progressIcon = (ImageView) itemView.findViewById(R.id.contentIcon);
    }

    @Override
    protected void bindData(Message message, boolean isNewMessage) {
        // Update model
        DocumentContent fileMessage = (DocumentContent) message.getContent();

        // Update bubble
        if (message.getSenderId() == myUid()) {
            messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_out);
        } else {
            messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_in);
        }

        // Update state
        if (message.getSenderId() == myUid()) {
            stateIcon.setVisibility(View.VISIBLE);
            switch (message.getMessageState()) {
                case ERROR:
                    stateIcon.setResource(R.drawable.msg_error);
                    stateIcon.setTint(COLOR_ERROR);
                    break;
                default:
                case PENDING:
                    stateIcon.setResource(R.drawable.msg_clock);
                    stateIcon.setTint(COLOR_PENDING);
                    break;
                case READ:
                    stateIcon.setResource(R.drawable.msg_check_2);
                    stateIcon.setTint(COLOR_READ);
                    break;
                case RECEIVED:
                    stateIcon.setResource(R.drawable.msg_check_2);
                    stateIcon.setTint(COLOR_RECEIVED);
                    break;
                case SENT:
                    stateIcon.setResource(R.drawable.msg_check_1);
                    stateIcon.setTint(COLOR_SENT);
                    break;
            }
        } else {
            stateIcon.setVisibility(View.GONE);
        }

        // Update time
        time.setText(TextUtils.formatTime(message.getDate()));

        // Update size
        if (isNewMessage) {
            int w, h;
            if (message.getContent() instanceof PhotoContent) {
                w = ((PhotoContent) message.getContent()).getW();
                h = ((PhotoContent) message.getContent()).getH();
                duration.setVisibility(View.GONE);
            } else if (message.getContent() instanceof VideoContent) {
                w = ((VideoContent) message.getContent()).getW();
                h = ((VideoContent) message.getContent()).getH();
                duration.setVisibility(View.VISIBLE);
                duration.setText(Formatter.duration(((VideoContent) message.getContent()).getDuration()));
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
        }

        // Update view
        boolean needRebind = false;
        if (isNewMessage) {
            // Resetting old content state
            imageKitView.noRequest();
            fastThumbLoader.cancel();

            // Resetting binding
            if (downloadFileVM != null) {
                downloadFileVM.detach();
                downloadFileVM = null;
            }
            needRebind = true;
        } else {

        }

        if (needRebind) {
            // Resetting progress state
            progressContainer.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            progressValue.setVisibility(View.GONE);
            progressIcon.setVisibility(View.GONE);

            if (fileMessage.getSource() instanceof FileRemoteSource) {
                boolean autoDownload = fileMessage instanceof PhotoContent;
                downloadFileVM = messenger().bindFile(((FileRemoteSource) fileMessage.getSource()).getFileLocation(),
                        autoDownload, new DownloadVMCallback(fileMessage));
            } else if (fileMessage.getSource() instanceof FileLocalSource) {
                // TODO: Implement
            } else {
                throw new RuntimeException("Unknown file source type: " + fileMessage.getSource());
            }
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

        // Releasing images
        fastThumbLoader.cancel();
        imageKitView.noRequest();
    }

    private class DownloadVMCallback implements FileVMCallback {

        private boolean isFastThumbLoaded = false;
        private DocumentContent doc;

        private DownloadVMCallback(DocumentContent doc) {
            this.doc = doc;
        }

        private void checkFastThumb() {
            if (!isFastThumbLoaded) {
                isFastThumbLoaded = true;
                if (doc.getFastThumb() != null) {
                    fastThumbLoader.request(doc.getFastThumb().getImage());
                }
            }
        }

        @Override
        public void onNotDownloaded() {
            checkFastThumb();
            showView(progressContainer);

            progressIcon.setImageResource(R.drawable.conv_media_download);
            showView(progressIcon);

            goneView(progressView);
            goneView(progressValue);
        }

        @Override
        public void onDownloading(float progress) {
            checkFastThumb();

            showView(progressContainer);

            goneView(progressIcon);

            int val = (int) (100 * progress);
            progressValue.setText(val + "");
            progressView.setValue(val);
            showView(progressView);
            showView(progressValue);
        }

        @Override
        public void onDownloaded(FileReference reference) {
            progressValue.setText(100 + "");
            progressView.setValue(100);

            goneView(progressContainer);
            goneView(progressView);
            goneView(progressValue);
        }
    }
}
