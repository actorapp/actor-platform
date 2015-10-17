package com.droidkit.pickers.file.search;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.droidkit.pickers.file.items.ExplorerItem;
import com.droidkit.pickers.file.util.Converter;
import com.droidkit.pickers.file.util.FileSearchOrderComparator;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by kiolt_000 on 07/10/2014.
 */
public abstract class SearchTask extends AsyncTask<Void, Void, ArrayList<ExplorerItem>> {

    private final File root;
    private final String strongRegex;
    private final ArrayList<File> foundItems;
    private final ArrayList<File> index;
    private final String weakRegex;
    private final String query;
    private Handler handler;

    public SearchTask(File searchRoot, String searchQuery, ArrayList<File> index) {
        this.root = searchRoot;
        this.query = searchQuery;
        String tempRegex = searchQuery.toLowerCase();
        String[] splitedTempRegex = tempRegex.split("\\s+");
        ArrayList<String> filtered = new ArrayList<String>();
        for (String s : splitedTempRegex) {
            if (s != null && !s.equals("")) {
                filtered.add(s);
            }
        }
        tempRegex = filtered.toString()
                .replaceAll("\\[", "(").replaceAll("]", ")")
                .replaceAll(",", "|")
                .replaceAll("\\s+", "")
                .replaceAll("\\.", "\\\\.");
        this.strongRegex = "(((.*)(\\s+))|(^))" + tempRegex + ".*";
        this.weakRegex = ".*" + tempRegex + ".*";
        this.index = index;
        handler = new Handler();
        foundItems = new ArrayList<File>();
    }

    public void start() {
        onPostExecute(doInBackground());
    }

    @Override
    protected final ArrayList<ExplorerItem> doInBackground(Void... voids) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                //onPreStart();
            }
        });


        handler.post(new Runnable() {
            @Override
            public void run() {
                //onSearchStarted();
            }
        });
        Log.i("Searching", "Search started. Root path: " + root);
        for (File file : index) {
            compare(file);
            if (isCancelled()) {
                Log.i("Searching", "Canceled");
                return null;
            }
        }
        resort();
        Log.i("Searching", "Search ended. " + foundItems.size() + " items found");
        ArrayList<ExplorerItem> items = new ArrayList<ExplorerItem>();
        for (File file : foundItems) {
            ExplorerItem item;
            if (file.isDirectory()) {
                item = Converter.getFolderItem(file);

            } else {
                item = Converter.getFileItem(file, false);
            }
            if (item != null)
                items.add(item);
        }
        // Collections.sort(items, new CancelableComparator(query));
        return items;
    }

    private class CancelableComparator extends FileSearchOrderComparator {

        public CancelableComparator(String searchQuery) {
            super(searchQuery);
        }

        @Override
        public int compare(ExplorerItem explorerItem, ExplorerItem explorerItem2) {
            if (isCancelled()) {
                return 0;
            }
            return super.compare(explorerItem, explorerItem2);
        }
    }

    private ArrayList<File> strongRegexFiles = new ArrayList<File>();
    private ArrayList<File> strongRegexFolders = new ArrayList<File>();
    private ArrayList<File> weakRegexFiles = new ArrayList<File>();
    private ArrayList<File> weakRegexFolders = new ArrayList<File>();

    private void compare(File file) {
        if (root == null || file.getPath().contains(root.getPath())) {

            String name = file.getName().toLowerCase();
            if (name.matches(weakRegex)) {
                if (name.matches(strongRegex)) {

                    if (file.isDirectory()) {
                        strongRegexFolders.add(file);
                    } else {
                        strongRegexFiles.add(file);
                    }
                } else if (file.isDirectory()) {
                    weakRegexFolders.add(file);
                } else {
                    weakRegexFiles.add(file);
                }
            }
        }
    }

    void resort() {
        foundItems.addAll(strongRegexFolders);
        foundItems.addAll(weakRegexFolders);
        foundItems.addAll(strongRegexFiles);
        foundItems.addAll(weakRegexFiles);
    }

    @Override
    protected final void onPostExecute(ArrayList<ExplorerItem> files) {
        if (files != null)
            onSearchEnded(files);
    }

    protected abstract void onSearchEnded(ArrayList<ExplorerItem> files);
}