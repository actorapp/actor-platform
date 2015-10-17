package com.droidkit.pickers.view;

import android.content.res.Resources;
import android.support.v7.internal.widget.TintImageView;
import android.support.v7.widget.SearchView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import java.lang.reflect.Field;

/**
 * Created by ex3ndr on 25.09.14.
 */
public class SearchViewHacker {

    private static View findView(View root, String name) {
        try {
            Field field = root.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return (View) field.get(root);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        throw new RuntimeException();
    }

    public static void setIcon(SearchView searchView, int res) {
        ImageView searchImageView = (ImageView) findView(searchView, "mSearchButton");
        searchImageView.setImageResource(res);

        TintImageView searchHintView = (TintImageView) findView(searchView, "mSearchHintIcon");
        searchHintView.setImageResource(res);
    }

    public static void setText(SearchView searchView, int color) {
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) findView(searchView, "mQueryTextView");
        autoComplete.setTextColor(color);
        // autoComplete.setHighlightColor(color);
    }

    public static void setHint(SearchView searchView, String hint, int resId, int color, Resources resources) {
        AutoCompleteTextView autoComplete = (AutoCompleteTextView) findView(searchView, "mQueryTextView");

        SpannableStringBuilder stopHint = new SpannableStringBuilder("");
        stopHint.append(hint);

        autoComplete.setHint(stopHint);
        autoComplete.setHintTextColor(color);
    }

    public static void setEditText(SearchView searchView, int resId) {
        View autoComplete = findView(searchView, "mSearchPlate");
        autoComplete.setBackgroundResource(resId);
    }

    public static void setCloseIcon(SearchView searchView, int res) {
        ImageView searchImageView = (ImageView) findView(searchView, "mCloseButton");
        searchImageView.setVisibility(View.VISIBLE);
        searchImageView.setAdjustViewBounds(false);
        searchImageView.setImageResource(res);
    }

    public static void disableCloseButton(SearchView searchView) {
        ImageView searchImageView = (ImageView) findView(searchView, "mCloseButton");
        // searchImageView.setMaxWidth(0);
        searchImageView.setVisibility(View.GONE);
        searchImageView.setImageBitmap(null);
        searchImageView.setAdjustViewBounds(true);
    }

    public static void disableMagIcon(SearchView searchView) {
        ImageView searchImageView = (ImageView) findView(searchView, "mSearchHintIcon");
        searchImageView.setMaxWidth(0);
        searchImageView.setImageBitmap(null);
        searchImageView.setVisibility(View.GONE);
        searchImageView.setAdjustViewBounds(true);
    }


}