package im.actor.messenger.app.fragment.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import im.actor.images.cache.BitmapReference;
import im.actor.images.loading.ReceiverCallback;
import im.actor.images.loading.tasks.RawFileTask;
import im.actor.images.loading.view.ImageKitView;
import im.actor.messenger.R;
import im.actor.messenger.app.base.BaseActivity;
import im.actor.messenger.app.util.Logger;
import im.actor.messenger.app.util.Screen;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.MaterialInterpolator;
import im.actor.model.entity.FileReference;
import im.actor.model.entity.Message;
import im.actor.model.entity.Peer;
import im.actor.model.entity.content.DocumentContent;
import im.actor.model.entity.content.FileLocalSource;
import im.actor.model.entity.content.FileRemoteSource;
import im.actor.model.files.FileSystemReference;
import im.actor.model.log.Log;
import im.actor.model.mvvm.BindedDisplayList;
import im.actor.model.mvvm.MVVMEngine;
import im.actor.model.viewmodel.FileCallback;

import static im.actor.messenger.app.Core.messenger;
import static im.actor.messenger.app.view.ViewUtils.goneView;
import static im.actor.messenger.app.view.ViewUtils.showView;

/**
 * Created by Jesus Christ. Amen.
 */
public class MediaActivity extends BaseActivity {
    private static final String EXTRA_PEER_UNIQ_ID = "arg_peer_uniq_id";
    private static final String TAG = "MediaActivity";

    public Toolbar toolbar;
    private ActionBar actionBar;


    private boolean uiIsHidden = false;


    //region Arguments
    private static final String ARG_PATH = "arg_absolute_path";
    private static final String ARG_OWNER = "arg_owner";
    private static final String ARG_TIMER = "arg_timer";
    private static final String ARG_IMAGE_TOP = "arg_image_top";
    private static final String ARG_IMAGE_LEFT = "arg_image_left";
    private static final String ARG_IMAGE_WIDTH = "arg_image_width";
    private static final String ARG_IMAGE_HEIGHT = "arg_image_height";
    private static final String ARG_FILEID = "arg_fileid";
    private static final String ARG_FILENAME = "arg_filename";
    private static final String ARG_FILESIZE = "arg_filesize";
    //endregion

    //region Image animation
    protected View transitionBackgroundView;
    private ImageKitView transitionImageView;
    //endregion


    //region grid
    private static final java.lang.String ARG_VIEW_TYPE = "ARG_VIEW_TYPE";
    private static final int VIEW_TYPE_GRID = 1;
    private static final int VIEW_TYPE_PICTURE = 2;
    private static final String ARG_CHAT_TYPE = "ARG_CHAT_TYPE";
    private static final String ARG_CHAT_ID = "ARG_CHAT_ID";


    private int chatType;
    private int chatId;
    // private ListEngine<Message> engine;
    //private EngineUiList<Message> mediaEngineList;
    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private View bbemptyView;
    private boolean isInit = true;
    private Intent activityIntent;
    private View emptyView;
    //endregion


    private AvatarView ownerAvatarView;
    private TextView ownerNameView;

    //region pager
    private boolean showingPager = false;
    private ViewPager viewPager;
    private int selectedIndex;
    private Peer peer;
    private BindedDisplayList<Message> displayList;
    private MediaPagerAdapter pagerAdapter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_media);
        activityIntent = getIntent();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowCustomEnabled(false);
        getSupportActionBar().setTitle(R.string.media);

        int statbarHeight = Screen.getStatusBarHeight();
        if (Build.VERSION.SDK_INT >= 19) {
            toolbar.setPadding(0, statbarHeight, 0, 0);
        }


        transitionBackgroundView = findViewById(R.id.fullscreenBackground);
        transitionImageView = (ImageKitView) findViewById(R.id.image);
        ownerAvatarView = (AvatarView) findViewById(R.id.avatar);
        ownerNameView = (TextView) findViewById(R.id.name);
        recyclerView = (RecyclerView) findViewById(R.id.mediaList);
        emptyView = findViewById(R.id.noMedia);
        viewPager = (ViewPager) findViewById(R.id.pager);

        showGrid();

    }


    /**
     * private void showPicture() {
     * <p/>
     * <p/>
     * Bundle bundle = activityIntent.getExtras();
     * String path = bundle.getString(ARG_PATH);
     * fileId = bundle.getLong(ARG_FILEID, 0);
     * fileName = bundle.getString(ARG_FILENAME);
     * fileSize = bundle.getInt(ARG_FILESIZE, 0);
     * int sender = bundle.getInt(ARG_OWNER, 0);
     * final UserVM owner = users().get(sender);
     * <p/>
     * showingPager = true;
     * viewPager.setAlpha(1);
     * showView(viewPager, false);
     * toolbar.setTitle(R.string.media);
     * viewPager.setAdapter(new SingleMediaFakePagerAdapter(path, owner));
     * //viewPager.setCurrentItem(selectedIndex, false);
     * <p/>
     * <p/>
     * //showPager();
     * <p/>
     * ownerContainer.setAlpha(1);
     * transitionImageView.setAlpha(1L);
     * showView(ownerContainer, false);
     * showView(transitionImageView, false);
     * showView(ownerContainer, false);
     * <p/>
     * setPictureActionbar();
     * <p/>
     * <p/>
     * actionBar.setTitle("Picture");
     * <p/>
     * <p/>
     * <p/>
     * final int navbarHeight = Screen.getNavbarHeight();
     * if(Build.VERSION.SDK_INT>=19) {
     * if (navbarHeight > 0) {
     * // ownerContainer.setPadding(0, 0, 0, navbarHeight);
     * }
     * }
     * <p/>
     * <p/>
     * transitionTop = bundle.getInt(ARG_IMAGE_TOP, 0);
     * transitionLeft = bundle.getInt(ARG_IMAGE_LEFT, 0);
     * transitionWidth = bundle.getInt(ARG_IMAGE_WIDTH, 0);
     * transitionHeight = bundle.getInt(ARG_IMAGE_HEIGHT, 0);
     * // ViewCompat.setTransitionName(imageView, TRANSIT_IMAGE);
     * // transitionBackgroundView.setAlpha(0);
     * <p/>
     * <p/>
     * <p/>
     * // animateImage(transitionTop, transitionLeft, transitionHeight, transitionWidth);
     * <p/>
     * Move to fragment?
     * ownerAvatarView.setEmptyDrawable(AvatarDrawable.create(owner, 16, this));
     * Avatar avatar = owner.getAvatar().getValue();
     * if (avatar != null) {
     * ownerAvatarView.bindAvatar(32, avatar);
     * } else {
     * ownerAvatarView.unbind();
     * }
     * ownerNameView.setText(owner.getName());
     * <p/>
     * ownerContainer.setOnClickListener(new View.OnClickListener() {
     *
     * @Override public void onClick(View v) {
     * startActivity(Intents.openProfile(owner.getId(), MediaActivity.this));
     * }
     * });
     * <p/>
     * //decorView.setOnSystemUiVisibilityChangeListener(this);
     * transitionImageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
     * @Override public void onViewTap(View view, float x, float y) {
     * }
     * });
     * transitionImageView.setOnDoubleClick(new GestureDetector.OnDoubleTapListener() {
     * @Override public boolean onSingleTapConfirmed(MotionEvent e) {
     * if (!uiIsHidden) {
     * hideSystemUi();
     * } else {
     * showSystemUi();
     * }
     * return false;
     * }
     * @Override public boolean onDoubleTap(MotionEvent e) {
     * if (!uiIsHidden)
     * hideSystemUi();
     * return true;
     * }
     * @Override public boolean onDoubleTapEvent(MotionEvent e) {
     * return true;
     * }
     * });
     * transitionBackgroundView.setOnClickListener(new View.OnClickListener() {
     * @Override public void onClick(View v) {
     * if (!uiIsHidden) {
     * hideSystemUi();
     * } else {
     * showSystemUi();
     * }
     * }
     * });
     * <p/>
     * }
     */


    private void setPictureActionbar() {
        setTitle(R.string.media_picture);
        invalidateOptionsMenu();
    }

    private void showGrid() {

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        emptyView.setAlpha(1);
        recyclerView.setAlpha(1);
        goneView(emptyView, false);
        showView(recyclerView, false);

        chatType = activityIntent.getIntExtra(ARG_CHAT_TYPE, 0);
        chatId = activityIntent.getIntExtra(ARG_CHAT_ID, 0);
        peer = Peer.fromUniqueId(activityIntent.getLongExtra(EXTRA_PEER_UNIQ_ID, 0));
        setGridActionbar();


        emptyView.setVisibility(View.GONE);
        displayList = messenger().getMediaGlobalList(peer);

        adapter = new MediaAdapter(displayList, new OnMediaClickListener() {


            @Override
            public void onClick(final MediaAdapter.MediaHolder holder, Message item) {
                final TransitionAnimation animation = new TransitionAnimation() {
                    @Override
                    public Runnable runnable(final View view, final Bitmap bitmap) {
                        return new Runnable() {
                            @Override
                            public void run() {
                                int[] location = new int[2];
                                transitionImageView.setExtraReceiverCallback(null);
                                if(!skipAnimation) {
                                    view.getLocationInWindow(location);
                                    MediaFullscreenAnimationUtils.animateForward(transitionImageView, bitmap, location[0], location[1], view.getWidth(), view.getHeight(),
                                            new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    showPager();
                                                }
                                            });
                                    transitionBackgroundView.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            MediaFullscreenAnimationUtils.animateBackgroundForward(transitionBackgroundView, null);
                                        }
                                    }, 50);
                                } else {
                                    showPager();
                                }
                            }
                        };
                    }
                };
                viewPager.setAdapter(pagerAdapter);
                viewPager.setVisibility(View.VISIBLE);
                viewPager.setAlpha(0);
                transitionBackgroundView.setVisibility(View.VISIBLE);
                transitionBackgroundView.setAlpha(0);
                final View view = holder.itemView;
                //transitionImageView.setVisibility(View.VISIBLE);
                transitionImageView.setExtraReceiverCallback(new ReceiverCallback() {
                    @Override
                    public void onImageLoaded(BitmapReference bitmapRef) {
                        Bitmap bitmap = bitmapRef.getBitmap();
                        //bitmap = fastBlur(bitmap, 5);

                        selectedIndex = holder.getPosition();
                        holder.itemView.post(animation.runnable(holder.itemView, bitmap));
                    }

                    @Override
                    public void onImageCleared() {

                    }

                    @Override
                    public void onImageError() {

                    }
                });

                final DocumentContent document = (DocumentContent) item.getContent();
                if (document.getSource() instanceof FileRemoteSource) {
                    FileRemoteSource remoteSource = (FileRemoteSource) document.getSource();
                    final FileReference location = remoteSource.getFileReference();
                    messenger().requestState(location.getFileId(), new FileCallback() {
                        @Override
                        public void onNotDownloaded() {
                            selectedIndex = holder.getPosition();
                            animation.skipAnimation();
                            animation.runnable(null, null).run();
                        }

                        @Override
                        public void onDownloading(float progress) {
                        }

                        @Override
                        public void onDownloaded(final FileSystemReference reference) {
                            MVVMEngine.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    transitionImageView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            transitionImageView.request(new RawFileTask(reference.getDescriptor()));
                                        }

                                    });
                                }

                            });
                        }
                    });
                    Logger.d(TAG, "Remote =(");
                    // todo not loaded?
                } else if (document.getSource() instanceof FileLocalSource) {
                    final String path = ((FileLocalSource) document.getSource()).getFileDescriptor();


                }


                // todo transformation from blur?
                /*
                hideListView();
                new MediaDialog.Builder(MediaActivity.this)
                        .setUsers(groups().get(chatId).getRaw().getMembers())
                        .setMedia(mediaEngineList)
                        .setSelected(position)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                showListView();
                            }
                        })
                        .show();
                
                // Downloaded d = downloaded().get(doc.getFileLocation().getFileId());

                if (d != null) {
                // String fileName = d.fileName;

                startActivity(Intents.openDoc(d));*/
            }
        }, this);
        pagerAdapter = new MediaPagerAdapter(displayList, this);
        // View footer = inflater.inflate(R.layout.adapter_doc_footer, recyclerView, false);
        // recyclerView.addFooterView(footer, null, false);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                int toolbarHeight = toolbar.getHeight();
                recyclerView.setPadding(0, toolbarHeight, 0, Screen.getNavbarHeight());
            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.gallery_items_count)));
        recyclerView.setAdapter(adapter);
        /*adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

            }
        });*/
        /*viewPager.setAdapter(new MediaPagerAdapter(mediaEngineList, groups().get(chatId).getRaw().getMembers()));
        */

        /*isInit = true;
        getBinder().bind(mediaEngineList.getListState(), new Listener<ListState>() {
            @Override
            public void onUpdated(ListState listState) {
                switch (listState.getState()) {
                    case LOADING_EMPTY:
                        break;
                    case LOADED_EMPTY:
                        showView(emptyView, !isInit, false);
                        goneView(recyclerView, !isInit, false);
                        break;
                    case LOADED:
                    default:
                        goneView(emptyView, !isInit, false);
                        showView(recyclerView, !isInit, false);
                        break;
                }
            }
        });
        isInit = false;
            }
        });*/
    }

    private void setGridActionbar() {
        toolbar.setTitle(R.string.profile_shared_media);
        invalidateOptionsMenu();
    }

    private void showListView() {
        /*if(recyclerView.getVisibility()!=View.VISIBLE) {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.clearAnimation();
            recyclerView.animate().alpha(1).scaleX(1).scaleY(1).setDuration(300).setListener(null).start();
        }*/
    }

    private void hideListView() {
        /*if(recyclerView.getVisibility()!=View.GONE) {
            recyclerView.clearAnimation();
            recyclerView.animate().alpha(0).scaleX(0.75f).scaleY(0.75f).setDuration(300)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            recyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .start();
        }*/
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if (mediaEngineList != null) {
            mediaEngineList.release();
            mediaEngineList = null;
        }
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        recyclerView = null;
        emptyView = null;*/
    }

    public static Intent openGrid(Context context, int chatType, int chatId) {
        Intent intent = new Intent(context, MediaActivity.class);
        intent.putExtra(ARG_CHAT_TYPE, chatType);
        intent.putExtra(ARG_CHAT_ID, chatId);
        intent.putExtra(ARG_VIEW_TYPE, VIEW_TYPE_GRID);
        return intent;
    }

    private void showSystemUi() {
        uiIsHidden = false;
        syncUiState();
    }

    private void hideSystemUi() {
        uiIsHidden = true;
        syncUiState();
    }

    private int getAbarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }


    private void syncUiState() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return !showingPager;
    }

    /*@Override


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (i == R.id.share) {
            String path = downloaded().get(adapter.getItem(viewPager.getCurrentItem()).id).getDownloadedPath();

            startActivity(new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.parse(path), "image/jpeg"));
            return true;
        } else if(i == R.id.save){
            String path = downloaded().get(adapter.getItem(viewPager.getCurrentItem()).id).getDownloadedPath();
            core().getImageLoader().createReceiver(new ReceiverCallback() {
                @Override
                public void onImageLoaded(BitmapReference bitmap) {
                    Intents.savePicture(getBaseContext(), bitmap.getBitmap());
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


    @Override
    public void onSystemUiVisibilityChange(int visibility) {

        // todo should we make fulscreen for sdk 19>
        if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
            uiIsHidden = true;
        } else {
            uiIsHidden = false;
        }
        syncUiState();
    }



    */
    @Override
    public void onBackPressed() {
        if (showingPager) {
            hidePager();
        } else {
            finish();
        }
    }

    private void showPager() {

        transitionImageView.clear();
        transitionImageView.setAlpha(0f);
        transitionBackgroundView.setAlpha(0f);
        transitionBackgroundView.setVisibility(View.GONE);
        //transitionImageView.setVisibility(View.GONE);
        showingPager = true;
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAlpha(1);
        toolbar.setTitle(getString(R.string.media_pager, selectedIndex + 1, displayList.getSize()));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectedIndex = position;
                toolbar.setTitle(getString(R.string.media_pager, position + 1, displayList.getSize()));
                recyclerView.smoothScrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setCurrentItem(selectedIndex, false);
    }

    abstract class TransitionAnimation {
        protected boolean skipAnimation;

        abstract Runnable runnable(View view, Bitmap bitmap);

        void skipAnimation() {
            skipAnimation = true;
        }
    }

    private void hidePager() {
        Message media = displayList.getItem(selectedIndex);
        transitionImageView.setVisibility(View.VISIBLE);
        transitionImageView.animate()
                .setStartDelay(0)
                .setDuration(0)
                .alpha(1)
                .setListener(null)
                .start();
        transitionBackgroundView.setVisibility(View.VISIBLE);
        transitionBackgroundView.setAlpha(1);
        final TransitionAnimation transitionAnimation = new TransitionAnimation() {
            @Override
            public Runnable runnable(View view, final Bitmap bitmap) {
                return new Runnable() {
                    @Override
                    public void run() {
                        toolbar.setTitle(R.string.media);
                        showingPager = false;
                        viewPager.setAdapter(null);
                        viewPager.setAlpha(0);
                        viewPager.setVisibility(View.GONE);
                        transitionImageView.setExtraReceiverCallback(null);
                        // posdelayed is needed cause View does not set alpha immidiately

                        Animator.AnimatorListener listener = new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                invalidateOptionsMenu();
                                toolbar.setTitle(R.string.media);
                                transitionImageView.clear();
                                transitionImageView.setAlpha(0f);
                                transitionBackgroundView.setAlpha(0f);
                                transitionBackgroundView.setOnClickListener(null);
                                transitionBackgroundView.setVisibility(View.GONE);
                            }
                        };
                        if(!skipAnimation) {

                            int firstVisiblePosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                            final View selectedView = recyclerView.getChildAt(selectedIndex - firstVisiblePosition);

                            final int[] location = new int[2];
                            selectedView.getLocationInWindow(location);
                            MediaFullscreenAnimationUtils.animateBack(transitionImageView, bitmap, location[0], location[1], selectedView.getWidth(), selectedView.getHeight(),
                                    listener);
                            MediaFullscreenAnimationUtils.animateBackgroundBack(transitionBackgroundView, null);
                        } else {
                            listener.onAnimationEnd(null);
                        }
                    }
                };
            }
        };
        transitionImageView.setExtraReceiverCallback(new ReceiverCallback() {
            @Override
            public void onImageLoaded(final BitmapReference bitmap) {
                transitionImageView.post(transitionAnimation.runnable(transitionImageView, bitmap.getBitmap()));
            }

            @Override
            public void onImageCleared() {
                Log.d("Media Activity", "cleared");
            }

            @Override
            public void onImageError() {
                Log.d("Media Activity", "error");
            }
        });
        final DocumentContent document = (DocumentContent) media.getContent();
        if (document.getSource() instanceof FileRemoteSource) {
            FileRemoteSource remoteSource = (FileRemoteSource) document.getSource();
            final FileReference location = remoteSource.getFileReference();
            messenger().requestState(location.getFileId(), new FileCallback() {
                @Override
                public void onNotDownloaded() {
                    transitionAnimation.skipAnimation();
                    transitionAnimation.runnable(null, null).run();
                }

                @Override
                public void onDownloading(float progress) {
                }

                @Override
                public void onDownloaded(final FileSystemReference reference) {
                    MVVMEngine.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            transitionImageView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    transitionImageView.request(new RawFileTask(reference.getDescriptor()));
                                }
                            }, 50);

                        }

                    });
                }
            });
            Logger.d(TAG, "Remote =(");
            // todo not loaded?
        } else if (document.getSource() instanceof FileLocalSource) {
            final String path = ((FileLocalSource) document.getSource()).getFileDescriptor();
            transitionImageView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    transitionImageView.request(new RawFileTask(path));
                }
            }, 50);
        }
    }

    public static Intent getIntent(Peer peer, Context context) {
        Intent intent = new Intent(context, MediaActivity.class);
        intent.putExtra(EXTRA_PEER_UNIQ_ID, peer.getUnuqueId());
        return intent;
    }

    public static class MediaFullscreenAnimationUtils {

        public static final int animationMultiplier = 1;
        public static int startDelay = 60;

        public static void animateForward(final View transitionView, Bitmap bitmap,
                                          int transitionLeft, int transitionTop,
                                          int transitionWidth, int transitionHeight, final Animator.AnimatorListener listener) {
            transitionView.clearAnimation();
            float bitmapWidth = bitmap.getWidth();
            float bitmapHeight = bitmap.getHeight();


            float screenWidth = Screen.getWidth();
            float screenHeight = Screen.getHeight() + (Build.VERSION.SDK_INT >= 21 ? Screen.getNavbarHeight() : 0);

            if (bitmapHeight > screenHeight || bitmapWidth > screenWidth) {
                if (bitmapWidth / screenWidth < bitmapHeight / screenHeight) {
                    bitmapWidth = bitmapWidth * (screenHeight / bitmapHeight);
                    bitmapHeight = screenHeight;
                } else {
                    bitmapHeight = bitmapHeight * (screenWidth / bitmapWidth);
                    bitmapWidth = screenWidth;
                }
            }

            float startScaleWidth = (float) transitionWidth / bitmapWidth;
            float startScaleHeight = (float) transitionHeight / bitmapHeight;
            /*transitionView.setLeft((int) (transitionLeft + (bitmapWidth * (startScaleWidth - 1) / 2)));
            transitionView.setTop((int) (transitionTop + (bitmapHeight * (startScaleHeight - 1) / 2)));
            transitionView.setScaleX(startScaleWidth);
            transitionView.setScaleY(startScaleHeight);*/
            transitionView.animate().setInterpolator(new MaterialInterpolator())
                    .setDuration(0)
                    .setStartDelay(0)
                    .alpha(1)
                    .x(transitionLeft + (bitmapWidth * (startScaleWidth - 1) / 2))
                    .y(transitionTop + (bitmapHeight * (startScaleHeight - 1) / 2))
                    .scaleX(startScaleWidth)
                    .scaleY(startScaleHeight)
                    .setListener(null)
                    .start();

            float endScale = 1;
            // float endScaleHeight = 1;
            float xPadding = 0;
            float yPadding = 0;
            if (bitmapWidth / screenWidth > bitmapHeight / screenHeight) {
                endScale = screenWidth / bitmapWidth;
                xPadding = (bitmapWidth * (endScale - 1) / 2);
                yPadding = screenHeight / 2 - (bitmapHeight / 2);
            } else {
                endScale = screenHeight / bitmapHeight;
                xPadding = screenWidth / 2 - (bitmapWidth / 2);
                yPadding = (bitmapHeight * (endScale - 1)) / 2;
            }
            final float finalXPadding = xPadding;
            final float finalEndScale = endScale;
            final float finalYPadding = yPadding;
            transitionView.post(new Runnable() {
                @Override
                public void run() {
                    transitionView.animate()
                            .setInterpolator(new MaterialInterpolator())
                            .setStartDelay(startDelay)
                            .setDuration(300 * animationMultiplier)
                            .setInterpolator(new MaterialInterpolator())
                            .x(finalXPadding)
                            .y(finalYPadding)
                            .scaleX(finalEndScale)
                            .scaleY(finalEndScale)
                            .setListener(listener)
                            .start();
                }
            });

        }

        public static void animateBack(final View transitionView, Bitmap bitmap,
                                       final int transitionLeft, final int transitionTop,
                                       int transitionWidth, int transitionHeight, final Animator.AnimatorListener listener) {
            transitionView.clearAnimation();
            float bitmapWidth = bitmap.getWidth();
            float bitmapHeight = bitmap.getHeight();


            float screenWidth = Screen.getWidth();
            float screenHeight = Screen.getHeight() + (Build.VERSION.SDK_INT >= 19 ? Screen.getNavbarHeight() : 0);

            if (bitmapHeight > screenHeight || bitmapWidth > screenWidth) {
                if (bitmapWidth / screenWidth < bitmapHeight / screenHeight) {
                    bitmapWidth = bitmapWidth * (screenHeight / bitmapHeight);
                    bitmapHeight = screenHeight;
                } else {
                    bitmapHeight = bitmapHeight * (screenWidth / bitmapWidth);
                    bitmapWidth = screenWidth;
                }
            }

            final float finishScaleWidth = (float) transitionWidth / bitmapWidth;
            final float finishScaleHeight = (float) transitionHeight / bitmapHeight;


            float endScale = 1;
            // float endScaleHeight = 1;
            float xPadding = 0;
            float yPadding = 0;
            if (bitmapWidth / screenWidth > bitmapHeight / screenHeight) {
                endScale = screenWidth / bitmapWidth;
                xPadding = (bitmapWidth * (endScale - 1) / 2);
                yPadding = screenHeight / 2 - (bitmapHeight / 2);
            } else {
                endScale = screenHeight / bitmapHeight;
                xPadding = screenWidth / 2 - (bitmapWidth / 2);
                yPadding = (bitmapHeight * (endScale - 1)) / 2;
            }

            transitionView.animate()
                    .setInterpolator(new MaterialInterpolator())
                    .setStartDelay(0)
                    .setDuration(0)
                    .setInterpolator(new MaterialInterpolator())
                    .x(xPadding)
                    .y(yPadding)
                    .scaleX(endScale)
                    .scaleY(endScale)
                    .setListener(null)
                    .start();

            final float finalBitmapWidth = bitmapWidth;
            final float finalBitmapHeight = bitmapHeight;
            final float finalXPadding = xPadding;
            final float finalYPadding = yPadding;
            transitionView.post(new Runnable() {
                @Override
                public void run() {
                    transitionView.animate()
                            .setStartDelay(startDelay)
                            .setInterpolator(new MaterialInterpolator())
                            .setDuration(300 * animationMultiplier)
                            .x(transitionLeft + (finalBitmapWidth * (finishScaleWidth - 1) / 2))
                            .y(transitionTop + (finalBitmapHeight * (finishScaleHeight - 1) / 2))
                            .scaleX(finishScaleWidth)
                            .scaleY(finishScaleHeight)
                            .setListener(listener)
                            .start();
                }
            });
        }

        public static void animateBackgroundForward(View backgroundView, Animator.AnimatorListener listener) {
            backgroundView.animate()
                    .setDuration(300 * animationMultiplier)
                    .setInterpolator(new MaterialInterpolator())
                    .alpha(1)
                    .setListener(listener)
                    .start();
        }

        public static void animateBackgroundBack(View backgroundView, Animator.AnimatorListener listener) {
            backgroundView.animate()
                    .setDuration(300 * animationMultiplier)
                    .setInterpolator(new MaterialInterpolator())
                    .alpha(0)
                    .setListener(listener)
                    .start();
        }
    }
}
