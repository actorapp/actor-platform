package im.actor.console;

import com.googlecode.lanterna.gui.Action;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.component.Label;
import com.googlecode.lanterna.gui.component.Panel;

public class WaitingDialog extends Window {
    private final Thread spinThread;
    private final Label spinLabel;
    private boolean isClosed;

    public WaitingDialog(String title, String description) {
        super(title);
        spinLabel = new Label("-");
        final Panel panel = new Panel(Panel.Orientation.HORISONTAL);
        panel.addComponent(new Label(description));
        panel.addComponent(spinLabel);
        addComponent(panel);

        isClosed = false;
        spinThread = new Thread(new SpinCode());
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        spinThread.start();
    }

    @Override
    public void close() {
        isClosed = true;
        super.close();
    }

    private class SpinCode implements Runnable {
        public void run() {
            while (!isClosed) {
                final String currentSpin = spinLabel.getText();
                final String nextSpin;
                if (currentSpin.equals("-"))
                    nextSpin = "\\";
                else if (currentSpin.equals("\\"))
                    nextSpin = "|";
                else if (currentSpin.equals("|"))
                    nextSpin = "/";
                else
                    nextSpin = "-";
                if (getOwner() != null) {
                    getOwner().runInEventThread(new Action() {
                        public void doAction() {
                            spinLabel.setText(nextSpin);
                        }
                    });
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
