package im.actor.sdk.controllers.conversation.attach;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import im.actor.runtime.mvvm.ValueModel;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.ActorBinder;
import im.actor.sdk.util.Screen;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class FastAttachAdapter extends RecyclerView.Adapter<FastAttachAdapter.FastShareVH> {

    private ArrayList<String> imagesPath = new ArrayList<>();
    private Set<String> selected = new HashSet<>();
    private Context context;
    private ValueModel<Set<String>> selectedVM;

    private ActorBinder binder;
    private WidthGetter widthGetter;

    public FastAttachAdapter(Context context, WidthGetter widthGetter) {
        this.widthGetter = widthGetter;
        this.context = context;
//        setHasStableIds(true);
        binder = new ActorBinder();
        binder.bind(messenger().getGalleryVM().getGalleryMediaPath(), (val, valueModel) -> {
            imagesPath.clear();
            imagesPath.addAll(val);
            notifyDataSetChanged();
        });
        selectedVM = new ValueModel<>("fast_share.selected", new HashSet<>());
    }


    protected View inflate(int id, ViewGroup viewGroup) {
        return LayoutInflater
                .from(context)
                .inflate(id, viewGroup, false);
    }


    public void release() {
        binder.unbindAll();
    }

    @Override
    public FastShareVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflate(R.layout.share_menu_fast_share, parent);
        itemView.setLayoutParams(new ViewGroup.LayoutParams(widthGetter.get(), widthGetter.get()));
        return new FastShareVH(itemView);
    }

    @Override
    public void onBindViewHolder(FastShareVH holder, int position) {

        holder.bind(imagesPath.get(position));
    }

    @Override
    public int getItemCount() {
        return imagesPath.size();
    }

    public class FastShareVH extends RecyclerView.ViewHolder {
        private SimpleDraweeView v;
        private CompoundButton chb;
        private String data;

        public FastShareVH(View itemView) {
            super(itemView);
            itemView.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
            v = (SimpleDraweeView) itemView.findViewById(R.id.image);
            chb = (CheckBox) itemView.findViewById(R.id.check);
            int size = Screen.dp(80);
            v.setLayoutParams(new FrameLayout.LayoutParams(size, size));
            chb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && data != null) {
                    selected.add(data);
                    notifyVm();
                } else {
                    selected.remove(data);
                    notifyVm();

                }
            });
        }

        public void bind(String path) {
            data = path;
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(path)))
                    .setResizeOptions(new ResizeOptions(v.getLayoutParams().width,
                            v.getLayoutParams().height))
                    .build();
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                    .setOldController(v.getController())
                    .setImageRequest(request)
                    .build();
            v.setController(controller);
            chb.setChecked(selected.contains(data));
        }
    }

    public void notifyVm() {
        selectedVM.change(new HashSet<String>(selected));
    }

    public void clearSelected() {
        selected.clear();
        notifyVm();
        notifyDataSetChanged();
    }

    public ValueModel<Set<String>> getSelectedVM() {
        return selectedVM;
    }

    public interface WidthGetter {
        int get();
    }
}
