package im.actor.console;

import com.googlecode.lanterna.gui.Component;

import java.util.List;

/**
 * Created by ex3ndr on 09.02.15.
 */
public class ConsoleList<T> {

    private List<T> items;
    private Adapter<T> adapter;
    private Panel panel;

    public ConsoleList(Panel panel, List<T> items, Adapter<T> adapter) {
        this.adapter = adapter;
        this.panel = panel;
        update(items);
    }

    public void update(List<T> items) {
        this.items = items;

        panel.removeAllComponents();
        for (int i = 0; i < items.size(); i++) {
            panel.addComponent(adapter.createComponent(items.get(i)));
        }
    }

    public interface Adapter<T> {
        Component createComponent(T item);
    }
}
