package im.actor.sdk.controllers.conversation.messages;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidkit.progress.CircularView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import im.actor.core.entity.FileReference;
import im.actor.core.entity.Message;
import im.actor.core.entity.content.AnimationContent;
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
import im.actor.runtime.Log;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.conversation.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.preprocessor.PreprocessedData;
import im.actor.sdk.controllers.conversation.view.FastBitmapDrawable;
import im.actor.sdk.controllers.conversation.view.FastThumbLoader;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.TintImageView;
import im.actor.runtime.files.FileSystemReference;

import static im.actor.sdk.util.ViewUtils.goneView;
import static im.actor.sdk.util.ViewUtils.showView;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;
import static im.actor.sdk.util.ActorSDKMessenger.myUid;

public class PhotoHolder extends MessageHolder {

    public static final String TAG = "PHOTO_HOLDER";
    private final int COLOR_PENDING;
    private final int COLOR_SENT;
    private final int COLOR_RECEIVED;
    private final int COLOR_READ;
    private final int COLOR_ERROR;

    private Context context;

    // Basic bubble
    protected FrameLayout messageBubble;
    protected View overlay;

    // Content Views
    protected SimpleDraweeView previewView;
    private FastThumbLoader fastThumbLoader;

    protected TextView time;
    protected TextView duration;
    protected TintImageView stateIcon;

    // Progress
    protected View progressContainer;
    protected TextView progressValue;
    protected CircularView progressView;
    protected ImageView progressIcon;

    // Binded model
    protected FileVM downloadFileVM;
    protected UploadFileVM uploadFileVM;
    protected boolean isPhoto;
    protected boolean isAnimation;


    int lastUpdatedIndex = 0;
    long currenrRid = 0;
    private boolean updated = false;
    private boolean playRequested = false;
    private final ControllerListener animationController;
    private Animatable anim;

    public PhotoHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView, false);
        this.context = fragment.getMessagesFragment().getActivity();

        COLOR_PENDING = ActorSDK.sharedActor().style.getConvMediaStatePendingColor();
        COLOR_SENT = ActorSDK.sharedActor().style.getConvMediaStateSentColor();
        COLOR_RECEIVED = ActorSDK.sharedActor().style.getConvMediaStateDeliveredColor();
        COLOR_READ = ActorSDK.sharedActor().style.getConvMediaStateReadColor();
        COLOR_ERROR = ActorSDK.sharedActor().style.getConvMediaStateErrorColor();

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

        animationController = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(
                    String id,
                    ImageInfo imageInfo,
                    Animatable anim) {
                PhotoHolder.this.anim = anim;
                playAnimation();
            }
        };

        fastThumbLoader = new FastThumbLoader(previewView);
        time = (TextView) itemView.findViewById(R.id.time);
        duration = (TextView) itemView.findViewById(R.id.duration);

        stateIcon = (TintImageView) itemView.findViewById(R.id.stateIcon);

        progressContainer = itemView.findViewById(R.id.progressBg);
        progressValue = (TextView) itemView.findViewById(R.id.progressValue);
        progressValue.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());
        progressView = (CircularView) itemView.findViewById(R.id.progressView);
        progressView.setColor(Color.WHITE);
        progressIcon = (ImageView) itemView.findViewById(R.id.contentIcon);
        onConfigureViewHolder();
    }

    @Override
    protected void bindData(Message message, long readDate, long receiveDate, boolean isNewMessage, PreprocessedData preprocessedData) {
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
                case SENT:
                    if (message.getSortDate() <= readDate) {
                        stateIcon.setResource(R.drawable.msg_check_2);
                        stateIcon.setTint(COLOR_READ);
                    } else if (message.getSortDate() <= receiveDate) {
                        stateIcon.setResource(R.drawable.msg_check_2);
                        stateIcon.setTint(COLOR_RECEIVED);
                    } else {
                        stateIcon.setResource(R.drawable.msg_check_1);
                        stateIcon.setTint(COLOR_SENT);
                    }
                    break;
            }
        } else {
            stateIcon.setVisibility(View.GONE);
        }

        // Update time
        setTimeAndReactions(time);
        Log.d(TAG, "isNewMessage: " + isNewMessage);
        // Update size
        if (isNewMessage) {
            int w, h;
            if (message.getContent() instanceof PhotoContent) {
                w = ((PhotoContent) message.getContent()).getW();
                h = ((PhotoContent) message.getContent()).getH();
                isPhoto = true;
                isAnimation = false;
                duration.setVisibility(View.GONE);
            } else if (message.getContent() instanceof AnimationContent) {
                w = ((AnimationContent) message.getContent()).getW();
                h = ((AnimationContent) message.getContent()).getH();
                isPhoto = true;
                isAnimation = true;
                duration.setVisibility(View.VISIBLE);
                duration.setText("");
            } else if (message.getContent() instanceof VideoContent) {
                w = ((VideoContent) message.getContent()).getW();
                h = ((VideoContent) message.getContent()).getH();
                isPhoto = false;
                isAnimation = false;
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
        Log.d(TAG, "needRebind by new: " + needRebind);

        updated = false;
//        int updatedCounter = fileMessage.getUpdatedCounter();
//        Log.d(TAG, "oldRid: " + currenrRid);
//        Log.d(TAG, "newRid: " + currentMessage.getRid());
//        Log.d(TAG, "oldCounter: " + lastUpdatedIndex);
//        Log.d(TAG, "newCounter: " + updatedCounter);

//        if (currenrRid == currentMessage.getRid() && lastUpdatedIndex != updatedCounter) {
//            updated = true;
//            needRebind = true;
//            lastUpdatedIndex = updatedCounter;
//        }
        currenrRid = currentMessage.getRid();
        Log.d(TAG, "updated: " + updated);


        if (needRebind) {
            anim = null;
            if (!updated) {
                playRequested = false;
            }
            // Resetting progress state
            progressContainer.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            progressValue.setVisibility(View.GONE);
            progressIcon.setVisibility(View.GONE);

            if (fileMessage.getSource() instanceof FileRemoteSource) {
                boolean autoDownload = fileMessage instanceof PhotoContent;
                if (!updated) {
                    previewView.setImageURI(null);
                }
                downloadFileVM = messenger().bindFile(((FileRemoteSource) fileMessage.getSource()).getFileReference(),
                        autoDownload, new DownloadVMCallback(fileMessage));
            } else if (fileMessage.getSource() instanceof FileLocalSource) {
                uploadFileVM = messenger().bindUpload(message.getRid(), new UploadVMCallback());
                if (isPhoto) {
                    Uri uri = Uri.fromFile(
                            new File(((FileLocalSource) fileMessage.getSource()).getFileDescriptor()));
                    bindImage(uri);
                } else {
                    if (!updated) {
                        previewView.setImageURI(null);
                        Log.d(TAG, "rebind video - setImageURI(null)!");

                    }
                    //TODO: better approach?
                    if (fileMessage.getFastThumb() != null && !updated) {
                        fastThumbLoader.request(fileMessage.getFastThumb().getImage());
                        Log.d(TAG, "rebind video- new thumb!");

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
                    playRequested = true;
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
                            } else if (document instanceof VideoContent) {
                                playVideo(document, reference);
                            } else if (document instanceof AnimationContent) {
                                toggleAnimation();
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

    private void playAnimation() {
        playAnimation(messenger().isAnimationAutoPlayEnabled());
    }

    private void playAnimation(boolean play) {
        if (anim != null) {
            if (play) {
                anim.start();
            } else {
                anim.stop();
            }
        }
    }

    private void toggleAnimation() {
        if (anim != null) {
            if (anim.isRunning()) {
                anim.stop();
            } else {
                anim.start();
            }
        }
    }

    public void playVideo(DocumentContent document, FileSystemReference reference) {
        Activity activity = getAdapter().getMessagesFragment().getActivity();
        if (activity != null) {
            activity.startActivity(Intents.openDoc(document.getName(), reference.getDescriptor()));
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
        previewView.destroyDrawingCache();

        playRequested = false;
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
            if (!updated) {
                checkFastThumb();

                showView(progressContainer);

                goneView(progressIcon);

                int val = (int) (100 * progress);
                progressValue.setText(val + "");
                progressView.setValue(val);
                showView(progressView);
                showView(progressValue);

            }
        }

        @Override
        public void onDownloaded(FileSystemReference reference) {
            if (isPhoto) {
                if (updated) {
                    previewView.destroyDrawingCache();
                    previewView.buildDrawingCache();
                    Bitmap drawingCache = previewView.getDrawingCache();
                    if (drawingCache != null && !drawingCache.isRecycled()) {
                        previewView.getHierarchy().setPlaceholderImage(new FastBitmapDrawable(drawingCache));
                    }
                }
                Uri uri = Uri.fromFile(new File(reference.getDescriptor()));
                bindImage(uri);
                if (isAnimation && !updated) {
                    checkFastThumb();
                }
                // previewView.setImageURI(Uri.fromFile(new File(reference.getDescriptor())));
            } else {
                if (!updated) {
                    checkFastThumb();
                }
                if (playRequested) {
                    playRequested = false;
                    playVideo((DocumentContent) currentMessage.getContent(), reference);
                }
            }

            progressValue.setText(100 + "");
            progressView.setValue(100);

            goneView(progressContainer);
            goneView(progressView);
            goneView(progressValue);
        }
    }

    public void bindImage(Uri uri) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(previewView.getLayoutParams().width,
                        previewView.getLayoutParams().height))
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(previewView.getController())
                .setImageRequest(request)
                .setControllerListener(animationController)
                .build();
        previewView.setController(controller);
    }
}
