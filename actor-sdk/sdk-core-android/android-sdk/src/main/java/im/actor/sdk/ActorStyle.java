package im.actor.sdk;

import android.graphics.Color;

public class ActorStyle {
    private int mainColor = 0;
    private int toolBarColor = 0;
    private int fabColor = 0;
    private int fabColorPressed = 0;

    public int getMainColor() {
        return mainColor;
    }

    public void setMainColor(int mainColor) {
        this.mainColor = mainColor;
    }

    public int getToolBarColor() {
        if (toolBarColor != 0) {
            return toolBarColor;
        } else {
            return getMainColor();
        }
    }

    public void setToolBarColor(int toolBarColor) {
        this.toolBarColor = toolBarColor;
    }

    public int getFabColor() {
        if (fabColor != 0) {
            return fabColor;
        } else {
            return getMainColor();
        }
    }

    public void setFabColor(int fabColor) {
        this.fabColor = fabColor;
    }

    public int getFabColorPressed() {
        if (fabColorPressed != 0) {
            return fabColorPressed;
        } else {
            double percent = 0.95;
            return getDarkenArgb(getFabColor(), percent);
        }
    }

    public void setFabColorPressed(int fabColorPressed) {
        this.fabColorPressed = fabColorPressed;
    }

    private int getDarkenArgb(int color, double percent) {
        return Color.argb(Color.alpha(color), (int) Math.round(Color.red(color) * percent), (int) Math.round(Color.green(color) * percent), (int) Math.round(Color.blue(color) * percent));
    }
}
