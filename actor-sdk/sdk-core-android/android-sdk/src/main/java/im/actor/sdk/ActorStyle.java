package im.actor.sdk;

import android.graphics.Color;

public class ActorStyle {
    private int toolBarColor = 0;
    private int fabColor = 0;
    private int fabColorPressed = 0;
    private int mainFabbg = Color.parseColor("#ccffffff");
    private int settingsCategoryTextColor = 0;
    private int recordIconTintColor = 0;
    private int avatarBackgroundColor = 0;
    private int actionShareColor = 0;

    //Root colors
    private int mainColor = Color.parseColor("#4d74a6");
    private int mainBackground = Color.parseColor("#ffffff");
    private int backyardBackground = 0;

    //Contacts
    private int contactFastTitleColor = 0;
    private int actionAddContactColor = 0;
    private int contactDivider = 0;

    //Dialogs
    private int dialogsStatePendingColor = Color.parseColor("#40000000");
    private int dialogsTime = 0;
    private int dialogsStateSentColor = Color.parseColor("#40000000");
    private int dialogsStateDeliveredColor = Color.parseColor("#40000000");
    private int dialogsStateReadColor = Color.parseColor("#ff7ea8ef");
    private int dialogsStateErrorColor = Color.parseColor("#d24a43");
    private int dialogsCounterTextColor = 0;
    private int dialogsCounterBackgroundColor = 0;
    private int dialogsDivider = 0;
    private int dialogsTitle = 0;
    private int dialogsText = 0;

    private int dialogsTyping = 0;

    private int primaryAlt = Color.parseColor("#4ca665");
    private int primaryAltHovered = Color.parseColor("#ff3d8652");
    private int primaryAltPressed = Color.parseColor("#ff5cca7b");

    private int accent = Color.parseColor("#d94335");
    private int accentHovered = Color.parseColor("#b3372c");
    private int accentPressed = Color.parseColor("#ca4a3f");

    //Base UI colors

    private int listAction = 0;
    private int sectonIconSmall = Color.parseColor("#b5b6b7");
    private int sectionIconLarge = 0;

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

    //Settings
    private int settingsMainTitle = 0;
    private int settingsIcon = 0;
    private int settingsTitle = 0;
    private int settingsSubtitle = 0;

    //Profile
    private int profileTitle = 0;
    private int profileSubtitle = 0;
    private int profilleIcon = 0;

    //Group Profile
    private int groupAdmin = 0;
    private int groupActionAddIcon = 0;
    private int groupActionAddText = 0;

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

    public int getSettingsCategoryTextColor() {
        if (settingsCategoryTextColor != 0) {
            return settingsCategoryTextColor;
        } else {
            return getMainColor();
        }
    }

    public void setSettingsCategoryTextColor(int settingsCategoryTextColor) {
        this.settingsCategoryTextColor = settingsCategoryTextColor;
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

    public int getAccent() {
        return accent;
    }

    public void setAccent(int accent) {
        this.accent = accent;
    }

    public int getAccentHovered() {
        return accentHovered;
    }

    public void setAccentHovered(int accentHovered) {
        this.accentHovered = accentHovered;
    }

    public int getAccentPressed() {
        return accentPressed;
    }

    public void setAccentPressed(int accentPressed) {
        this.accentPressed = accentPressed;
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

    public int getListAction() {
        if (listAction != 0) {
            return listAction;
        } else {
            return getMainColor();
        }
    }

    public void setListAction(int listAction) {
        this.listAction = listAction;
    }

    public int getSectonIconSmall() {
        return sectonIconSmall;
    }

    public void setSectonIconSmall(int sectonIconSmall) {
        this.sectonIconSmall = sectonIconSmall;
    }

    public int getSectionIconLarge() {
        if (sectionIconLarge != 0) {
            return sectionIconLarge;
        } else {
            return getMainColor();
        }
    }

    public void setSectionIconLarge(int sectionIconLarge) {
        this.sectionIconLarge = sectionIconLarge;
    }

    public int getSettingsMainTitle() {
        if (listAction != 0) {
            return listAction;
        } else {
            return getSettingsCategoryTextColor();
        }
    }

    public void setSettingsMainTitle(int settingsMainTitle) {
        this.settingsMainTitle = settingsMainTitle;
    }

    public int getSettingsIcon() {
        if (settingsIcon != 0) {
            return settingsIcon;
        } else {
            return getSectonIconSmall();
        }
    }

    public void setSettingsIcon(int settingsIcon) {
        this.settingsIcon = settingsIcon;
    }

    public int getSettingsTitle() {
        if (settingsTitle != 0) {
            return settingsTitle;
        } else {
            return getTextPrimary();
        }
    }

    public void setSettingsTitle(int settingsTitle) {
        this.settingsTitle = settingsTitle;
    }

    public int getSettingsSubtitle() {
        if (settingsSubtitle != 0) {
            return settingsSubtitle;
        } else {
            return getTextSecondary();
        }
    }

    public void setSettingsSubtitle(int settingsSubtitle) {
        this.settingsSubtitle = settingsSubtitle;
    }

    public int getProfileTitle() {
        if (profileTitle != 0) {
            return profileTitle;
        } else {
            return getTextPrimaryInv();
        }
    }

    public void setProfileTitle(int profileTitle) {
        this.profileTitle = profileTitle;
    }

    public int getProfileSubtitle() {
        if (profileSubtitle != 0) {
            return profileSubtitle;
        } else {
            return getTextSecondaryInv();
        }
    }

    public void setProfileSubtitle(int profileSubtitle) {
        this.profileSubtitle = profileSubtitle;
    }

    public int getProfilleIcon() {
        if (profilleIcon != 0) {
            return profilleIcon;
        } else {
            return getSectionIconLarge();
        }
    }

    public void setProfilleIcon(int profilleIcon) {
        this.profilleIcon = profilleIcon;
    }

    public int getGroupAdmin() {
        if (groupAdmin != 0) {
            return groupAdmin;
        } else {
            return getAccent();
        }
    }

    public void setGroupAdmin(int groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public int getGroupActionAddIcon() {
        if (groupActionAddIcon != 0) {
            return groupActionAddIcon;
        } else {
            return getListAction();
        }
    }

    public void setGroupActionAddIcon(int groupActionAddIcon) {
        this.groupActionAddIcon = groupActionAddIcon;
    }

    public int getGroupActionAddText() {
        if (groupActionAddText != 0) {
            return groupActionAddText;
        } else {
            return getListAction();
        }
    }

    public void setGroupActionAddText(int groupActionAddText) {
        this.groupActionAddText = groupActionAddText;
    }

    public int getContactDivider() {
        if (contactDivider != 0) {
            return contactDivider;
        } else {
            return getDivider();
        }
    }

    public void setContactDivider(int contactDivider) {
        this.contactDivider = contactDivider;
    }

    public int getMainFabbg() {
        return mainFabbg;
    }

    public void setMainFabbg(int mainFabbg) {
        this.mainFabbg = mainFabbg;
    }

    private int getDarkenArgb(int color, double percent) {
        return Color.argb(Color.alpha(color), (int) Math.round(Color.red(color) * percent), (int) Math.round(Color.green(color) * percent), (int) Math.round(Color.blue(color) * percent));
    }
}
