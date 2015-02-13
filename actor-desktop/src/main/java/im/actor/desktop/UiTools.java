package im.actor.desktop;

import javax.swing.*;
import java.awt.*;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class UiTools {
    public static JPanel fill(JComponent panel) {
        JPanel chatsPanel = new JPanel();
        chatsPanel.setLayout(new GridLayout());
        chatsPanel.add(panel);
        chatsPanel.setOpaque(false);
        return chatsPanel;
    }

    public static void replace(Container container, JComponent component) {
        container.removeAll();
        container.add(component);
        container.revalidate();
    }

    public static JComponent scroller(JList list) {
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setBorder(null);
        listScroller.setViewportBorder(null);
        listScroller.setBackground(Color.WHITE);
        listScroller.getVerticalScrollBar().setUnitIncrement(8);
        listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return listScroller;
    }

    public static JPanel center(Component panel) {
        JPanel res = new JPanel(new GridBagLayout());
        res.add(panel);
        res.setBorder(null);
        res.setOpaque(false);
        return res;
    }

    public static JPanel boxLayout() {
        JPanel res = new JPanel();
        res.setLayout(new BoxLayout(res, BoxLayout.PAGE_AXIS));
        res.setBorder(null);
        res.setOpaque(false);
        return res;
    }

    public static JPanel verticalLayout() {
        JPanel res = new JPanel();
        res.setLayout(new BoxLayout(res, BoxLayout.Y_AXIS));
        res.setBorder(null);
        res.setOpaque(false);
        return res;
    }

    public static JPanel horizontalLayout() {
        JPanel res = new JPanel();
        res.setLayout(new BoxLayout(res, BoxLayout.X_AXIS));
        res.setBorder(null);
        res.setOpaque(false);
        return res;
    }

    public static Component fixSize(Component component, int w, int h) {
        component.setMaximumSize(new Dimension(w, h));
        component.setMinimumSize(new Dimension(w, h));
        component.setPreferredSize(new Dimension(w, h));
        return component;
    }
}
