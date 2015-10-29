package im.actor.sdk;

import android.graphics.Color;

public class ActorStyle {
    private int toolBarColor = 0;
    private int fabColor = 0;
    private int fabColorPressed = 0;
    private int categoryTextColor = 0;
    private int recordIconTintColor = 0;
    private int avatarBackgroundColor = 0;
    private int actionShareColor = 0;
    private int actionAddContactColor = 0;
    private int contactFastTitleColor = 0;

    //Dialogs
    private int dialogsStatePendingColor = Color.parseColor("#40000000");
    private int dialogsStateSentColor = Color.parseColor("#40000000");
    private int dialogsStateDeliveredColor = Color.parseColor("#40000000");
    private int dialogsStateReadColor = Color.parseColor("#ff7ea8ef");
    private int dialogsStateErrorColor = Color.parseColor("#d24a43");
    private int dialogsCounterTextColor = 0;
    private int dialogsCounterBackgroundColor = 0;
    private int dialogsDivider = 0;
    private int dialogsTitle = 0;
    private int dialogsText = 0;
    private int dialogsTime = 0;
    private int dialogsTyping = 0;

    //Root colors
    private int mainColor = Color.parseColor("#4d74a6");
    private int mainBackground = Color.parseColor("#ffffff");
    private int backyardBackground = 0;

    private int primaryAlt = Color.parseColor("#4ca665");
    private int primaryAltHovered = Color.parseColor("#ff3d8652");
    private int primaryAltPressed = Color.parseColor("#ff5cca7b");

    //Dividers
    private int divider = Color.parseColor("#1e000000");
    private int divider_inv = Color.parseColor("#1effffff");

    //Text colors
    private int textPrimaryInv = Color.parseColor("#DEffffff");
    private int textPrimary = Color.parseColor("#DE000000");
    private int textSecondary = Color.parseColor("#7A000000");
    private int textHint = Color.parseColor("#42000000");
    private int textSubHeader = Color.parseColor("#6F000000");
    private int textSecondaryInv = Color.parseColor("#8Affffff");
    private int textHintInv = Color.parseColor("#42ffffff");
    private int textSubheaderInv = Color.parseColor("#8Fffffff");

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

    public int getMainBackground() {
        return mainBackground;
    }

    public void setMainBackground(int mainBackground) {
        this.mainBackground = mainBackground;
    }

    public int getBackyardBackground() {
        if (backyardBackground != 0) {
            return backyardBackground;
        } else {
            return getDarkenArgb(getMainBackground(), 0.9375);
        }
    }

    public void setBackyardBackground(int backyardBackground) {
        this.backyardBackground = backyardBackground;
    }

    public int getActionShareColor() {
        if (actionShareColor != 0) {
            return actionShareColor;
        } else {
            return getMainColor();
        }
    }

    public void setActionShareColor(int actionShareColor) {
        this.actionShareColor = actionShareColor;
    }

    public int getActionAddContactColor() {
        if (actionAddContactColor != 0) {
            return actionAddContactColor;
        } else {
            return getMainColor();
        }
    }

    public void setActionAddContactColor(int actionAddContactColor) {
        this.actionAddContactColor = actionAddContactColor;
    }

    public int getContactFastTitleColor() {
        if (contactFastTitleColor != 0) {
            return contactFastTitleColor;
        } else {
            return getMainColor();
        }
    }

    public void setContactFastTitleColor(int contactFastTitleColor) {
        this.contactFastTitleColor = contactFastTitleColor;
    }

    public int getDialogsCounterTextColor() {
        if (dialogsCounterTextColor != 0) {
            return dialogsCounterTextColor;
        } else {
            return getTextPrimaryInv();
        }
    }

    public void setDialogsCounterTextColor(int dialogsCounterTextColor) {
        this.dialogsCounterTextColor = dialogsCounterTextColor;
    }

    public int getDialogsCounterBackgroundColor() {
        if (dialogsCounterBackgroundColor != 0) {
            return dialogsCounterBackgroundColor;
        } else {
            return getPrimaryAlt();
        }
    }

    public void setDialogsCounterBackgroundColor(int dialogsCounterBackgroundColor) {
        this.dialogsCounterBackgroundColor = dialogsCounterBackgroundColor;
    }

    public int getTextPrimaryInv() {
        return textPrimaryInv;
    }

    public void setTextPrimaryInv(int textPrimaryInv) {
        this.textPrimaryInv = textPrimaryInv;
    }

    public int getPrimaryAlt() {
        return primaryAlt;
    }

    public void setPrimaryAlt(int primaryAlt) {
        this.primaryAlt = primaryAlt;
    }

    public int getPrimaryAltHovered() {
        return primaryAltHovered;
    }

    public void setPrimaryAltHovered(int primaryAltHovered) {
        this.primaryAltHovered = primaryAltHovered;
    }

    public int getPrimaryAltPressed() {
        return primaryAltPressed;
    }

    public void setPrimaryAltPressed(int primaryAltPressed) {
        this.primaryAltPressed = primaryAltPressed;
    }

    public int getDialogsStatePendingColor() {
        return dialogsStatePendingColor;
    }

    public void setDialogsStatePendingColor(int dialogsStatePendingColor) {
        this.dialogsStatePendingColor = dialogsStatePendingColor;
    }

    public int getDialogsStateSentColor() {
        return dialogsStateSentColor;
    }

    public void setDialogsStateSentColor(int dialogsStateSentColor) {
        this.dialogsStateSentColor = dialogsStateSentColor;
    }

    public int getDialogsStateDeliveredColor() {
        return dialogsStateDeliveredColor;
    }

    public void setDialogsStateDeliveredColor(int dialogsStateDeliveredColor) {
        this.dialogsStateDeliveredColor = dialogsStateDeliveredColor;
    }

    public int getDialogsStateReadColor() {
        return dialogsStateReadColor;
    }

    public void setDialogsStateReadColor(int dialogsStateReadColor) {
        this.dialogsStateReadColor = dialogsStateReadColor;
    }

    public int getDialogsStateErrorColor() {
        return dialogsStateErrorColor;
    }

    public void setDialogsStateErrorColor(int dialogsStateErrorColor) {
        this.dialogsStateErrorColor = dialogsStateErrorColor;
    }

    public int getDialogsDivider() {
        if (dialogsDivider != 0) {
            return dialogsDivider;
        } else {
            return getDividerColor();
        }
    }

    public void setDialogsDivider(int dialogsDivider) {
        this.dialogsDivider = dialogsDivider;
    }

    public int getDialogsTitle() {
        if (dialogsTitle != 0) {
            return dialogsTitle;
        } else {
            return getTextPrimary();
        }

    }

    public void setDialogsTitle(int dialogsTitle) {
        this.dialogsTitle = dialogsTitle;
    }

    public int getDialogsText() {
        if (dialogsText != 0) {
            return dialogsText;
        } else {
            return getTextSecondary();
        }
    }

    public void setDialogsText(int dialogsText) {
        this.dialogsText = dialogsText;
    }

    public int getDialogsTime() {
        if (dialogsTime != 0) {
            return dialogsTime;
        } else {
            return getTextSecondary();
        }
    }

    public void setDialogsTime(int dialogsTime) {
        this.dialogsTime = dialogsTime;
    }

    public int getDialogsTyping() {
        if (dialogsTyping != 0) {
            return dialogsTyping;
        } else {
            return getMainColor();
        }
    }

    public void setDialogsTyping(int dialogsTyping) {
        this.dialogsTyping = dialogsTyping;
    }

    public int getDividerColor() {
        return divider;
    }

    public int getDivider_inv() {
        return divider_inv;
    }

    public void setDivider_inv(int divider_inv) {
        this.divider_inv = divider_inv;
    }

    public int getDivider() {
        return divider;
    }

    public void setDivider(int divider) {
        this.divider = divider;
    }

    public int getTextPrimary() {
        return textPrimary;
    }

    public void setTextPrimary(int textPrimary) {
        this.textPrimary = textPrimary;
    }

    public int getTextSecondary() {
        return textSecondary;
    }

    public void setTextSecondary(int textSecondary) {
        this.textSecondary = textSecondary;
    }

    public int getTextHint() {
        return textHint;
    }

    public void setTextHint(int textHint) {
        this.textHint = textHint;
    }

    public int getTextSubHeader() {
        return textSubHeader;
    }

    public void setTextSubHeader(int textSubHeader) {
        this.textSubHeader = textSubHeader;
    }

    public int getTextSecondaryInv() {
        return textSecondaryInv;
    }

    public void setTextSecondaryInv(int textSecondaryInv) {
        this.textSecondaryInv = textSecondaryInv;
    }

    public int getTextHintInv() {
        return textHintInv;
    }

    public void setTextHintInv(int textHintInv) {
        this.textHintInv = textHintInv;
    }

    public int getTextSubheaderInv() {
        return textSubheaderInv;
    }

    public void setTextSubheaderInv(int textSubheaderInv) {
        this.textSubheaderInv = textSubheaderInv;
    }

    private int getDarkenArgb(int color, double percent) {
        return Color.argb(Color.alpha(color), (int) Math.round(Color.red(color) * percent), (int) Math.round(Color.green(color) * percent), (int) Math.round(Color.blue(color) * percent));
    }
}
