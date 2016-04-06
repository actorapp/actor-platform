package im.actor.sdk.controllers.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.ActorStyle;
import im.actor.sdk.R;
import im.actor.sdk.controllers.conversation.view.ChatBackgroundView;
import im.actor.sdk.controllers.fragment.BaseFragment;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.BackgroundPreviewView;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class PickWallpaperFragment extends BaseFragment {

    private ChatBackgroundView wallpaper;
    SharedPreferences shp;
    SharedPreferences.Editor ed;
    int selectedWallpaper = 0;

    public static PickWallpaperFragment chooseWallpaper(int id) {
        Bundle args = new Bundle();
        args.putInt("EXTRA_ID", id);
        PickWallpaperFragment res = new PickWallpaperFragment();
        res.setArguments(args);
        return res;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ActorStyle style = ActorSDK.sharedActor().style;
        shp = getActivity().getSharedPreferences("wallpaper", Context.MODE_PRIVATE);
        ed = shp.edit();
        selectedWallpaper = getArguments().getInt("EXTRA_ID");
        if (selectedWallpaper == -1) {
            selectedWallpaper = BackgroundPreviewView.getBackgroundIdByUri(messenger().getSelectedWallpaper(), getContext(), shp.getInt("wallpaper", 0));
        }
        View res = inflater.inflate(R.layout.fragment_pick_wallpaper, container, false);

        res.setBackgroundColor(ActorSDK.sharedActor().style.getMainBackgroundColor());
        ((TextView) res.findViewById(R.id.cancel)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());
        ((TextView) res.findViewById(R.id.ok)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryColor());

        res.findViewById(R.id.dividerTop).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());
        res.findViewById(R.id.dividerBot).setBackgroundColor(ActorSDK.sharedActor().style.getDividerColor());

        res.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        res.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messenger().changeSelectedWallpaper("local:".concat(getResources().getResourceEntryName(BackgroundPreviewView.getBackground(selectedWallpaper))));
                ed.putInt("wallpaper", selectedWallpaper);
                ed.commit();
                getActivity().finish();
            }
        });

        wallpaper = (ChatBackgroundView) res.findViewById(R.id.wallpaper);
        wallpaper.bind(selectedWallpaper);

        LinearLayout botContainer = (LinearLayout) res.findViewById(R.id.wallpaper_preview_container);
        LinearLayout wallpaperContainer = (LinearLayout) res.findViewById(R.id.background_container);
        botContainer.setBackgroundColor(style.getMainBackgroundColor());
        View.OnClickListener ocl = new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                selectedWallpaper = (int) v.getTag();
                wallpaper.bind(selectedWallpaper);
            }
        };
        int previewSize = 90;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Screen.dp(previewSize), Screen.dp(previewSize));
        for (int i = 0; i < BackgroundPreviewView.getSize(); i++) {
            FrameLayout frame = new FrameLayout(getActivity());
            BackgroundPreviewView bckgrnd = new BackgroundPreviewView(getActivity());
            bckgrnd.init(Screen.dp(previewSize), Screen.dp(previewSize));
            bckgrnd.bind(i);
            //bckgrnd.setPadding(Screen.dp(5), Screen.dp(10), Screen.dp(5), Screen.dp(20));
            frame.setTag(i);
            frame.setOnClickListener(ocl);
            frame.addView(bckgrnd);
            wallpaperContainer.addView(frame, params);

        }

        return res;
    }

}
