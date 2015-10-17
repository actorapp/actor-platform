package com.droidkit.pickers.file;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.droidkit.pickers.file.items.ExplorerItem;
import com.droidkit.pickers.file.search.IndexTask;
import com.droidkit.pickers.file.search.SearchTask;
import com.droidkit.pickers.file.view.MaterialInterpolator;
import com.droidkit.pickers.view.SearchViewHacker;

import java.io.File;
import java.util.ArrayList;

import im.actor.messenger.R;


public class SearchFileFragment extends Fragment implements AbsListView.OnScrollListener {
    private View rootView;
    private String lastTitle;
    private ArrayList<ExplorerItem> items = new ArrayList<ExplorerItem>();
    SearchTask searchingTask;
    private ListView listView;
    private ExplorerAdapter adapter;
    private String root;
    private TextView status;
    private BasePickerActivity pickerActivity;
    private android.support.v7.widget.SearchView searchView;
    private boolean animated = false;
    private IndexTask indexingTask;
    private ArrayList<File> index = new ArrayList<File>();
    private String query;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.picker_fragment_file_search, container, false);
        root = getArguments().getString("root");

//        ViewGroup searchContainer = (ViewGroup) rootView.findViewById(R.id.search_container);
        View contentContainer = rootView.findViewById(R.id.content_container);

        status = (TextView) rootView.findViewById(R.id.status);
        // progress = new IndeterminateWrapper(pickerActivity);
        // progress.show();
        listView = (ListView) contentContainer.findViewById(R.id.list);
        listView.setOnScrollListener(this);

        adapter = new ExplorerAdapter(pickerActivity, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(pickerActivity);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(300);
        alphaAnimation.setInterpolator(new MaterialInterpolator());

        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, -150, 0);
        translateAnimation.setDuration(450);
        translateAnimation.setInterpolator(new MaterialInterpolator());

        //  searchContainer.startAnimation(translateAnimation);
        contentContainer.startAnimation(alphaAnimation);

        /**/

        pickerActivity.getSupportActionBar().setTitle(R.string.picker_file_search_activity_title);
        if (index.isEmpty()) {
            indexingTask = new IndexTask(new File(root)) {
                @Override
                public void onIndexingEnded(ArrayList<File> indexedItems) {
                    index.clear();
                    index.addAll(indexedItems);
                    indexingTask = null;
                    if (searchingTask != null) {
                        searchingTask.execute();
                    } else {
                        //  progress.hide();
                    }
                }
            };
            //searchingProgressBar.progressiveStart();
            indexingTask.execute();
        } else {
        }
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == R.id.menu_search) {
            searchView.setIconified(!searchView.isIconified());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.picker_search, menu);
        MenuItem searchMenuItem = menu.getItem(0);

        searchView = (android.support.v7.widget.SearchView) searchMenuItem.getActionView();

        searchView.setIconified(false);
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            // searchPlate.setBackgroundColor(Color.DKGRAY);
            int searchTextId = searchPlate.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView searchText = (TextView) searchPlate.findViewById(searchTextId);
            if (searchText != null) {
                searchText.setTextColor(Color.WHITE);
                searchText.setHintTextColor(Color.WHITE);
            }
        }

        /*
        SearchViewHacker.setCloseIcon(searchView, R.drawable.bar_clear_search);
        SearchViewHacker.setIcon(searchView, R.drawable.picker_bar_search);
        SearchViewHacker.setText(searchView, getResources().getColor(R.color.picker_file_searchbox_focused_color));
        SearchViewHacker.setEditText(searchView, R.drawable.picker_search_text_box);
        SearchViewHacker.setHint(searchView, getString(R.string.picker_file_search_query_text), 0, getResources().getColor(R.color.picker_file_searchbox_hint_color), null);
        SearchViewHacker.disableMagIcon(searchView);
        */

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                hideKeyBoard();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {

                if (query.equals("")) {
                    SearchViewHacker.disableCloseButton(searchView);
                    return true;
                } else {
                    SearchViewHacker.setCloseIcon(searchView, R.drawable.bar_clear_search);

                }
                SearchFileFragment.this.query = query;

                if (searchingTask != null) {
                    searchingTask.cancel(true);
                    searchingTask = null;
                }
                File rootFile = new File(root);
                if (root == null || root.equals("")) {
                    rootFile = null;
                }
                // progress.show();
                searchingTask = new SearchTask(rootFile, query, index) {
                    @Override
                    public void onSearchEnded(final ArrayList<ExplorerItem> files) {
                        searchingTask = null;
                        // progress.hide();
                        if (files.isEmpty()) {
                            status.setVisibility(View.VISIBLE);
                            status.setText(R.string.picker_empty);
                            items.clear();
                            adapter.notifyDataSetChanged();
                            // listView.setVisibility(View.GONE);
                        } else {
                            showItems(files);
                            /*
                            AnimationSet outAnimation = new AnimationSet(true);
                            outAnimation.addAnimation(new TranslateAnimation(0, 0, 100, 0));
                            outAnimation.addAnimation(new AlphaAnimation(0,1));
                            outAnimation.setDuration(350);
                            outAnimation.setInterpolator(new MaterialInterpolator());
                            outAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    animated = true;
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    animated = false;
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            listView.startAnimation(outAnimation);*/
                        }

                    }
                };
                if (indexingTask == null || !index.isEmpty()) {
                    searchingTask.execute();
                }
                return false;
            }
        });
        searchView.setOnCloseListener(new android.support.v7.widget.SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                hideKeyBoard();
                getActivity().onBackPressed();
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (searchView.getQuery().length() == 0) {
                    searchView.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            SearchViewHacker.disableCloseButton(searchView);
                        }
                    }, 0);
                    SearchViewHacker.disableCloseButton(searchView);
                } else
                    SearchViewHacker.setCloseIcon(searchView, R.drawable.bar_clear_search);
            }
        });
        searchView.requestFocusFromTouch();
        SearchViewHacker.disableCloseButton(searchView);
        InputMethodManager inputMethodManager = (InputMethodManager) pickerActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        if (query != null) {
            searchView.setQuery(query, false);
        }
    }

    private void showItems(ArrayList<ExplorerItem> found) {
        items.clear();
        items.addAll(found);
        status.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        if (!animated) {
            listView.setAlpha(0);
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.setAlpha(1);
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        View searchItemView = listView.getChildAt(i);
                        AnimationSet slideInAnimation = new AnimationSet(true);
                        slideInAnimation.setInterpolator(new MaterialInterpolator());
                        slideInAnimation.setDuration(200);
                        if (i != 0) {
                            slideInAnimation.setStartOffset(i * 50 + 25);
                        } else {
                            slideInAnimation.setStartOffset(75);
                        }
                        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                        slideInAnimation.addAnimation(alphaAnimation);
                        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 150, 0);
                        slideInAnimation.addAnimation(translateAnimation);
                        searchItemView.startAnimation(slideInAnimation);
                    }
                }
            });

            animated = true;
        } else
            listView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        hideKeyBoard();
        pickerActivity.getSupportActionBar().setIcon(R.drawable.picker_bar_filepicker_icon);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // pickerActivity.searchDisable();
        pickerActivity.setFragment(this);
        pickerActivity.invalidateOptionsMenu();
        pickerActivity.getSupportActionBar().setIcon(R.drawable.picker_bar_search);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.pickerActivity = (BasePickerActivity) activity;
    }

    void hideKeyBoard() {
        if (searchView != null)
            searchView.clearFocus();
        pickerActivity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        View focusedView = pickerActivity.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) pickerActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        switch (i) {
            case SCROLL_STATE_FLING:
                break;
            case SCROLL_STATE_IDLE:
                break;
            case SCROLL_STATE_TOUCH_SCROLL:
                hideKeyBoard();
                break;
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {

    }
}