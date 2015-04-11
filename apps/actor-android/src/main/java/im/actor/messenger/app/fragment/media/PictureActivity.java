package im.actor.messenger.app.fragment.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.droidkit.progress.CircularView;

import im.actor.images.cache.BitmapReference;
import im.actor.images.loading.ReceiverCallback;
import im.actor.images.loading.tasks.RawFileTask;
import im.actor.images.loading.view.ImageKitView;
import im.actor.messenger.R;
import im.actor.messenger.app.Intents;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.MaterialInterpolator;
import im.actor.model.files.FileSystemReference;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.viewmodel.DownloadCallback;
import im.actor.model.viewmodel.UserVM;
import uk.co.senab.photoview.PhotoViewAttacher;

import static im.actor.messenger.app.Core.getImageLoader;
import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.Core.users;


public class PictureActivity extends ActionBarActivity {

    private static final String ARG_FILE_PATH = "arg_file_path";
    private static final String ARG_FILE_ID = "arg_file_id";
    private static final String ARG_OWNER = "arg_owner";
    private static final String ARG_TIMER = "arg_timer";
    private static final String ARG_IMAGE_TOP = "arg_image_top";
    private static final String ARG_IMAGE_LEFT = "arg_image_left";
    private static final String ARG_IMAGE_WIDTH = "arg_image_width";
    private static final String ARG_IMAGE_HEIGHT = "arg_image_height";
    private static int animationMultiplier = 1;
    private ImageKitView transitionView;
    private Bitmap bitmap;
    private int transitionTop;
    private int transitionLeft;
    private int transitionWidth;
    private int transitionHeight;
    private View backgroundView;
    private PictureFragment fragment;
    private String path;
    private boolean uiIsHidden;
    private Toolbar toolbar;

    public static void launchPhoto(Activity activity, View transitionView, String path, int senderId) {

        Intent intent = new Intent(activity, PictureActivity.class);
        intent.putExtra(ARG_FILE_PATH, path);
        intent.putExtra(ARG_OWNER, senderId);

        int[] location = new int[2];
        transitionView.getLocationInWindow(location);
        intent.putExtra(ARG_IMAGE_TOP, location[1]);
        intent.putExtra(ARG_IMAGE_LEFT, location[0]);
        intent.putExtra(ARG_IMAGE_WIDTH, transitionView.getWidth());
        intent.putExtra(ARG_IMAGE_HEIGHT, transitionView.getHeight());


        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        getSupportActionBar().setTitle(R.string.media_picture);

        int statbarHeight = Screen.getStatusBarHeight();
        if (Build.VERSION.SDK_INT >= 19) {
            toolbar.setPadding(0, statbarHeight, 0, 0);
        }

        final Bundle bundle = getIntent().getExtras();
        path = bundle.getString(ARG_FILE_PATH);
        int sender = bundle.getInt(ARG_OWNER, 0);

        toolbar.setVisibility(View.GONE);

        transitionTop = bundle.getInt(ARG_IMAGE_TOP, 0);
        transitionLeft = bundle.getInt(ARG_IMAGE_LEFT, 0);
        transitionWidth = bundle.getInt(ARG_IMAGE_WIDTH, 0);
        transitionHeight = bundle.getInt(ARG_IMAGE_HEIGHT, 0);

        transitionView = (ImageKitView) findViewById(R.id.transition);
        backgroundView = findViewById(R.id.background);

        transitionView.setExtraReceiverCallback(new ReceiverCallback() {
            @Override
            public void onImageLoaded(BitmapReference bitmapRef) {
                bitmap = bitmapRef.getBitmap();
                MediaActivity.MediaFullscreenAnimationUtils.animateForward(transitionView, bitmap, transitionLeft, transitionTop, transitionWidth, transitionHeight,
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                fragment = new PictureFragment();
                                fragment.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction()
                                        .add(R.id.container, fragment)
                                        .commit();

                                transitionView.setExtraReceiverCallback(null);
                                transitionView.clear();
                                transitionView.setAlpha(0f);
                                transitionView.setVisibility(View.GONE);
                            }
                        });
                MediaActivity.MediaFullscreenAnimationUtils.animateBackgroundForward(backgroundView);


            }

            @Override
            public void onImageCleared() {

            }

            @Override
            public void onImageError() {

            }
        });
        transitionView.request(new RawFileTask(path));
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        getSupportFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
        transitionView.request(new RawFileTask(path));
        transitionView.setAlpha(1f);
        transitionView.setVisibility(View.VISIBLE);
        MediaActivity.MediaFullscreenAnimationUtils.animateBack(transitionView, bitmap, transitionLeft, transitionTop, transitionWidth, transitionHeight,
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        superFinish();
                    }
                });
        MediaActivity.MediaFullscreenAnimationUtils.animateBackgroundBack(backgroundView);
    }

    private void superFinish() {
        super.finish();
        overridePendingTransition(0, 0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class PictureFragment extends Fragment {

        private ImageKitView imageView;
        private boolean uiIsHidden = true;
        private AvatarView ownerAvatarView;
        private TextView ownerNameView;
        private View ownerContainer;
        private Toolbar toolbar;
        private boolean firstShowing = true;
        private PhotoViewAttacher attacher;
        private String path;
        private long fileId;
        private CircularView circularView;
        private View backgroundView;

        public PictureFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_media_picture, container, false);

            final Bundle bundle = getArguments();
            path = bundle.getString(ARG_FILE_PATH);
            fileId = bundle.getLong(ARG_FILE_ID);
            int sender = bundle.getInt(ARG_OWNER, 0);
            circularView = (CircularView) rootView.findViewById(R.id.progress);
            circularView.setValue(50);
            imageView = (ImageKitView) rootView.findViewById(R.id.image);
            imageView.setExtraReceiverCallback(new ReceiverCallback() {
                @Override
                public void onImageLoaded(BitmapReference bitmap) {
                    attacher = new PhotoViewAttacher(imageView);
                    attacher.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {
                            if (!uiIsHidden) {
                                hideSystemUi();
                            } else {
                                showSystemUi();
                            }
                            return false;
                        }

                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            if (!uiIsHidden)
                                hideSystemUi();
                            return true;
                        }

                        @Override
                        public boolean onDoubleTapEvent(MotionEvent e) {
                            return true;
                        }
                    });
                }

                @Override
                public void onImageCleared() {

                }

                @Override
                public void onImageError() {

                }
            });
            if(path==null){
                messenger().requestState( fileId, new DownloadCallback() {
                    @Override
                    public void onNotDownloaded() {
                        //messenger().startDownloading(location);
                        Toast.makeText(getActivity(), "File is not loaded :O", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDownloading(float progress) {

                    }

                    @Override
                    public void onDownloaded(final FileSystemReference reference) {
                        MVVMEngine.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                path = reference.getDescriptor();
                                imageView.request(new RawFileTask(path));
                            }
                        });
                    }
                });
            } else {
                imageView.request(new RawFileTask(path));
            }

            ownerAvatarView = (AvatarView) rootView.findViewById(R.id.avatar);
            ownerNameView = (TextView) rootView.findViewById(R.id.name);
            ownerContainer = rootView.findViewById(R.id.ownerContainer);

            if (Build.VERSION.SDK_INT >= 19) {
                ownerContainer.setPadding(0, 0, 0, Screen.getNavbarHeight());
            }

            UserVM owner = users().get(sender);

            ownerAvatarView.init(Screen.dp(48), 18);
            ownerAvatarView.bind(owner);
            /*ownerAvatarView.setEmptyDrawable(AvatarDrawable.create(owner, 16, getActivity()));
            Avatar avatar = owner.getAvatar().getValue();
            if (avatar != null) {
                ownerAvatarView.bindAvatar(32, avatar);
            } else {
                ownerAvatarView.unbind();
            }*/
            ownerNameView.setText(owner.getName().get());

            backgroundView = null;

            Activity activity = getActivity();
            if (activity instanceof PictureActivity) {
                backgroundView = ((PictureActivity) activity).backgroundView;
            } else {
                if (activity instanceof MediaActivity) {
                    backgroundView = ((MediaActivity) activity).transitionBackgroundView;
                }
            }
            if (backgroundView != null)
                backgroundView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!uiIsHidden) {
                            hideSystemUi();
                        } else {
                            showSystemUi();
                        }
                    }
                });

            ownerContainer.setVisibility(View.GONE);

            return rootView;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            attacher.cleanup();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.media_picture, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.share) {
                startActivity(Intents.shareDoc("picture.jpeg", path));
            /*startActivity(new Intent(Intent.ACTION_SEND)
                    .setType("image/jpeg")
                    .putExtra(Intent.EXTRA_STREAM,Uri.parse(path)));*/
                return true;
            } else if (item.getItemId() == R.id.save) {
                getImageLoader().createReceiver(new ReceiverCallback() {
                    @Override
                    public void onImageLoaded(BitmapReference bitmap) {
                        Intents.savePicture(getActivity(), bitmap.getBitmap());
                    }

                    @Override
                    public void onImageCleared() {

                    }

                    @Override
                    public void onImageError() {

                    }
                }).request(new RawFileTask(path));

                item.setEnabled(false);
                item.setTitle(R.string.menu_saved);

                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void showSystemUi() {
            toolbar.setVisibility(View.VISIBLE);
            ownerContainer.setVisibility(View.VISIBLE);

            uiIsHidden = false;
            syncUiState();
        }

        private void hideSystemUi() {
            uiIsHidden = true;
            syncUiState();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (activity instanceof PictureActivity) {
                toolbar = ((PictureActivity) activity).toolbar;
            } else {
                if (activity instanceof MediaActivity) {
                    toolbar = ((MediaActivity) activity).toolbar;
                }
            }
        }

        private void syncUiState() {

            toolbar.clearAnimation();
            ownerContainer.clearAnimation();
            if (uiIsHidden) {


                toolbar.animate()
                        .setInterpolator(new MaterialInterpolator())
                        .y(-toolbar.getHeight())
                        .alpha(0)
                        .setStartDelay(0)
                        .setDuration(300 * animationMultiplier)
                        .start();
                ownerContainer.animate()
                        .setInterpolator(new MaterialInterpolator())
                        .alpha(0)
                        .setStartDelay(0)
                        .setDuration(300 * animationMultiplier)
                        .start();
            } else {
                if (firstShowing) {
                    firstShowing = false;
                    // костыль
                    toolbar.setAlpha(0);
                    toolbar.setTop(-toolbar.getHeight());
                    ownerContainer.setAlpha(0);
                    toolbar.post(new Runnable() {
                        @Override
                        public void run() {
                            toolbar.animate()
                                    .setInterpolator(new MaterialInterpolator())
                                    .y(0)
                                    .alpha(1)
                                    .setStartDelay(50)
                                    .setDuration(450 * animationMultiplier)
                                    .start();
                            ownerContainer.animate()
                                    .setInterpolator(new MaterialInterpolator())
                                    .alpha(1)
                                    .setStartDelay(50)
                                    .setDuration(450 * animationMultiplier)
                                    .start();
                        }
                    });
                    return;
                }
                toolbar.animate()
                        .setInterpolator(new MaterialInterpolator())
                        .y(0)
                        .alpha(1)
                        .setStartDelay(120)
                        .setDuration(420 * animationMultiplier)
                        .start();
                ownerContainer.animate()
                        .setInterpolator(new MaterialInterpolator())
                        .alpha(1)
                        .setStartDelay(120)
                        .setDuration(420 * animationMultiplier)
                        .start();
            }

        }

        public static Fragment getInstance(String path, int senderId) {

            Bundle bundle = new Bundle();
            bundle.putString(ARG_FILE_PATH, path);
            bundle.putInt(ARG_OWNER, senderId);
            Fragment fragment = new PictureFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        public static Fragment getInstance(long fileId, int senderId) {
            Bundle bundle = new Bundle();
            bundle.putLong(ARG_FILE_ID, fileId);
            bundle.putInt(ARG_OWNER, senderId);
            Fragment fragment = new PictureFragment();
            fragment.setArguments(bundle);
            return fragment;
        }
    }
}