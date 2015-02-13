package im.actor.desktop;

import im.actor.desktop.engines.SwingListEngine;
import im.actor.model.Messenger;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.MessageState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static im.actor.desktop.UiTools.*;
import static im.actor.desktop.UiTools.fixSize;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class MainPanel {
    public MainPanel(Container container, Messenger messenger) {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(240);

        JList<Dialog> jList = new JList<Dialog>();
        jList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jList.setLayoutOrientation(JList.VERTICAL);
        jList.setVisibleRowCount(-1);

        // DefaultListModel<Dialog> dataListModel = new DefaultListModel<Dialog>();
        // dataListModel.addElement(new Dialog(null, 0, "Title", null, 0, 0, Dialog.ContentType.TEXT, "Text", MessageState.UNKNOWN, hashCode(), 0, 0));
        // jList.setModel(dataListModel);
        jList.setModel(((SwingListEngine<Dialog>) messenger.getDialogs()).getListModel());
        jList.setCellRenderer(new DialogRenderer());
        jList.setFixedCellHeight(76);
        // jList.setFixedCellWidth(240);
        jList.setBorder(null);

        splitPane.setLeftComponent(fill(scroller(jList)));

        replace(container, splitPane);
    }

    private class DialogRenderer extends JPanel implements ListCellRenderer<Dialog> {

        public DialogRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(new EmptyBorder(8, 16, 8, 16));
            // setBackground(Color.WHITE);
            setBackground(Color.CYAN);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Dialog> list, Dialog value, int index, boolean isSelected, boolean cellHasFocus) {
            removeAll();

            JPanel avatar = new JPanel();
            avatar.setBackground(Color.DARK_GRAY);
            add(fixSize(avatar, 56, 56));

            add(Box.createRigidArea(new Dimension(8, 0)));

            JPanel messageContainer = verticalLayout();
            // messageContainer.setPreferredSize(new Dimension(300, 60));
            // messageContainer.setBackground(Color.BLUE);

            JPanel titleContainer = horizontalLayout();

            JLabel title = new JLabel();
            title.setText(value.getDialogTitle());

            JLabel status = new JLabel();
            status.setText(value.getStatus() + "");

            titleContainer.add(title);
            titleContainer.add(Box.createRigidArea(new Dimension(8, 0)));
            titleContainer.add(status);

            JLabel message = new JLabel();
            message.setText(value.getText());

            messageContainer.add(titleContainer);
            messageContainer.add(message);

            add(messageContainer);

            revalidate();
            return this;
        }
    }
}
