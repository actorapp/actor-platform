package im.actor.sdk.controllers.conversation.attach;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import im.actor.core.entity.Peer;
import im.actor.core.utils.GalleryScannerActor;
import im.actor.runtime.collections.ManagedList;
import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.tools.MediaPickerCallback;
import im.actor.sdk.controllers.tools.MediaPickerFragment;
import im.actor.sdk.util.SDKFeatures;
import im.actor.sdk.util.Screen;
import im.actor.sdk.view.ShareMenuButtonFactory;

import static im.actor.sdk.util.ActorSDKMessenger.messenger;

public class AttachFragment extends AbsAttachFragment implements MediaPickerCallback {

    private static final int PERMISSION_REQ_MEDIA = 11;

    private FrameLayout root;
    private View container;
    private FastAttachAdapter fastAttachAdapter;
    private ImageView menuIconToChange;
    private TextView menuTitleToChange;

    private boolean isLoaded = false;

    public AttachFragment(Peer peer) {
        super(peer);
    }

    public AttachFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup fcontainer, @Nullable Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .add(new MediaPickerFragment(), "picker")
                    .commitNow();
        }

        root = new FrameLayout(getContext());
        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        isLoaded = false;

        return root;
    }

    private void prepareView() {
        if (isLoaded) {
            return;
        }
        isLoaded = true;

        container = getLayoutInflater(null).inflate(R.layout.share_menu, root, false);
        container.setVisibility(View.INVISIBLE);

        container.findViewById(R.id.menu_bg).setBackgroundColor(style.getMainBackgroundColor());
        container.findViewById(R.id.cancelField).setOnClickListener(view -> hide());

        //
        // Building Menu Fields
        //
        ArrayList<ShareMenuField> menuFields = new ArrayList<>(onCreateFields());
        // Adding Additional Hide for better UI
        if (menuFields.size() % 2 != 0) {
            menuFields.add(new ShareMenuField(R.id.share_hide, R.drawable.attach_hide2, style.getBackyardBackgroundColor(), ""));
        }

        //
        // Building Layout
        //
        FrameLayout row = (FrameLayout) container.findViewById(R.id.share_row_one);
        boolean first = true;
        int menuItemSize = Screen.dp(80);
        int screenWidth =
                (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                        ? Screen.getWidth()
                        : Screen.getHeight());
        int distance = screenWidth / (menuFields.size() / 2 + menuFields.size() % 2);
        int initialMargin = distance / 2 - menuItemSize / 2;
        int marginFromStart = initialMargin;
        int secondRowTopMargin = Screen.dp(96);
        int shareIconSize = Screen.dp(60);
        View.OnClickListener defaultSendOcl = null;

        Configuration config = getResources().getConfiguration();
        boolean isRtl = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isRtl = config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        }

        for (int i = 0; i < menuFields.size(); i++) {
            ShareMenuField f = menuFields.get(i);

            LinearLayout shareItem = new LinearLayout(getActivity());
            shareItem.setOrientation(LinearLayout.VERTICAL);
            shareItem.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView title = new TextView(getActivity());
            title.setGravity(Gravity.CENTER);
            title.setTextColor(ActorSDK.sharedActor().style.getTextSecondaryColor());
            title.setText(f.getTitle());
            title.setTextSize(14);

            ImageView icon = new ImageView(getActivity());
            icon.setClickable(true);
            if (f.getSelector() != 0) {
                icon.setBackgroundResource(f.getSelector());
            } else {
                icon.setBackgroundDrawable(ShareMenuButtonFactory.get(f.getColor(), getActivity()));
                icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                icon.setImageResource(f.getIcon());
            }

            shareItem.addView(icon, shareIconSize, shareIconSize);
            shareItem.addView(title);

            View.OnClickListener l = v -> {
                hide();
                onItemClicked(v.getId());
            };
            icon.setId(f.getId());
            icon.setOnClickListener(l);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(menuItemSize, menuItemSize);
            params.setMargins(isRtl ? initialMargin : marginFromStart, first ? 0 : secondRowTopMargin, isRtl ? marginFromStart : initialMargin, 0);

            if (i == menuFields.size() - 1) {
                menuIconToChange = icon;
                menuTitleToChange = title;
                defaultSendOcl = l;

                params.setMargins(isRtl ? 0 : marginFromStart, first ? 0 : secondRowTopMargin, isRtl ? marginFromStart : 0, 0);

            }
            row.addView(shareItem, params);
            if (!first) {
                marginFromStart += distance;
            }
            first = !first;
        }

        menuIconToChange.setTag(R.id.icon, menuIconToChange.getDrawable());
        menuIconToChange.setTag(R.id.background, menuIconToChange.getBackground());
        menuTitleToChange.setTag(menuTitleToChange.getText().toString());

        View.OnClickListener shareSendOcl = v -> {
            Set<String> paths = fastAttachAdapter.getSelectedVM().get();
            if (paths.size() > 0) {
                List<Uri> uris = ManagedList.of(paths)
                        .map((x) -> Uri.fromFile(new File(x)));
                onUrisPicked(uris);
            }
            hide();
        };

        RecyclerView fastShare = (RecyclerView) container.findViewById(R.id.fast_share);
        fastAttachAdapter = new FastAttachAdapter(getActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        fastShare.setAdapter(fastAttachAdapter);
        fastShare.setLayoutManager(layoutManager);
        StateListDrawable background = ShareMenuButtonFactory.get(style.getMainColor(), getActivity());

        final View.OnClickListener finalDefaultSendOcl = defaultSendOcl;
        fastAttachAdapter.getSelectedVM().subscribe((val, valueModel) -> {
            if (val.size() > 0) {
                menuIconToChange.setBackgroundDrawable(background);
                menuIconToChange.setImageResource(R.drawable.conv_send);
                menuIconToChange.setColorFilter(0xffffffff, PorterDuff.Mode.SRC_IN);
                menuTitleToChange.setText(getString(R.string.chat_doc_send) + "(" + val.size() + ")");
                menuIconToChange.setOnClickListener(shareSendOcl);
                menuIconToChange.setPadding(Screen.dp(10), 0, Screen.dp(5), 0);
            } else {

                menuIconToChange.setBackgroundDrawable((Drawable) menuIconToChange.getTag(R.id.background));
                menuIconToChange.setImageDrawable((Drawable) menuIconToChange.getTag(R.id.icon));
                menuIconToChange.setColorFilter(null);
                menuIconToChange.setOnClickListener(finalDefaultSendOcl);
                menuTitleToChange.setText((String) menuTitleToChange.getTag());
                menuIconToChange.setPadding(0, 0, 0, 0);
            }
        });

        root.addView(container);
    }

    @Override
    public void show() {
        prepareView();
        if (container.getVisibility() == View.INVISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQ_MEDIA);
                    return;
                }
            }
            onShown();
            messenger().getGalleryScannerActor().send(new GalleryScannerActor.Show());
            showView(container);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                View internal = container.findViewById(R.id.menu_bg);
                int cx = internal.getWidth() - Screen.dp(56 + 56);
                int cy = internal.getHeight() - Screen.dp(56 / 2);
                float finalRadius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(internal, cx, cy, 0, finalRadius);
                anim.setDuration(200);
                anim.start();
                internal.setAlpha(1);
            }
        }
    }

    @Override
    public void hide() {
        if (container != null && container.getVisibility() == View.VISIBLE) {
            onHidden();
            fastAttachAdapter.clearSelected();
            messenger().getGalleryScannerActor().send(new GalleryScannerActor.Hide());
            hideView(container);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                View internal = container.findViewById(R.id.menu_bg);
                int cx = internal.getWidth() - Screen.dp(56 + 56);
                int cy = internal.getHeight() - Screen.dp(56 / 2);
                float finalRadius = (float) Math.hypot(cx, cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(internal, cx, cy, finalRadius, 0);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        internal.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        internal.setAlpha(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

                anim.setDuration(200);
                anim.start();
            }
        }
    }

    protected void onShown() {

    }

    protected void onHidden() {

    }

    protected List<ShareMenuField> onCreateFields() {
        ArrayList<ShareMenuField> res = new ArrayList<>();

        res.add(new ShareMenuField(R.id.share_camera, getString(R.string.share_menu_camera), R.drawable.share_camera_selector));
        res.add(new ShareMenuField(R.id.share_file, getString(R.string.share_menu_file), R.drawable.share_file_selector));
        res.add(new ShareMenuField(R.id.share_gallery, getString(R.string.share_menu_gallery), R.drawable.share_gallery_selector));
        if (SDKFeatures.isGoogleMapsSupported()) {
            res.add(new ShareMenuField(R.id.share_location, getString(R.string.share_menu_location), R.drawable.share_location_selector));
        }
        res.add(new ShareMenuField(R.id.share_video, getString(R.string.share_menu_video), R.drawable.share_video_selector));
        res.add(new ShareMenuField(R.id.share_contact, getString(R.string.share_menu_contact), R.drawable.share_contact_selector));

        return res;
    }

    protected void onItemClicked(int id) {
        MediaPickerFragment picker = (MediaPickerFragment) getChildFragmentManager().findFragmentByTag("picker");
        if (id == R.id.share_gallery) {
            picker.requestGallery();
        } else if (id == R.id.share_camera) {
            picker.requestPhoto();
        } else if (id == R.id.share_video) {
            picker.requestVideo();
        } else if (id == R.id.share_file) {
            picker.requestFile();
        } else if (id == R.id.share_location) {
            picker.requestLocation();
        } else if (id == R.id.share_contact) {
            picker.requestContact();
        }
    }

    @Override
    public void onUriPicked(Uri uri) {
        execute(messenger().sendUri(getPeer(), uri, ActorSDK.sharedActor().getAppName()));
    }

    protected void onUrisPicked(List<Uri> uris) {
        for (Uri s : uris) {
            execute(messenger().sendUri(getPeer(), s, ActorSDK.sharedActor().getAppName()));
        }
    }

    @Override
    public void onFilesPicked(List<String> paths) {
        for (String path : paths) {
            messenger().sendDocument(getPeer(), path);
        }
    }

    @Override
    public void onPhotoPicked(String path) {
        messenger().sendPhoto(getPeer(), path);
    }

    @Override
    public void onVideoPicked(String path) {
        messenger().sendVideo(getPeer(), path);
    }

    @Override
    public void onContactPicked(String name, List<String> phones, List<String> emails, byte[] avatar) {
        String avatar64 = null;
        if (avatar != null) {
            avatar64 = Base64.encodeToString(avatar, Base64.DEFAULT);
        }
        messenger().sendContact(getPeer(), name, new ArrayList<>(phones), new ArrayList<>(emails),
                avatar64);
    }

    @Override
    public void onLocationPicked(double longitude, double latitude, String street, String place) {
        messenger().sendLocation(getPeer(), longitude, latitude, street, place);
    }

    @Override
    public boolean onBackPressed() {
        if (container != null && container.getVisibility() == View.VISIBLE) {
            hide();
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fastAttachAdapter != null) {
            fastAttachAdapter.release();
            fastAttachAdapter = null;
        }
        container = null;
        root = null;
        menuIconToChange = null;
        menuTitleToChange = null;
    }
}
