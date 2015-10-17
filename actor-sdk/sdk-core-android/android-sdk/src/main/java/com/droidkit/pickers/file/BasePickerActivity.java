package com.droidkit.pickers.file;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidkit.pickers.file.items.ExplorerItem;

import java.io.File;
import java.util.ArrayList;

import im.actor.messenger.R;

/**
 * Created by kiolt_000 on 15/09/2014.
 */
public abstract class BasePickerActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    protected ArrayList<String> selectedItems = new ArrayList<String>();
    private boolean searchEnabled;
    protected Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_picker);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.picker_fragment_explorer_welcome_enter, R.animator.picker_fragment_explorer_welcome_exit)
                    .add(R.id.container, getWelcomeFragment())
                    .commit();
        }
        //getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(false);
        // getSupportActionBar().setDisplayUseLogoEnabled(false);

        View select = findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnResult();
            }
        });
        View cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    protected void returnResult() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("picked", selectedItems);
        setResult(RESULT_OK, returnIntent);
        save();
        finish();
    }

    protected abstract Fragment getWelcomeFragment();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentFragment != null) {
            currentFragment.onCreateOptionsMenu(menu, getMenuInflater());
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                // finish();
                onBackPressed();
                return true;
        }
        if (currentFragment != null)
            return currentFragment.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        // currentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    protected abstract void save();

    public boolean selectItem(String path) {

        boolean selected = !selectedItems.contains(path);

        if (selected) {
            if (selectedItems.size() > 9) {
                Toast.makeText(this, "You can pick only 10 items.", Toast.LENGTH_SHORT).show();
                return false;
            }
            selectedItems.add(path);
        } else {
            selectedItems.remove(path);
        }

        //itemView.findViewById(R.id.selected).setSelected(selected);
        updateCounter();
        return selected;
    }

    public void selectItem(ExplorerItem item, View itemView) {
        item.setSelected(selectItem(item.getPath()));
        item.bindData(itemView);
    }

    public void updateCounter() {
        final TextView counterView = (TextView) findViewById(R.id.counter);
        View selectView = findViewById(R.id.select);
        View cancelView = findViewById(R.id.cancel);
        View controllerHolder = findViewById(R.id.controllers);
        if (!selectedItems.isEmpty()) {
            counterView.setText("" + selectedItems.size());
        }
        final View counterHolder = findViewById(R.id.counter_holder);
        findViewById(R.id.select_text).setEnabled(!selectedItems.isEmpty());
        selectView.setEnabled(!selectedItems.isEmpty());
        if ((selectedItems.isEmpty() && counterHolder.getVisibility() != View.GONE)
                || (!selectedItems.isEmpty() && counterHolder.getVisibility() == View.GONE)) {

            final int i = counterHolder.getLayoutParams().width;
            ValueAnimator valueAnimator;
            if (!selectedItems.isEmpty()) {
                valueAnimator = ValueAnimator.ofInt(0, i);
            } else {
                valueAnimator = ValueAnimator.ofInt(i, 0);
            }
            valueAnimator.setDuration(100);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Integer width = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams params = counterHolder.getLayoutParams();
                    if (width == 0) {
                        params.width = i;
                        counterHolder.setVisibility(View.GONE);
                    } else {
                        counterHolder.setVisibility(View.VISIBLE);
                        params.width = width;
                    }
                    counterHolder.setLayoutParams(params);
                    counterHolder.invalidate();

                }
            });
            valueAnimator.start();
        }
    }

    public boolean isSelected(String path) {
        return selectedItems.contains(path);
    }

    public boolean isSelected(File file) {
        return isSelected(file.getPath());
    }

    public void setFragment(Fragment fragment) {
        this.currentFragment = fragment;
    }
}
