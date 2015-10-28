package im.actor.sdk;

import android.graphics.Color;

public class ActorStyle {
    private int mainColor = Color.parseColor("#4d74a6");
    private int toolBarColor = 0;
    private int fabColor = 0;
    private int fabColorPressed = 0;
    private int categoryTextColor = 0;
    private int recordIconTintColor = 0;
    private int avatarBackgroundColor = 0;

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

    public int getCategoryTextColor() {
        if (categoryTextColor != 0) {
            return categoryTextColor;
        } else {
            return getMainColor();
        }
    }

    public void setCategoryTextColor(int categoryTextColor) {
        this.categoryTextColor = categoryTextColor;
    }

    public int getRecordIconTintColor() {
        if (recordIconTintColor != 0) {
            return recordIconTintColor;
        } else {
            return getMainColor();
        }
    }

    public void setRecordIconTintColor(int recordIconTintColor) {
        this.recordIconTintColor = recordIconTintColor;
    }

    public int getAvatarBackgroundColor() {
        if (avatarBackgroundColor != 0) {
            return avatarBackgroundColor;
        } else {
            return getMainColor();
        }
    }

    public void setAvatarBackgroundColor(int avatarBackgroundColor) {
        this.avatarBackgroundColor = avatarBackgroundColor;
    }



    private int getDarkenArgb(int color, double percent) {
        return Color.argb(Color.alpha(color), (int) Math.round(Color.red(color) * percent), (int) Math.round(Color.green(color) * percent), (int) Math.round(Color.blue(color) * percent));
    }
}
