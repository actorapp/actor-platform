package com.droidkit.pickers.file;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.TextView;

import com.droidkit.pickers.file.items.BackItem;
import com.droidkit.pickers.file.items.ExplorerItem;
import com.droidkit.pickers.file.items.ExternalStorageItem;
import com.droidkit.pickers.file.items.HeaderItem;
import com.droidkit.pickers.file.items.StorageItem;
import com.droidkit.pickers.file.util.Converter;
import com.droidkit.pickers.file.util.FileDateOrderComparator;
import com.droidkit.pickers.file.util.FileNameOrderComparator;
import com.droidkit.pickers.file.util.HistoryDatabase;
import com.droidkit.pickers.file.view.MaterialInterpolator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import im.actor.messenger.R;

public class ExplorerFragment extends Fragment {

    protected static final String LOG_TAG = "FILE SELECTOR";
    protected View rootView;
    protected String path;
    private BasePickerActivity pickerActivity;
    private ArrayList<ExplorerItem> items;
    private TextView statusView;
    private ListView list;
    private String title;
    private boolean welcome = false;
    private ExplorerAdapter adapter;
    private Menu menu;
    private MenuItem sortnameMenuItem;
    private MenuItem sortdateMenuItem;
    private View emptyView;

    // animation needs
    private boolean animated;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pickerActivity = (BasePickerActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("Explorer Animation", "Created");
        {
            rootView = inflater.inflate(R.layout.picker_fragment_file_picker, container, false);
            list = (ListView) rootView.findViewById(R.id.list);
            Bundle bundle = getArguments();
            statusView = (TextView) rootView.findViewById(R.id.status);
            emptyView = rootView.findViewById(R.id.empty);

            items = new ArrayList<ExplorerItem>();

            if (bundle != null) {
                path = bundle.getString("path");


                Log.d(LOG_TAG, "Path: " + path);
                File currentPathFile = new File(path);
                File[] fileList = currentPathFile.listFiles();
                title = currentPathFile.getPath();
                if (title.contains(Environment.getExternalStorageDirectory().getPath())) {
                    title = title.replace(Environment.getExternalStorageDirectory().getPath(), "");
                }
                if (title.length() > 0 && title.toCharArray()[0] == '/') {
                    title = title.substring(1);
                }

                if (path.equals(Environment.getExternalStorageDirectory().getPath())) {
                    if (Environment.isExternalStorageEmulated()) {
                        title = getString(R.string.picker_file_memory_phone);
                    } else
                        title = getString((R.string.picker_file_memory_external));
                } else if (path.equals("/"))
                    title = getString(R.string.picker_file_memory_phone);

                if (fileList == null) {
                    statusView.setVisibility(View.VISIBLE);
                    File external = Environment.getExternalStorageDirectory();
                    if (path.equals(external.getPath()))
                        statusView.setText(R.string.picker_file_memory_external_error);
                    else
                        statusView.setText(R.string.picker_file_denied);

                    return rootView;
                } else {
                    if (fileList.length == 0) {

                        emptyView.setVisibility(View.VISIBLE);
                        AnimationSet slideInAnimation = new AnimationSet(true);
                        slideInAnimation.setInterpolator(new MaterialInterpolator());
                        slideInAnimation.setDuration(280);
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                        slideInAnimation.addAnimation(alphaAnimation);
                        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 150, 0);
                        slideInAnimation.addAnimation(translateAnimation);
                        emptyView.startAnimation(slideInAnimation);

                        statusView.setVisibility(View.VISIBLE);
                        AnimationSet slideInAnimation1 = new AnimationSet(true);
                        slideInAnimation1.setInterpolator(new MaterialInterpolator());
                        slideInAnimation1.setDuration(280);
                        slideInAnimation1.setStartOffset(150);//cause of offset
                        AlphaAnimation alphaAnimation1 = new AlphaAnimation(0, 1);
                        slideInAnimation1.addAnimation(alphaAnimation1);
                        TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, 150, 0);
                        slideInAnimation1.addAnimation(translateAnimation1);
                        statusView.startAnimation(slideInAnimation1);
                        statusView.setText(R.string.picker_file_directory_empty);
                        //return rootView;
                    }
                }

                Log.d(LOG_TAG, "Size: " + fileList.length);

                for (File file : fileList) {
                    putItem(file);
                }
                Collections.sort(items, new FileNameOrderComparator());

                insertBack();
                adapter = new ExplorerAdapter(getActivity(), items);

            } else {
                welcome = true;
//                items.add(new StorageItem(getActivity()));
                adapter = new WelcomeExplorerAdapter(getActivity(), items);
                items.add(new HeaderItem(getString(R.string.picker_file_header_main)));
                String externalStorageState = Environment.getExternalStorageState();
                Log.w(LOG_TAG, externalStorageState);
                if (
                        externalStorageState.equals(Environment.MEDIA_REMOVED)
                                || externalStorageState.equals(Environment.MEDIA_BAD_REMOVAL)
                                || externalStorageState.equals(Environment.MEDIA_UNKNOWN)
                                || externalStorageState.equals(Environment.MEDIA_UNMOUNTED)
                                || externalStorageState.equals(Environment.MEDIA_UNMOUNTABLE)
                                || externalStorageState.equals(Environment.MEDIA_SHARED)
                                || externalStorageState.equals(Environment.MEDIA_NOFS)
                        ) {
                    items.add(new StorageItem(getString(R.string.picker_file_memory_phone)));
                } else {/*
                    File cameraFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                    if(cameraFile.exists()) {
                       items.add(new FolderItem(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),R.drawable.picker_folder_camera,getString(R.string.picker_files_camera)));
                    }*/
                    putItem(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
                    putItem(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                    putItem((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
                    putItem(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
                    putItem((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)));
                    if (Environment.isExternalStorageEmulated()) {

                        items.add(new ExternalStorageItem(getString(R.string.picker_file_memory_phone), R.drawable.picker_memory));
                    } else
                        items.add(new ExternalStorageItem(getString(R.string.picker_file_memory_external), R.drawable.picker_sdcard));


                    if (Build.VERSION.SDK_INT >= 19) {
                        // putItem(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
                        // even on sdk>19 documents folder does not work.
                    }
                }
                path = "";
                title = getString(R.string.picker_file_activity_title);

                ArrayList<ExplorerItem> historyItems = loadHistory();
                if (!historyItems.isEmpty()) {
                    items.addAll(historyItems);
                }

            }

            list.setAdapter(adapter);
            list.setOnItemClickListener((BasePickerActivity) getActivity());

        }
        pickerActivity.updateCounter();

        return rootView;
    }

    private void reinsertBack() {
        removeBack();
        insertBack();
    }

    private void removeBack() {
        for (ExplorerItem item : items) {
            if (item instanceof BackItem) {
                items.remove(item);
                break;
            }
        }
    }

    private void insertBack() {
        items.add(0, new BackItem());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        if (!welcome) {
            inflater.inflate(R.menu.picker_file, menu);
            sortnameMenuItem = menu.findItem(R.id.sortname);
            sortdateMenuItem = menu.findItem(R.id.sortdate);
        } else
            inflater.inflate(R.menu.picker_file_welcome, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sortname) {
            list.post(new Runnable() {
                @Override
                public void run() {
                    sortdateMenuItem.setVisible(true);
                    sortnameMenuItem.setVisible(false);
                }
            });
            removeBack();
            Collections.sort(items, new FileNameOrderComparator());
            insertBack();
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.sortdate) {
            list.post(new Runnable() {
                @Override
                public void run() {
                    sortdateMenuItem.setVisible(false);
                    sortnameMenuItem.setVisible(true);
                }
            });
            removeBack();
            Collections.sort(items, new FileDateOrderComparator());
            insertBack();
            adapter.notifyDataSetChanged();
            return true;
        } else if (id == R.id.search) {
            Bundle bundle = new Bundle();
            bundle.putString("root", path);
            SearchFileFragment searchFragment = new SearchFileFragment();
            searchFragment.setArguments(bundle);
            pickerActivity.getFragmentManager().beginTransaction()
                    //.setCustomAnimations(R.animator.picker_fragment_explorer_welcome_enter, R.animator.picker_fragment_explorer_welcome_exit)
                    .replace(R.id.container, searchFragment)
                    .addToBackStack("search")
                    .commit();
            //pickerActivity.searchDisable();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void putItem(File file) {

        ExplorerItem item;
        if (file.isDirectory()) {
            item = getFolderItem(file);

        } else {
            item = getFileItem(file);
        }
        if (item != null)
            items.add(item);
    }

    private ExplorerItem getFolderItem(File file) {
        return Converter.getFolderItem(file);
    }

    private ExplorerItem getFileItem(File file) {
        return Converter.getFileItem(file, pickerActivity.isSelected(file.getPath()));
    }

    private ArrayList<ExplorerItem> loadHistory() {
        ArrayList<ExplorerItem> history = new ArrayList<ExplorerItem>();

        ArrayList<String> pathesFromDB = HistoryDatabase.getHistory(pickerActivity);
        if (!pathesFromDB.isEmpty()) {
            history.add(new HeaderItem(getString(R.string.picker_file_header_recent)));
            for (String pathFromDB : pathesFromDB) {
                File historyFile = new File(pathFromDB);
                if (historyFile.exists())
                    history.add(getFileItem(historyFile));
            }
        }

        return history;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Explorer Animation", "Resume");
        setTitle();
        pickerActivity.setFragment(this);
        pickerActivity.invalidateOptionsMenu();
        if (!animated) {
            animated = true;
        }
    }


    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        Log.d("Explorer animation", "CreateAnimator: " + transit + " " + enter + " " + nextAnim);


        int animationLength = 0;

        if (nextAnim == R.animator.picker_fragment_explorer_welcome_enter) {
            list.setAlpha(0);
            list.post(new Runnable() {
                @Override
                public void run() {
                    list.setAlpha(1);
                    int offsetIncreaseOffset = 0;
                    for (int i = 0; i < list.getChildCount(); i++) {
                        View searchItemView = list.getChildAt(i);
                        AnimationSet slideInAnimation = new AnimationSet(true);
                        slideInAnimation.setInterpolator(new MaterialInterpolator());
                        slideInAnimation.setDuration(180);
                        if (items.get(i) instanceof HeaderItem) {
                            offsetIncreaseOffset += 150;
                            slideInAnimation.setStartOffset(i * 50 + offsetIncreaseOffset);
                            offsetIncreaseOffset += 200;
                        } else
                            slideInAnimation.setStartOffset(i * 50 + offsetIncreaseOffset);
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                        slideInAnimation.addAnimation(alphaAnimation);
                        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 150, 0);
                        slideInAnimation.addAnimation(translateAnimation);
                        searchItemView.startAnimation(slideInAnimation);
                    }
                }
            });
            animationLength = list.getChildCount() * 100 + 50;
            Log.d("Explorer animation", "CreateAnimator: enter");
        } else if (nextAnim == R.animator.picker_fragment_explorer_enter) {
            list.setAlpha(0);
            list.post(new Runnable() {
                @Override
                public void run() {
                    list.setAlpha(1);
                    int offsetIncreaseOffset = 0;
                    for (int i = 0; i < list.getChildCount(); i++) {
                        View searchItemView = list.getChildAt(i);
                        AnimationSet slideInAnimation = new AnimationSet(true);
                        slideInAnimation.setInterpolator(new MaterialInterpolator());
                        slideInAnimation.setDuration(100);
                        slideInAnimation.setStartOffset(i * 50 + offsetIncreaseOffset);
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                        slideInAnimation.addAnimation(alphaAnimation);
                        TranslateAnimation translateAnimation = new TranslateAnimation(100, 0, 0, 0);
                        slideInAnimation.addAnimation(translateAnimation);
                        searchItemView.startAnimation(slideInAnimation);
                    }
                }
            });
            animationLength = list.getChildCount() * 100 + 50;
            Log.d("Explorer animation", "CreateAnimator: enter");
        } else if (nextAnim == R.animator.picker_fragment_explorer_welcome_exit ||
                nextAnim == R.animator.picker_fragment_explorer_exit) {
            for (int i = 0; i < list.getChildCount(); i++) {
                View searchItemView = list.getChildAt(i);
                AnimationSet slideInAnimation = new AnimationSet(true);
                slideInAnimation.setInterpolator(new MaterialInterpolator());
                slideInAnimation.setDuration(100);
                slideInAnimation.setStartOffset(i * 50);
                slideInAnimation.setFillAfter(true);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setFillAfter(true);
                slideInAnimation.addAnimation(alphaAnimation);
                TranslateAnimation translateAnimation = new TranslateAnimation(0, -100, 0, 0);
                slideInAnimation.addAnimation(translateAnimation);
                searchItemView.startAnimation(slideInAnimation);
            }

            animationLength = 0;// list.getChildCount() * 100 + 50;
            Log.d("Explorer animation", "CreateAnimator: exit");
        } else if (nextAnim == R.animator.picker_fragment_explorer_return) {
            list.setAlpha(0);
            list.post(new Runnable() {
                @Override
                public void run() {
                    list.setAlpha(1);
                    for (int i = 0; i < list.getChildCount(); i++) {
                        View searchItemView = list.getChildAt(i);
                        AnimationSet slideInAnimation = new AnimationSet(true);
                        slideInAnimation.setInterpolator(new MaterialInterpolator());
                        slideInAnimation.setDuration(100);
                        slideInAnimation.setStartOffset(i * 50);
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                        slideInAnimation.addAnimation(alphaAnimation);
                        TranslateAnimation translateAnimation = new TranslateAnimation(-100, 0, 0, 0);
                        slideInAnimation.addAnimation(translateAnimation);
                        searchItemView.startAnimation(slideInAnimation);
                    }
                }
            });
            animationLength = list.getChildCount() * 100 + 50;
            Log.d("Explorer animation", "CreateAnimator: return");
        } else if (nextAnim == R.animator.picker_fragment_explorer_out) {
            for (int i = 0; i < list.getChildCount(); i++) {
                View searchItemView = list.getChildAt(i);
                AnimationSet slideInAnimation = new AnimationSet(true);
                slideInAnimation.setInterpolator(new MaterialInterpolator());
                slideInAnimation.setDuration(100);
                slideInAnimation.setStartOffset(i * 50);
                slideInAnimation.setFillAfter(true);
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setFillAfter(true);
                slideInAnimation.addAnimation(alphaAnimation);
                TranslateAnimation translateAnimation = new TranslateAnimation(0, 100, 0, 0);
                slideInAnimation.addAnimation(translateAnimation);
                searchItemView.startAnimation(slideInAnimation);
            }
            if (items.size() == 1) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
                alphaAnimation.setInterpolator(new MaterialInterpolator());
                alphaAnimation.setDuration(100);
                alphaAnimation.setFillAfter(true);
                emptyView.startAnimation(alphaAnimation);
                statusView.startAnimation(alphaAnimation);
                animationLength = 250;
            }//else
            animationLength = 0;// list.getChildCount() * 100 + 50;
            Log.d("Explorer animation", "CreateAnimator: out");
        }

        AnimatorSet animator = (AnimatorSet) AnimatorInflater.loadAnimator(pickerActivity,
                R.animator.picker_fragment_explorer_enter);
        animator.setDuration(animationLength);

        return animator;
    }

    private void setTitle() {
        pickerActivity.getSupportActionBar().setTitle(title);
    }

}