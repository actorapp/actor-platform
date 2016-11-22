package im.actor.sdk.controllers.settings;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.BackgroundPreviewView;

public class WallpapersAdapter extends RecyclerView.Adapter<WallpapersAdapter.WallpaperHolder> {

    private int wallpaperSize = Screen.dp(85);
    private int padding;
    private ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(wallpaperSize, wallpaperSize);
    private int selected = 0;

    @Override
    public WallpaperHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BackgroundPreviewView itemView = new BackgroundPreviewView(parent.getContext());
        itemView.init(wallpaperSize, wallpaperSize, Screen.dp(2));
        itemView.setLayoutParams(params);
        padding = Screen.dp(8);

        ImageView selected = new ImageView(parent.getContext());
        selected.setImageResource(R.drawable.ic_done_white_36dp);
        selected.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int selectedPadding = Screen.dp(12);
        int selectedSize = Screen.dp(48);
        selected.setPadding(selectedPadding, selectedPadding, selectedPadding, selectedPadding);
        selected.setBackgroundResource(R.drawable.avatar_background);

        FrameLayout fl = new FrameLayout(parent.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(wallpaperSize, wallpaperSize, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        params.setMargins(0, 0, Screen.dp(8), 0);
        fl.addView(itemView, params);
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(selectedSize, selectedSize, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        params1.setMargins(Screen.dp(18.5f), 0, 0, 0);
        fl.addView(selected, params1);
        return new WallpaperHolder(fl, itemView, selected);
    }

    @Override
    public void onBindViewHolder(WallpaperHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return ActorSDK.sharedActor().style.getDefaultBackgrouds().length;
    }

    public class WallpaperHolder extends RecyclerView.ViewHolder {
        private BackgroundPreviewView view;
        private int i;
        private View selectedView;

        public WallpaperHolder(FrameLayout container, BackgroundPreviewView itemView, ImageView selectedView) {
            super(container);
            view = itemView;
            this.selectedView = selectedView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PickWallpaperActivity.class);
                    intent.putExtra("EXTRA_ID", i);
                    v.getContext().startActivity(intent);
                }
            });
        }

        public void bind(int i) {
            this.i = i;
            view.bind(i);
            if (selected == i) {
                selectedView.setVisibility(View.VISIBLE);
            } else {
                selectedView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void setSelected(int selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }
}
