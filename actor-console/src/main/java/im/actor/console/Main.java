package im.actor.console;

import im.actor.model.droidkit.bser.Bser;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.*;
import com.googlecode.lanterna.gui.component.AbstractComponent;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;
import com.googlecode.lanterna.gui.layout.LinearLayout;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.ScreenCharacterStyle;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.TerminalPosition;
import com.googlecode.lanterna.terminal.TerminalSize;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;

import im.actor.console.entity.UserEntity;
import im.actor.model.Configuration;
import im.actor.model.Messenger;
import im.actor.model.State;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.concurrency.MainThread;
import im.actor.model.entity.*;
import im.actor.model.entity.content.TextContent;
import im.actor.model.jvm.JavaInit;
import im.actor.model.mvvm.KeyValueEngine;
import im.actor.model.mvvm.ListEngine;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.Endpoints;
import im.actor.model.storage.EnginesFactory;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 08.02.15.
 */
public class Main {

    private static Messenger messenger;
    private static GUIScreen gui;

    public static void main(String[] args) {

        SwingTerminal terminal = TerminalFacade.createSwingTerminal();
        Screen screen = new Screen(terminal);
        gui = new GUIScreen(screen, new DefaultBackgroundRenderer());
        screen.startScreen();

        JavaInit.init();

        DB db = DBMaker.newFileDB(new File("prefs.db")).make();

        Configuration configuration = new Configuration();
        configuration.setEndpoints(new Endpoints(new ConnectionEndpoint[]{
                new ConnectionEndpoint("mtproto-api.actor.im", 8080, ConnectionEndpoint.Type.TCP)
        }));
        configuration.setMainThread(new MainThread() {
            @Override
            public void runOnUiThread(final Runnable runnable) {
                gui.runInEventThread(new Action() {
                    @Override
                    public void doAction() {
                        runnable.run();
                    }
                });
            }
        });
        configuration.setPreferencesStorage(new MapDbPreferences(db));
        configuration.setEnginesFactory(new EnginesFactory() {
            @Override
            public KeyValueEngine<User> createUsersEngine() {
                return new MapDbKeyValueEngine<User>(DBMaker.newFileDB(new File("users.db")).make(),
                        new MapDbKeyValueEngine.Serializer<User>() {
                            @Override
                            public byte[] serialize(User user) {
                                return new UserEntity(user).toByteArray();
                            }

                            @Override
                            public User deserialize(byte[] v) {
                                try {
                                    return Bser.parse(new UserEntity(), v).getUser();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        });
            }

            @Override
            public ListEngine<Dialog> createDialogsEngine() {
                return new MemoryListEngine<Dialog>();
            }

            @Override
            public ListEngine<Message> createMessagesEngine(Peer peer) {
                return new MemoryListEngine<Message>();
            }

            @Override
            public KeyValueEngine<PendingMessage> pendingMessages(Peer peer) {
                return null;
            }
        });

        messenger = new Messenger(configuration);

        updateMainUi();
    }

    public static void updateMainUi() {
        if (messenger.getState() == State.AUTH_START) {
            String res = TextInputDialog.showTextInputBox(gui, "Validate Phone", "Please, set your phone number", "");

            long phone;
            try {
                phone = Long.parseLong(res);
            } catch (Exception e) {
                MessageBox.showMessageBox(gui, "Incorrect phone", null);
                updateMainUi();
                return;
            }

            executeCommand(messenger.getAuth().requestSms(phone), new CommandCallback<State>() {
                @Override
                public void onResult(State res) {
                    updateMainUi();
                }

                @Override
                public void onError(Exception e) {
                    MessageBox.showMessageBox(gui, "Unable to register phone", "Error: " + e);
                    updateMainUi();
                }
            });
        } else if (messenger.getState() == State.CODE_VALIDATION) {
            String res = TextInputDialog.showTextInputBox(gui, "Validate Phone", "Please, enter activation code", "");
            executeCommand(messenger.getAuth().sendCode(Integer.parseInt(res)), new CommandCallback<State>() {
                @Override
                public void onResult(State res) {
                    updateMainUi();
                }

                @Override
                public void onError(Exception e) {
                    MessageBox.showMessageBox(gui, "Unable to register phone", "Error: " + e);
                    updateMainUi();
                }
            });
        } else if (messenger.getState() == State.SIGN_UP) {
            MessageBox.showMessageBox(gui, "Need signup", "Please, perform signup on phone");
        } else if (messenger.getState() == State.LOGGED_IN) {
            gui.showWindow(new MainWindow(), GUIScreen.Position.FULL_SCREEN);
        }
    }

    public static <T> void executeCommand(Command<T> execute, final CommandCallback<T> callback) {
        final WaitingDialog waitingDialog = new WaitingDialog("Actor Systems", "Loading...");
        execute.start(new CommandCallback<T>() {
            @Override
            public void onResult(T res) {
                waitingDialog.close();
                callback.onResult(res);
            }

            @Override
            public void onError(Exception e) {
                waitingDialog.close();
                callback.onError(e);
            }
        });
        gui.showWindow(waitingDialog, GUIScreen.Position.CENTER);
    }

    public static class MainWindow extends Window {

        public MainWindow() {
            super("Actor Messenger");

            setBorder(new Border.Invisible());

            Panel horisontalPanel = new Panel(new Border.Invisible(), Panel.Orientation.HORISONTAL);
            Panel leftPanel = new Panel(new Border.Invisible(), Panel.Orientation.VERTICAL);
            final Panel rightPanel = new Panel(new Border.Invisible(), Panel.Orientation.VERTICAL);

            leftPanel.addComponent(new Label("Recent", 30));
//            leftPanel.addComponent(new DialogElement(new Dialog(new Peer(PeerType.EMAIL, 0), 0,
//                    "???", 0, 0, 0, "!!!!", null, 0, 0, 0)));

            final MemoryListEngine<Dialog> dialogEngine = ((MemoryListEngine<Dialog>) messenger.getDialogs());

            ArrayList<Dialog> d = new ArrayList<Dialog>();
//            d.add(new Dialog(new Peer(PeerType.EMAIL, 0), 0,
//                    "???", 0, 0, 0, "!!!!", null, 0, 0, 0));
            final ConsoleList<Dialog> dialogConsoleList = new ConsoleList<Dialog>(leftPanel, d, new ConsoleList.Adapter<Dialog>() {
                @Override
                public Component createComponent(Dialog item) {
                    return new DialogElement(item);
                }
            });

            final ConsoleList<Message> messagesList = new ConsoleList<Message>(rightPanel, new ArrayList<Message>(), new ConsoleList.Adapter<Message>() {
                @Override
                public Component createComponent(Message item) {
                    return new MessageElement(item);
                }
            });

            dialogEngine.addListener(new MemoryListEngine.EngineListener() {
                @Override
                public void onItemsChanged() {
                    final List<Dialog> itms = dialogEngine.getList();
                    messenger.getConfiguration().getMainThread().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (itms.size() > 0) {
                                Dialog dialog = itms.get(0);
                                messagesList.update(((MemoryListEngine<Message>)
                                        messenger.getMessages(dialog.getPeer())).getList());
                                messenger.getMessages(dialog.getPeer());
                            }
                            dialogConsoleList.update(itms);
                        }
                    });

                }
            });

            horisontalPanel.addComponent(leftPanel, LinearLayout.MAXIMIZES_VERTICALLY);
            horisontalPanel.addComponent(rightPanel, LinearLayout.MAXIMIZES_VERTICALLY, LinearLayout.MAXIMIZES_HORIZONTALLY);

            addComponent(horisontalPanel);


        }
    }

    public static class DialogElement extends AbstractComponent {

        private Dialog dialog;

        public DialogElement(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        protected TerminalSize calculatePreferredSize() {
            return new TerminalSize(30, 4);
        }

        @Override
        public void repaint(TextGraphics graphics) {
            graphics.setBackgroundColor(Terminal.Color.BLACK);
            graphics.setForegroundColor(Terminal.Color.WHITE);
            graphics.fillRectangle(' ', new TerminalPosition(0, 0), new TerminalSize(30, 4));

            graphics.setForegroundColor(Terminal.Color.WHITE);
            String title = dialog.getDialogTitle();
            if (dialog.getDialogAvatar() != null) {
                title = dialog.getDialogAvatar().getSmallImage().getFileLocation().getFileId() +
                        "# " + title;
            }
            graphics.drawString(2, 1, trim(title, 25), ScreenCharacterStyle.Bold);

            graphics.setForegroundColor(Terminal.Color.GREEN);
            graphics.drawString(27, 1, "\u2713");

            graphics.setForegroundColor(Terminal.Color.WHITE);
            graphics.drawString(2, 2, trim("\u25B6 " + dialog.getText(), 27));
        }
    }

    public static class MessageElement extends Panel {

        public MessageElement(Message message) {
            User user = messenger.getUsers().getValue(message.getSenderId());
            addComponent(new Label(user.getName() + " says:"));
            addComponent(new Label(((TextContent) message.getContent()).getText()));
        }
    }

    private static String trim(String s, int len) {
        if (s.length() > len) {
            int nLen = Math.min(len - 4, s.length());
            return s.substring(0, nLen) + "...";
        }
        return s;
    }
}
