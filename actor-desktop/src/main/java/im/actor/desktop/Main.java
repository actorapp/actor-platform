package im.actor.desktop;

import im.actor.desktop.engines.SwingListEngine;
import im.actor.model.Configuration;
import im.actor.model.storage.MemoryKeyValueEngine;
import im.actor.model.Messenger;
import im.actor.model.State;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.entity.*;
import im.actor.model.entity.Dialog;
import im.actor.model.jvm.JavaInit;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.Endpoints;
import im.actor.model.storage.EnginesFactory;

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
        JavaInit.init();
        Configuration configuration = new Configuration();
        configuration.setEndpoints(new Endpoints(new ConnectionEndpoint[]{
                new ConnectionEndpoint("mtproto-api.actor.im", 8080, ConnectionEndpoint.Type.TCP)
        }));
        configuration.setEnginesFactory(new EnginesFactory() {
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
            public KeyValueEngine<PendingMessage> pendingMessages(Peer peer) {
                return new MemoryKeyValueEngine<PendingMessage>();
            }
        });
        messenger = new Messenger(configuration);

        JFrame frame = new JFrame("Actor Messenger");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(740, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Color.WHITE);
        init(frame.getContentPane());
        frame.setVisible(true);
    }

    private static void init(final Container container) {
        if (messenger.getState() == State.AUTH_START) {
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
                    execute(messenger.getAuth().requestSms(Long.parseLong(textField.getText())), new CommandCallback<State>() {
                        @Override
                        public void onResult(State res) {
                            init(container);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            });
        } else if (messenger.getState() == State.CODE_VALIDATION) {
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
                    execute(messenger.getAuth().sendCode(textField.getText()), new CommandCallback<State>() {
                        @Override
                        public void onResult(State res) {
                            init(container);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            });
        } else if (messenger.getState() == State.SIGN_UP) {
            // TODO: Implement
        } else if (messenger.getState() == State.LOGGED_IN) {
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
