package com.droidkit.pickers.file.search;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by kiolt_000 on 17/09/2014.
 */
public abstract class IndexTask extends AsyncTask<Void, File, Integer> {
    private final File root;
    private final ArrayList<File> index;
    private Integer foundCount = 0;
    private Handler handler;

    public IndexTask(File searchRoot) {
        this.root = searchRoot;
        handler = new Handler();
        index = new ArrayList<File>();
    }


    @Override
    protected final Integer doInBackground(Void... voids) {
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
        Log.i("Searching", "Indexing started. Root path: " + root);
        if (!root.getPath().equals("")) {
            scanFolder(root);
        } else {
            scanFolder(Environment.getExternalStorageDirectory());
            /*scanFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            scanFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            if (Build.VERSION.SDK_INT >= 19) {
                scanFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
            }
            scanFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));*/
        }
        if (isCancelled()) {
            return null;
        } else
            Log.i("Search", "Indexing ended. " + index.size() + " items indexed");
        return foundCount;
    }

    private void scanFolder(File folder) {
        if (folder.getPath().contains("/sys")
                || folder.getPath().toLowerCase().contains("/cache")
                || folder.getPath().toLowerCase().contains(Environment.getExternalStorageDirectory().getPath().toLowerCase() + "/android")) {
            return;
        }

        if (folder.listFiles() == null || folder.getName().toCharArray()[0] == '.') {
            return;
        }
        index.add(folder);
        for (final File file : folder.listFiles()) {
            if (isCancelled()) {
                return;
            }
            if (file.isDirectory()) {
                scanFolder(file);
            } else {
                if (file.getName().toCharArray()[0] == '.') {
                    continue;
                }
                index.add(file);
            }
        }
    }


    @Override
    protected final void onProgressUpdate(final File... values) {
        for (File value : values) {
            index.add(value);
        }
    }

    @Override
    protected final void onPostExecute(Integer result) {

        onIndexingEnded(index);
    }

    public abstract void onIndexingEnded(ArrayList<File> indexedItems);
}