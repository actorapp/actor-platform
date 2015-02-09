package im.actor.console;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.*;
import com.googlecode.lanterna.gui.component.ActionListBox;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.gui.dialog.TextInputDialog;
import com.googlecode.lanterna.gui.layout.LinearLayout;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;
import im.actor.model.Configuration;
import im.actor.model.Messenger;
import im.actor.model.State;
import im.actor.model.concurrency.Command;
import im.actor.model.concurrency.CommandCallback;
import im.actor.model.concurrency.MainThread;
import im.actor.model.jvm.JavaInit;
import im.actor.model.network.ConnectionEndpoint;
import im.actor.model.network.Endpoints;

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
            executeCommand(messenger.getAuth().sendCode(res), new CommandCallback<State>() {
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

            leftPanel.addComponent(new Label("Recent", 30));
            ActionListBox listBox = new ActionListBox();
            leftPanel.addComponent(listBox);
            listBox.addAction("Title", null);
            listBox.addAction("Title1", null);
            listBox.addAction("Title2", null);
            listBox.addAction("Title3", null);
            listBox.addAction("Title4", null);
            listBox.addAction("Title5", null);
            listBox.addAction("Title6", null);
            listBox.addAction("Title7", null);
            listBox.addAction("Title8", null);
            listBox.addAction("Title9", null);
            listBox.addAction("Title10", null);
            listBox.addAction("Title11", null);
            listBox.addAction("Title12", null);
            listBox.addAction("Title13", null);
            listBox.addAction("Title14", null);
            listBox.addAction("Title15", null);
            listBox.addAction("Title16", null);
            listBox.addAction("Title17", null);
            listBox.addAction("Title18", null);

            horisontalPanel.addComponent(leftPanel, LinearLayout.MAXIMIZES_VERTICALLY);

            addComponent(horisontalPanel);
        }
    }
}
