package im.actor.desktop;

import im.actor.desktop.engines.SwingListEngine;
import im.actor.model.Messenger;
import im.actor.model.entity.Dialog;
import im.actor.model.entity.Message;
import im.actor.model.entity.User;
import im.actor.model.entity.content.TextContent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;

import static im.actor.desktop.UiTools.*;
import static im.actor.desktop.UiTools.fixSize;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class MainPanel {
    private Messenger messenger;

    public MainPanel(Container container, final Messenger messenger) {
        this.messenger = messenger;

        JSplitPane splitPane = new JSplitPane();
        splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        splitPane.setDividerLocation(240);
        splitPane.setDividerSize(0);
        splitPane.setContinuousLayout(true);

        final JList<Dialog> jList = new JList<Dialog>();

        jList.setModel(((SwingListEngine<Dialog>) messenger.getDialogs()).getListModel());
        jList.setCellRenderer(new DialogRenderer());
        jList.setFixedCellHeight(76);
        jList.setFixedCellWidth(240);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setSelectionModel(new DefaultListSelectionModel());

        splitPane.setLeftComponent(fill(scroller(jList)));

        JPanel right = verticalLayout();
        JPanel root = new JPanel();
        root.setLayout(new OverlayLayout(root));

        JLabel emptyLabel = new JLabel("Select dialog");
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyLabel.setVerticalAlignment(SwingConstants.CENTER);
        emptyLabel.setBackground(Color.WHITE);
        emptyLabel.setOpaque(true);
        JPanel emptyLabelFill = fill(emptyLabel);
        emptyLabelFill.setVisible(false);
        root.add(emptyLabelFill);

        final JList<Message> jListMessages = new JList<Message>();
        jListMessages.setCellRenderer(new MessagesRenderer());

        jList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Dialog dialog = ((SwingListEngine<Dialog>) messenger.getDialogs()).getListModel().getElementAt(e.getFirstIndex());
                SwingListEngine<Message> chatMessages = ((SwingListEngine<Message>) messenger.getMessages(dialog.getPeer()));
                jListMessages.setModel(chatMessages.getListModel());
            }
        });

        root.add(fill(scroller(jListMessages)));

        root.setBackground(Color.WHITE);
        right.add(fill(root));
        right.add(fixSize(new JTextField(), 300, 64));
        right.setBackground(Color.WHITE);
        splitPane.setRightComponent(fill(right));

        replace(container, splitPane);
    }

    private class MessagesRenderer extends JPanel implements ListCellRenderer<Message> {

        public MessagesRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(8, 16, 8, 16));
            setBackground(Color.WHITE);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index, boolean isSelected, boolean cellHasFocus) {
            removeAll();
            User user = messenger.getUsers().getValue(value.getSenderId());
            add(fill(new JLabel(user.getName())));
            add(fill(new JLabel(((TextContent) value.getContent()).getText())));
            return this;
        }
    }

    private class DialogRenderer extends JPanel implements ListCellRenderer<Dialog> {

        public DialogRenderer() {
            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(new EmptyBorder(8, 8, 8, 8));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Dialog> list, Dialog value, int index, boolean isSelected, boolean cellHasFocus) {
            removeAll();

            JPanel avatar = new JPanel();
            avatar.setBackground(Color.DARK_GRAY);
            add(fixSize(avatar, 56, 56));

            add(Box.createRigidArea(new Dimension(8, 0)));

            JPanel messageContainer = verticalLayout();

            JPanel titleContainer = horizontalLayout();

            JLabel title = new JLabel();
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, MainPanel.class.getResourceAsStream("/fonts/Roboto-Bold.ttf"));
                title.setFont(font.deriveFont(14.0f));
            } catch (FontFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            title.setText(value.getDialogTitle());


            JLabel status = new JLabel();
            status.setText(value.getStatus() + "");

            titleContainer.add(title);
            titleContainer.add(Box.createHorizontalGlue());
            titleContainer.add(status);

            JLabel message = new JLabel();
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, MainPanel.class.getResourceAsStream("/fonts/Roboto-Medium.ttf"));
                message.setFont(font.deriveFont(14.0f));
            } catch (FontFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            message.setText(value.getText());
            messageContainer.add(fill(titleContainer));
            messageContainer.add(fill(message));

            fixSize(messageContainer, 240 - 32 - 8 - 56, 56);

            add(messageContainer);

            add(Box.createRigidArea(new Dimension(8, 0)));

            if (isSelected) {
                setBackground(Color.LIGHT_GRAY);
                title.setForeground(Color.WHITE);
                status.setForeground(Color.WHITE);
                message.setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                title.setForeground(new Color(0x21, 0x21, 0x21));
                status.setForeground(new Color(0x9D, 0x9D, 0x9D));
                message.setForeground(new Color(0x9D, 0x9D, 0x9D));
            }

            revalidate();
            return this;
        }
    }
}
