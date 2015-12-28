package im.actor.sdk.controllers.conversation.messages;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import im.actor.core.entity.Message;
import im.actor.core.entity.content.LocationContent;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.UploadFileVM;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.Intents;
import im.actor.sdk.controllers.pickers.map.MapPickerActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.TintImageView;

import static im.actor.sdk.util.ActorSDKMessenger.myUid;

public class LocationHolder extends MessageHolder {

    private final int COLOR_PENDING;
    private final int COLOR_SENT;
    private final int COLOR_RECEIVED;
    private final int COLOR_READ;
    private final int COLOR_ERROR;

    private Context context;

    // Basic bubble
    protected FrameLayout messageBubble;

    // Content Views
    protected SimpleDraweeView previewView;

    protected TextView time;
    protected TintImageView stateIcon;

    // Binded model
    protected FileVM downloadFileVM;
    protected UploadFileVM uploadFileVM;
    protected boolean isPhoto;

    public LocationHolder(MessagesAdapter fragment, View itemView) {
        super(fragment, itemView, false);
        this.context = fragment.getMessagesFragment().getActivity();

        COLOR_PENDING = ActorSDK.sharedActor().style.getConvMediaStatePendingColor();
        COLOR_SENT = ActorSDK.sharedActor().style.getConvMediaStateSentColor();
        COLOR_RECEIVED = ActorSDK.sharedActor().style.getConvMediaStateDeliveredColor();
        COLOR_READ = ActorSDK.sharedActor().style.getConvMediaStateReadColor();
        COLOR_ERROR = ActorSDK.sharedActor().style.getConvMediaStateErrorColor();

        messageBubble = (FrameLayout) itemView.findViewById(R.id.bubbleContainer);

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

        time = (TextView) itemView.findViewById(R.id.time);

        stateIcon = (TintImageView) itemView.findViewById(R.id.stateIcon);
        onConfigureViewHolder();
    }

    @Override
    protected void bindData(Message message, boolean isNewMessage, PreprocessedData preprocessedData) {
        // Update model
        LocationContent locationContent = (LocationContent) message.getContent();

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
        setTimeAndReactions(time);

        previewView.setTag(message.getRid());
        new DownloadImageTask(previewView, message.getRid())
                .execute("https://maps.googleapis.com/maps/api/staticmap?center=" + locationContent.getLatitude() + "," + locationContent.getLongitude() + "&zoom=15&size=200x100&scale=2&maptype=roadmap&markers=color:red%7C" + locationContent.getLatitude() + "," + locationContent.getLongitude());


    }

    @Override
    public void onClick(final Message currentMessage) {
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;

            if (bundle.containsKey("com.google.android.geo.API_KEY")) {
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("latitude", ((LocationContent) currentMessage.getContent()).getLatitude());
                intent.putExtra("longitude", ((LocationContent) currentMessage.getContent()).getLongitude());
                context.startActivity(intent);
            } else {
//                Toast.makeText(context, "please, set up google map api key in AndroidManifest metadata", Toast.LENGTH_LONG).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unbind() {
        super.unbind();

        previewView.setImageURI(null);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, File> {
        ImageView bmImage;
        Long id;
        File file;

        public DownloadImageTask(ImageView bmImage, Long id) {
            this.bmImage = bmImage;
            this.id = id;
        }

        protected File doInBackground(String... params) {
            String urldisplay = params[0];
            InputStream in = null;
            try {
                file = new File(context.getCacheDir(), id + "_map");
                if (!file.exists()) {
                    in = new java.net.URL(urldisplay).openStream();
                    OutputStream output = new FileOutputStream(file);
                    try {
                        try {
                            byte[] buffer = new byte[4 * 1024];
                            int read;

                            while ((read = in.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                            output.flush();
                        } finally {
                            output.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            return file;
        }

        protected void onPostExecute(File result) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(result))
                    .setResizeOptions(new ResizeOptions(previewView.getLayoutParams().width,
                            previewView.getLayoutParams().height))
                    .build();
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                    .setOldController(previewView.getController())
                    .setImageRequest(request)
                    .build();
            previewView.setController(controller);
        }
    }


}
