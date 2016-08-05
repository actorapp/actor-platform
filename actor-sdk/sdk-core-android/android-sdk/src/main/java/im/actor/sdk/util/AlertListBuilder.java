package im.actor.sdk.util;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AlertListBuilder {

    private ArrayList<Item> items = new ArrayList<>();

    public AlertListBuilder addItem(String name, @NotNull SelectListener selectListener) {
        items.add(new Item(name, selectListener));
        return this;
    }

    private void select(int i) {
        items.get(i).getOnClickListener().onSelected();
    }

    public CharSequence[] getItems() {
        CharSequence[] res = new CharSequence[items.size()];
        for (int i = 0; i < items.size(); i++) {
            res[i] = items.get(i).getName();
        }
        return res;
    }

    private class Item {
        int id;
        String name;
        SelectListener selectListener;

        public Item(String name, @NotNull SelectListener selectListener) {
            this.name = name;
            this.selectListener = selectListener;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @NotNull
        public SelectListener getOnClickListener() {
            return selectListener;
        }
    }

    public interface SelectListener {
        void onSelected();
    }

    public android.app.AlertDialog.Builder build(Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setItems(getItems(), (dialog, which) -> {
            select(which);
        });
        return builder;
    }
}