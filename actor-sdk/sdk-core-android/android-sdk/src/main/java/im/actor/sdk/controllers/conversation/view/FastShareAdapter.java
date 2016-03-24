package im.actor.sdk.controllers.conversation.view;

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

import im.actor.runtime.mvvm.Value;
import im.actor.runtime.mvvm.ValueChangedListener;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class FastShareAdapter extends RecyclerView.Adapter<FastShareAdapter.FastShareVH> {

    ArrayList<String> imagesPath = new ArrayList<>();
    Set<String> selected = new HashSet<>();
    Context context;

    public FastShareAdapter(Context context) {
        this.context = context;
        messenger().getGalleryVM().getGalleryMediaPath().subscribe(new ValueChangedListener<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> val, Value<ArrayList<String>> valueModel) {
                imagesPath.clear();
                imagesPath.addAll(val);
                notifyDataSetChanged();
            }
        });
    }

    protected View inflate(int id, ViewGroup viewGroup) {
        return LayoutInflater
                .from(context)
                .inflate(id, viewGroup, false);
    }


    @Override
    public FastShareVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FastShareVH(inflate(R.layout.share_menu_fast_share, parent));
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
        SimpleDraweeView v;
        CheckBox chb;
        String data;

        public FastShareVH(View itemView) {
            super(itemView);
            v = (SimpleDraweeView) itemView.findViewById(R.id.image);
            chb = (CheckBox) itemView.findViewById(R.id.check);
            int size = context.getResources().getDimensionPixelSize(R.dimen.share_btn_size);
            v.setLayoutParams(new FrameLayout.LayoutParams(size, size));
            chb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && data != null) {
                        selected.add(data);
                    } else {
                        selected.remove(data);
                    }
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
}
