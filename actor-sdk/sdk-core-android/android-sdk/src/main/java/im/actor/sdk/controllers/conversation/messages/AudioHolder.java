package im.actor.sdk.controllers.conversation.messages;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.droidkit.progress.CircularView;

import im.actor.core.entity.Message;
import im.actor.core.entity.content.DocumentContent;
import im.actor.core.entity.content.FileLocalSource;
import im.actor.core.entity.content.FileRemoteSource;
import im.actor.core.entity.content.VoiceContent;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.core.viewmodel.UploadFileVM;
import im.actor.core.viewmodel.UploadFileVMCallback;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.files.FileSystemReference;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.MessagesAdapter;
import im.actor.sdk.controllers.conversation.messages.preprocessor.PreprocessedData;
import im.actor.sdk.core.audio.AudioPlayerActor;
import im.actor.sdk.view.TintImageView;

import static im.actor.sdk.util.ActorSDKMessenger.myUid;
import static im.actor.sdk.util.ViewUtils.goneView;
import static im.actor.sdk.util.ViewUtils.showView;


public class AudioHolder extends MessageHolder {

    private int waitColor;
    private int sentColor;
    private int deliveredColor;
    private int readColor;
    private int errorColor;
    private final TintImageView stateIcon;
    private final TextView time;
    private final TextView duration;
    private final SeekBar seekBar;
    private final CircularView progressView;

    private Context context;

    protected ViewGroup mainContainer;
    protected FrameLayout messageBubble;
    protected AudioPlayerActor.AudioPlayerCallback callback;
    protected String currentAudio;
    protected static String currentPlayingAudio;
    protected ImageView playBtn;
    protected static ActorRef audioActor;
    protected FileVM downloadFileVM;
    protected UploadFileVM uploadFileVM;
    protected long currentDuration;
    protected boolean treckingTouch;
    protected Handler mainThread;

    public AudioHolder(MessagesAdapter fragment, final View itemView) {
        super(fragment, itemView, false);
        context = fragment.getMessagesFragment().getContext();
        mainThread = new Handler(context.getMainLooper());
        waitColor = ActorSDK.sharedActor().style.getConvStatePendingColor();
        sentColor = ActorSDK.sharedActor().style.getConvStateSentColor();
        deliveredColor = ActorSDK.sharedActor().style.getConvStateDeliveredColor();
        readColor = ActorSDK.sharedActor().style.getConvStateReadColor();
        errorColor = ActorSDK.sharedActor().style.getConvStateErrorColor();

        if (audioActor == null) {
            audioActor = ActorSystem.system().actorOf(Props.create(new ActorCreator() {
                @Override
                public AudioPlayerActor create() {
                    return new AudioPlayerActor(context);
                }
            }), "actor/audio_player");

        }

        stateIcon = (TintImageView) itemView.findViewById(R.id.stateIcon);
        time = (TextView) itemView.findViewById(R.id.time);
        time.setTextColor(ActorSDK.sharedActor().style.getConvTimeColor());
        duration = (TextView) itemView.findViewById(R.id.duration);
        duration.setTextColor(ActorSDK.sharedActor().style.getConvTimeColor());
        seekBar = (SeekBar) itemView.findViewById(R.id.audioSlide);
        progressView = (CircularView) itemView.findViewById(R.id.progressView);
        progressView.setColor(context.getResources().getColor(R.color.primary));
        progressView.setMaxValue(100);
//        seekBar.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return true;
//            }
//        });
//        seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float seek = (float) seekBar.getProgress() / (float) seekBar.getMax();
                duration.setText(ActorSDK.sharedActor().getMessenger().getFormatter().formatDuration((int) (seek * currentDuration / 1000)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                treckingTouch = true;
                audioActor.send(new AudioPlayerActor.Stop());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float progress = (float) seekBar.getProgress() / (float) seekBar.getMax();
                audioActor.send(new AudioPlayerActor.Seek(progress, currentAudio));
                currentPlayingAudio = currentAudio;
            }
        });

        mainContainer = (ViewGroup) itemView.findViewById(R.id.mainContainer);
        messageBubble = (FrameLayout) itemView.findViewById(R.id.fl_bubble);
        playBtn = (ImageView) itemView.findViewById(R.id.contact_avatar);
        playBtn.getBackground().setColorFilter(Color.parseColor("#4295e3"), PorterDuff.Mode.MULTIPLY);
        callback = new AudioPlayerActor.AudioPlayerCallback() {
            @Override
            public void onStart(final String fileName) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        play(fileName);
                    }
                });
            }

            @Override
            public void onStop(final String fileName) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                    }
                });
            }

            @Override
            public void onPause(final String fileName, float progress) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if (currentAudio != null && currentAudio.equals(fileName)) {
                            pause();
                        }
                    }
                });
            }

            @Override
            public void onProgress(final String fileName, final float progress) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if (currentAudio != null && currentAudio.equals(fileName) && currentPlayingAudio.equals(currentAudio)) {
                            if (!treckingTouch) progress(progress);
                        }
                    }
                });
            }

            @Override
            public void onError(final String fileName) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        if (currentAudio != null && currentAudio.equals(fileName)) {
                            Toast.makeText(context, "error playing this file", Toast.LENGTH_SHORT).show();
                            keepScreenOn(false);
                        }
                    }
                });
            }
        };

        audioActor.send(new AudioPlayerActor.RegisterCallback(callback));
        onConfigureViewHolder();
    }

    private void play(String fileName) {
        if (currentAudio != null && currentAudio.equals(fileName)) {
            playBtn.setImageResource(R.drawable.ic_pause_white_24dp);
            keepScreenOn(true);
        } else {
            stop();
        }
    }

    private void progress(float progress) {
        playBtn.setImageResource(R.drawable.ic_pause_white_24dp);
        duration.setText(ActorSDK.sharedActor().getMessenger().getFormatter().formatDuration((int) (progress * currentDuration / 1000)));
        seekBar.setProgress((int) (100 * progress));
    }

    private void stop() {
        if (!treckingTouch) {
            seekBar.setProgress(0);
            playBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            duration.setText(ActorSDK.sharedActor().getMessenger().getFormatter().formatDuration((int) (currentDuration / 1000)));
        }
        treckingTouch = false;
        keepScreenOn(false);
    }

    private void pause() {
        playBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        keepScreenOn(false);
    }

    @Override
    protected void bindData(final Message message, long readDate, long receiveDate, boolean isUpdated, PreprocessedData preprocessedData) {

        VoiceContent audioMsg = (VoiceContent) message.getContent();
        if (message.getSenderId() == myUid()) {
//            messageBubble.getBackground().setColorFilter(messageBubble.getContext().getResources().getColor(R.color.conv_bubble), PorterDuff.Mode.MULTIPLY);
            messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_out);
        } else {
            messageBubble.setBackgroundResource(R.drawable.conv_bubble_media_in);
            messageBubble.getBackground().setColorFilter(null);
        }

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
        currentDuration = ((VoiceContent) message.getContent()).getDuration();
        duration.setText(ActorSDK.sharedActor().getMessenger().getFormatter().formatDuration((int) (currentDuration / 1000)));

        // Update view
        boolean needRebind = false;
        if (isUpdated) {
            // Resetting old content state

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

            if (audioMsg.getSource() instanceof FileRemoteSource) {
                boolean autoDownload = audioMsg instanceof VoiceContent;
                downloadFileVM = ActorSDK.sharedActor().getMessenger().bindFile(((FileRemoteSource) audioMsg.getSource()).getFileReference(),
                        autoDownload, new DownloadVMCallback(audioMsg));
            } else if (audioMsg.getSource() instanceof FileLocalSource) {
                uploadFileVM = ActorSDK.sharedActor().getMessenger().bindUpload(message.getRid(), new UploadVMCallback());
                currentAudio = ((FileLocalSource) audioMsg.getSource()).getFileDescriptor();
                stop();
                bindPlayButton();
            } else {
                throw new RuntimeException("Unknown file source type: " + audioMsg.getSource());
            }
        }

    }

    private class DownloadVMCallback implements FileVMCallback {

        private DocumentContent doc;

        private DownloadVMCallback(DocumentContent doc) {
            this.doc = doc;
        }

        @Override
        public void onNotDownloaded() {
            goneView(progressView);
            playBtn.setImageResource(R.drawable.msg_audio_download_selector);
            playBtn.setOnClickListener(null);
        }

        @Override
        public void onDownloading(float progress) {
            playBtn.setImageResource(R.drawable.msg_audio_download_selector);
            progressView.setValue((int) (progress * 100));
            showView(progressView);
            playBtn.setOnClickListener(null);
        }

        @Override
        public void onDownloaded(FileSystemReference reference) {
            currentAudio = reference.getDescriptor();
            goneView(progressView);
            stop();
            bindPlayButton();
        }
    }

    private class UploadVMCallback implements UploadFileVMCallback {

        @Override
        public void onNotUploaded() {
            goneView(progressView);
//            playBtn.setImageResource(R.drawable.msg_audio_download_selector);
//            playBtn.setOnClickListener(null);
            stop();
            bindPlayButton();
        }

        @Override
        public void onUploading(float progress) {
            progressView.setValue((int) (progress * 100));
            showView(progressView);
        }

        @Override
        public void onUploaded() {
            goneView(progressView);
            stop();
            bindPlayButton();
        }
    }

    private void bindPlayButton() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioActor.send(new AudioPlayerActor.Toggle(currentAudio));
                currentPlayingAudio = currentAudio;
            }
        });
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

        audioActor.send(new AudioPlayerActor.RemoveCallback(callback));

        messageBubble.getBackground().setColorFilter(null);
    }

    public static void stopPlaying() {
        if (audioActor != null) {
            audioActor.send(new AudioPlayerActor.Stop());
        }
    }

    private void keepScreenOn(boolean on) {
        if (context != null) {
            Window window =  ((FragmentActivity) context).getWindow();
            if (on == true)
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else
                window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }
    }
}
