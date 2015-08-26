package im.actor.messenger.app.fragment.chat.messages;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidkit.progress.CircularView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import im.actor.core.entity.FileReference;
import im.actor.core.entity.Message;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileLocalSource;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.entity.content.PhotoContent;
import im.actor.core.entity.content.VideoContent;
import im.actor.core.viewmodel.FileCallback;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.core.viewmodel.UploadFileCallback;
import im.actor.core.viewmodel.UploadFileVM;
import im.actor.core.viewmodel.UploadFileVMCallback;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.fragment.chat.view.FastThumbLoader;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.util.TextUtils;
import im.actor.messenger.app.view.TintImageView;
import im.actor.runtime.files.FileSystemReference;

import static im.actor.messenger.app.core.Core.messenger;
import static im.actor.messenger.app.core.Core.myUid;
import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;

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
    private SimpleDraweeView previewView;
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
    private UploadFileVM uploadFileVM;
    private boolean isPhoto;

    public PhotoHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView, false);
        this.context = fragment.getMessagesFragment().getActivity();

        COLOR_PENDING = context.getResources().getColor(R.color.conv_media_state_pending);
        COLOR_SENT = context.getResources().getColor(R.color.conv_media_state_sent);
        COLOR_RECEIVED = context.getResources().getColor(R.color.conv_media_state_delivered);
        COLOR_READ = context.getResources().getColor(R.color.conv_media_state_read);
        COLOR_ERROR = context.getResources().getColor(R.color.conv_media_state_error);

        messageBubble = (FrameLayout) itemView.findViewById(R.id.bubbleContainer);
        overlay = itemView.findViewById(R.id.photoOverlay);

        // Content
        previewView = (SimpleDraweeView) itemView.findViewById(R.id.image);
        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(context.getResources());

        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(200)
                .setRoundingParams(new RoundingParams()
                        .setCornersRadius(Screen.dp(2))
                        .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY))
                .build();
        previewView.setHierarchy(hierarchy);

        fastThumbLoader = new FastThumbLoader(previewView);
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
    protected void bindData(Message message, boolean isNewMessage, PreprocessedData preprocessedData) {
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
                isPhoto = true;
                duration.setVisibility(View.GONE);
            } else if (message.getContent() instanceof VideoContent) {
                w = ((VideoContent) message.getContent()).getW();
                h = ((VideoContent) message.getContent()).getH();
                isPhoto = false;
                duration.setVisibility(View.VISIBLE);
                duration.setText(messenger().getFormatter().formatDuration(((VideoContent) message.getContent()).getDuration()));
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
            previewView.setLayoutParams(new FrameLayout.LayoutParams(bubbleW, bubbleH));
            overlay.setLayoutParams(new FrameLayout.LayoutParams(bubbleW, bubbleH));
        }

        // Update view
        boolean needRebind = false;
        if (isNewMessage) {
            // Resetting old content state
            // imageKitView.noRequest();
            fastThumbLoader.cancel();

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
        }

        if (needRebind) {
            // Resetting progress state
            progressContainer.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            progressValue.setVisibility(View.GONE);
            progressIcon.setVisibility(View.GONE);

            if (fileMessage.getSource() instanceof FileRemoteSource) {
                boolean autoDownload = fileMessage instanceof PhotoContent;
                previewView.setImageURI(null);
                downloadFileVM = messenger().bindFile(((FileRemoteSource) fileMessage.getSource()).getFileReference(),
                        autoDownload, new DownloadVMCallback(fileMessage));
            } else if (fileMessage.getSource() instanceof FileLocalSource) {
                uploadFileVM = messenger().bindUpload(message.getRid(), new UploadVMCallback());
                if (isPhoto) {
                    previewView.setImageURI(Uri.fromFile(
                            new File(((FileLocalSource) fileMessage.getSource()).getFileDescriptor())));
                } else {
                    previewView.setImageURI(null);
                    //TODO: better approach?
                    if (fileMessage.getFastThumb() != null) {
                        fastThumbLoader.request(fileMessage.getFastThumb().getImage());
                    }
                }
            } else {
                throw new RuntimeException("Unknown file source type: " + fileMessage.getSource());
            }
        }
    }

    @Override
    public void onClick(final Message currentMessage) {
        final DocumentContent document = (DocumentContent) currentMessage.getContent();
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
                                Intents.openMedia(getAdapter().getMessagesFragment().getActivity(), previewView, reference.getDescriptor(), currentMessage.getSenderId());
                            } else {
                                Activity activity = getAdapter().getMessagesFragment().getActivity();
                                activity.startActivity(Intents.openDoc(document.getName(), reference.getDescriptor()));
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

        // Releasing images
        fastThumbLoader.cancel();
        previewView.setImageURI(null);
    }

    private class UploadVMCallback implements UploadFileVMCallback {

        @Override
        public void onNotUploaded() {
            showView(progressContainer);

            progressIcon.setImageResource(R.drawable.conv_media_upload);
            showView(progressIcon);

            goneView(progressView);
            goneView(progressValue);
        }

        @Override
        public void onUploading(float progress) {
            showView(progressContainer);

            goneView(progressIcon);

            int val = (int) (100 * progress);
            progressValue.setText(val + "");
            progressView.setValue(val);
            showView(progressView);
            showView(progressValue);
        }

        @Override
        public void onUploaded() {
            progressValue.setText(100 + "");
            progressView.setValue(100);

            goneView(progressContainer);
            goneView(progressView);
            goneView(progressValue);
        }
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
        public void onDownloaded(FileSystemReference reference) {
            if (isPhoto) {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(reference.getDescriptor())))
                        .setResizeOptions(new ResizeOptions(previewView.getLayoutParams().width,
                                previewView.getLayoutParams().height))
                        .build();
                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setOldController(previewView.getController())
                        .setImageRequest(request)
                        .build();
                previewView.setController(controller);
                // previewView.setImageURI(Uri.fromFile(new File(reference.getDescriptor())));
            } else {
                checkFastThumb();
            }

            progressValue.setText(100 + "");
            progressView.setValue(100);

            goneView(progressContainer);
            goneView(progressView);
            goneView(progressValue);
        }
    }
}
