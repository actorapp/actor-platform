package im.actor.desktop;

import im.actor.desktop.engines.SwingListEngine;
import im.actor.model.AuthState;
import im.actor.model.ConfigurationBuilder;
import im.actor.model.MessengerCallback;
import im.actor.model.jvm.JavaLog;
import im.actor.model.jvm.JavaNetworking;
import im.actor.model.jvm.JavaThreading;
import im.actor.model.modules.messages.entity.OutUnreadMessage;
import im.actor.model.storage.MemoryKeyValueEngine;
import im.actor.model.Messenger;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.*;
import im.actor.model.entity.Dialog;
import im.actor.model.storage.KeyValueEngine;
import im.actor.model.storage.ListEngine;
import im.actor.model.Storage;
import im.actor.model.storage.MemoryPreferences;
import im.actor.model.storage.PreferencesStorage;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static im.actor.desktop.UiTools.center;
import static im.actor.desktop.UiTools.replace;

/**
 * Created by ex3ndr on 11.02.15.
 */
public class Main {
    private static Messenger messenger;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setThreading(new JavaThreading());
        builder.setNetworking(new JavaNetworking());
        builder.setLog(new JavaLog());
        builder.setStorage(new Storage() {
            @Override
            public PreferencesStorage createPreferencesStorage() {
                return new MemoryPreferences();
            }

            @Override
            public KeyValueEngine<User> createUsersEngine() {
                return new MemoryKeyValueEngine<User>();
            }

            @Override
            public ListEngine<Dialog> createDialogsEngine() {
                return new SwingListEngine<Dialog>();
            }

            @Override
            public ListEngine<Message> createMessagesEngine(Peer peer) {
                return new SwingListEngine<Message>();
            }

            @Override
            public KeyValueEngine<OutUnreadMessage> pendingMessages(Peer peer) {
                return new MemoryKeyValueEngine<OutUnreadMessage>();
            }
        });
        builder.setCallback(new MessengerCallback() {
            @Override
            public void onUserOnline(int uid) {

            }

            @Override
            public void onUserOffline(int uid) {

            }

            @Override
            public void onUserLastSeen(int uid, long lastSeen) {

            }

            @Override
            public void onGroupOnline(int gid, int count) {

            }

            @Override
            public void onTypingStart(int uid) {

            }

            @Override
            public void onTypingEnd(int uid) {

            }

            @Override
            public void onGroupTyping(int gid, int[] uids) {

            }
        });
        builder.addEndpoint("tcp://mtproto-api.actor.im:8080");

        messenger = new Messenger(builder.build());

        JFrame frame = new JFrame("Actor Messenger");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(740, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Color.WHITE);
        init(frame.getContentPane());
        frame.setVisible(true);
    }

    private static void init(final Container container) {
        if (messenger.getAuthState() == AuthState.AUTH_START) {
            JPanel authPanel = linearLayout();
            authPanel.setPreferredSize(new Dimension(480, 300));

            final JLabel title = new JLabel("Please, enter your phone number");
            final JTextField textField = new JTextField();
            final JButton button = new JButton("Next");

            authPanel.add(title);
            authPanel.add(textField);
            authPanel.add(button);

            replace(container, center(authPanel));

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    execute(messenger.requestSms(Long.parseLong(textField.getText())), new CommandCallback<AuthState>() {
                        @Override
                        public void onResult(AuthState res) {
                            init(container);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            });
        } else if (messenger.getAuthState() == AuthState.CODE_VALIDATION) {
            JPanel authPanel = linearLayout();
            authPanel.setPreferredSize(new Dimension(480, 300));

            final Label title = new Label("Activation code");
            final TextField textField = new TextField();
            final Button button = new Button("Next");

            authPanel.add(title);
            authPanel.add(textField);
            authPanel.add(button);

            replace(container, center(authPanel));

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    execute(messenger.sendCode(Integer.parseInt(textField.getText())), new CommandCallback<AuthState>() {
                        @Override
                        public void onResult(AuthState res) {
                            init(container);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            });
        } else if (messenger.getAuthState() == AuthState.SIGN_UP) {
            // TODO: Implement
        } else if (messenger.getAuthState() == AuthState.LOGGED_IN) {
            new MainPanel(container, messenger);
            // ChatForm form = new ChatForm();
            // container.add(new ChatForm());
        } else {
            throw new RuntimeException("Unknown state");
        }
    }

    private static <T> void execute(Command<T> c, CommandCallback<T> res) {
        c.start(res);
    }

    private static JPanel linearLayout() {
        JPanel authPanel = new JPanel();
        //authPanel.setLayout(new BoxLayout(authPanel, BoxLayout.Y_AXIS));
        authPanel.setLayout(new BoxLayout(authPanel, BoxLayout.Y_AXIS));
        return authPanel;
    }
}
