package im.actor.sdk.view.avatar;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;

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

import im.actor.core.entity.Avatar;
import im.actor.core.entity.AvatarImage;
import im.actor.core.entity.Contact;
import im.actor.core.entity.Dialog;
import im.actor.core.entity.PublicGroup;
import im.actor.core.viewmodel.FileVM;
import im.actor.core.viewmodel.FileVMCallback;
import im.actor.core.viewmodel.GroupVM;
import im.actor.core.viewmodel.UserVM;
import im.actor.runtime.actors.Actor;
import im.actor.runtime.actors.ActorCreator;
import im.actor.runtime.actors.ActorRef;
import im.actor.runtime.actors.ActorSystem;
import im.actor.runtime.actors.Props;
import im.actor.runtime.android.AndroidContext;
import im.actor.runtime.files.FileSystemReference;
import im.actor.sdk.controllers.activity.BaseActivity;
import im.actor.sdk.controllers.conversation.view.FastThumbLoader;
import im.actor.sdk.util.Screen;

import static im.actor.runtime.actors.ActorSystem.system;
import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class CallBackgroundAvatarView extends SimpleDraweeView {

    private FileVM bindedFile;
    private long currentId;
    private FastThumbLoader fastThumbLoader;

    private static ActorRef blurActor;

    public CallBackgroundAvatarView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        init();
    }

    public CallBackgroundAvatarView(Context context) {
        super(context);
        init();
    }

    public CallBackgroundAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CallBackgroundAvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        if(blurActor==null){
            blurActor = system().actorOf(Props.create(new ActorCreator() {
                @Override
                public BlurActor create() {
                    return new BlurActor();
                }
            }), "actor/call_blur");
        }
        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());

        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(200)
                .build();
        setHierarchy(hierarchy);
    }
    public void bind(UserVM user) {
        bind(user.getAvatar().get());
    }

    public void bind(GroupVM group) {
        bind(group.getAvatar().get());
    }



    public void bind(Avatar avatar) {
        // Same avatar
        if (avatar != null && getImage(avatar) != null
                && getImage(avatar).getFileReference().getFileId() == currentId) {
            return;
        }

        fastThumbLoader = new FastThumbLoader(this);
        fastThumbLoader.setBlur(10);

        if (bindedFile != null) {
            bindedFile.detach();
            bindedFile = null;
        }

        setImageURI(null);

        if (avatar == null || getImage(avatar) == null) {
            currentId = 0;
            return;
        }
        currentId = getImage(avatar).getFileReference().getFileId();

        bindedFile = messenger().bindFile(getImage(avatar).getFileReference(), true, new FileVMCallback() {
            @Override
            public void onNotDownloaded() {

            }

            @Override
            public void onDownloading(float progress) {

            }

            @Override
            public void onDownloaded(FileSystemReference reference) {

                blurActor.send(new BlurActor.RequestBlur(reference.getDescriptor(), 10, new BlurActor.BluredListener() {
                    @Override
                    public void onBlured(final File f) {
                        ((BaseActivity)getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(f))
                                        .setResizeOptions(new ResizeOptions(Screen.getWidth(), Screen.getHeight()))
                                        .build();
                                PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                                        .setOldController(getController())
                                        .setImageRequest(request)
                                        .build();
                                setController(controller);
                            }
                        });

                    }
                }));
            }
        });
    }

    public AvatarImage getImage(Avatar avatar) {

        return avatar.getSmallImage();
    }

    public void bindRaw(String fileName) {
        if (bindedFile != null) {
            bindedFile.detach();
            bindedFile = null;
        }
        currentId = 0;

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(fileName)))
                .setAutoRotateEnabled(true)
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(getController())
                .setImageRequest(request)
                .build();
        setController(controller);
    }

    public void unbind() {
        if (bindedFile != null) {
            bindedFile.detach();
            bindedFile = null;
        }
        currentId = 0;

        setImageURI(null);
    }
}
