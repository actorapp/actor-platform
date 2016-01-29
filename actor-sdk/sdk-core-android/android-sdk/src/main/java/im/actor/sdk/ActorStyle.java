package im.actor.sdk;

import android.graphics.Color;

/**
 * Actor Styling class
 */
public class ActorStyle {

    private int[] defaultBackgrouds = new int[]{R.drawable.img_chat_background_default, R.drawable.bg_8, R.drawable.bg_2, R.drawable.bg_1};

    private int toolBarColor = 0;
    private int fabColor = 0;
    private int fabPressedColor = 0;
    private int mainFabbgColor = Color.parseColor("#ccffffff");
    private int settingsCategoryTextColor = 0;
    private int recordIconTintColor = 0;
    private int avatarBackgroundColor = 0;
    private int avatarBackgroundResource = 0;
    private int actionShareColor = 0;

    //Root colors
    private int mainColor = Color.parseColor("#4d74a6");
    private int mainBackgroundColor = Color.parseColor("#ffffff");
    private int backyardBackgroundColor = 0;

    //Contacts
    private int contactFastTitleColor = 0;
    private int actionAddContactColor = 0;
    private int contactDividerColor = 0;

    //Dialogs
    private int dialogsStatePendingColor = Color.parseColor("#40000000");
    private int dialogsTimeColor = 0;
    private int dialogsStateSentColor = Color.parseColor("#40000000");
    private int dialogsStateDeliveredColor = Color.parseColor("#40000000");
    private int dialogsStateReadColor = Color.parseColor("#ff7ea8ef");
    private int dialogsStateErrorColor = Color.parseColor("#d24a43");
    private int dialogsCounterTextColor = 0;
    private int dialogsCounterBackgroundColor = 0;
    private int dialogsDividerColor = 0;
    private int dialogsTitleColor = 0;
    private int dialogsTextColor = 0;

    private int dialogsTypingColor = 0;

    private int primaryAltColor = Color.parseColor("#4ca665");
    private int primaryAltHoveredColor = Color.parseColor("#ff3d8652");
    private int primaryAltPressedColor = Color.parseColor("#ff5cca7b");

    private int accentColor = Color.parseColor("#d94335");
    private int accentHoveredColor = Color.parseColor("#b3372c");
    private int accentPressedColor = Color.parseColor("#ca4a3f");

    //Base UI colors

    private int listActionColor = 0;
    private int sectonIconSmallColor = Color.parseColor("#b5b6b7");
    private int sectionIconLargeColor = 0;

    //Dividers
    private int dividerColor = Color.parseColor("#1e000000");
    private int dividerInvColor = Color.parseColor("#1effffff");

    //Text colors
    private int textPrimaryInvColor = Color.parseColor("#DEffffff");
    private int textPrimaryColor = Color.parseColor("#DE000000");
    private int textSecondaryColor = Color.parseColor("#7A000000");
    private int textSecondaryAccentColor = 0;
    private int textHintColor = Color.parseColor("#42000000");
    private int textSubHeaderColor = Color.parseColor("#6F000000");
    private int textSecondaryInvColor = Color.parseColor("#8Affffff");
    private int textHintInvColor = Color.parseColor("#42ffffff");
    private int textSubheaderInvColor = Color.parseColor("#8Fffffff");

    //Settings
    private int settingsMainTitleColor = 0;
    private int settingsIconColor = 0;
    private int settingsTitleColor = 0;
    private int settingsSubtitleColor = 0;

    //Profile
    private int profileTitleColor = 0;
    private int profileSubtitleColor = 0;
    private int profilleIconColor = 0;

    //Group Profile
    private int groupAdminColor = 0;
    private int groupActionAddIconColor = 0;
    private int groupActionAddTextColor = 0;
    private int groupOnlineColor = Color.parseColor("#ff7ea8ef");

    //Conversation
    private int convSendEnabledColor = 0;
    private int convSendDisabledColor = Color.parseColor("#42000000");

    private int convDateLineColor = Color.parseColor("#14000000");
    private int convDatetextColor = Color.parseColor("#ffffff");
    private int convDateBgColor = Color.parseColor("#99000000");

    private int convTextColor = 0;
    private int convTimeColor = Color.parseColor("#60000000");

    private int convStatePendingColor = Color.parseColor("#40000000");
    private int convStateSentColor = Color.parseColor("#40000000");
    private int convStateDeliveredColor = Color.parseColor("#40000000");
    private int convStateReadColor = Color.parseColor("#ff7ea8ef");
    private int convStateErrorColor = Color.parseColor("#d24a43");
    private int convStateWarrningColor = Color.parseColor("#d24a43");

    private int convMediaDateBgColor = Color.parseColor("#99000000");
    private int convMediaStatePendingColor = Color.parseColor("#ffffff");
    private int convMediaStateSentColor = Color.parseColor("#ffffff");
    private int convMediaStateDeliveredColor = Color.parseColor("#ffffff");
    private int convMediaStateReadColor = Color.parseColor("#ff7ea8ef");
    private int convMediaStateErrorColor = Color.parseColor("#ffed534b");
    private int convMediaStateWarrningColor = Color.parseColor("#ffed534b");
    private int convLikeColor = Color.parseColor("#e2264d");

    public int getMainColor() {
        return mainColor;
    }

    public int getMainPressedColor() {
        return getDarkenArgb(getMainColor(), 0.95);
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

    public int getFabPressedColor() {
        if (fabPressedColor != 0) {
            return fabPressedColor;
        } else {
            double percent = 0.95;
            return getDarkenArgb(getFabColor(), percent);
        }
    }

    public void setFabPressedColor(int fabPressedColor) {
        this.fabPressedColor = fabPressedColor;
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

    public void setAvatarBackgroundResource(int avatarBackgroundResource) {
        this.avatarBackgroundResource = avatarBackgroundResource;
    }

    public int getAvatarBackgroundResourse() {
        return avatarBackgroundResource;
    }

    public void setAvatarBackgroundColor(int avatarBackgroundColor) {
        this.avatarBackgroundColor = avatarBackgroundColor;
    }

    public int getMainBackgroundColor() {
        return mainBackgroundColor;
    }

    public void setMainBackgroundColor(int mainBackgroundColor) {
        this.mainBackgroundColor = mainBackgroundColor;
    }

    public int getBackyardBackgroundColor() {
        if (backyardBackgroundColor != 0) {
            return backyardBackgroundColor;
        } else {
            return getDarkenArgb(getMainBackgroundColor(), 0.9375);
        }
    }

    public void setBackyardBackgroundColor(int backyardBackgroundColor) {
        this.backyardBackgroundColor = backyardBackgroundColor;
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
            return getTextPrimaryInvColor();
        }
    }

    public void setDialogsCounterTextColor(int dialogsCounterTextColor) {
        this.dialogsCounterTextColor = dialogsCounterTextColor;
    }

    public int getDialogsCounterBackgroundColor() {
        if (dialogsCounterBackgroundColor != 0) {
            return dialogsCounterBackgroundColor;
        } else {
            return getPrimaryAltColor();
        }
    }

    public void setDialogsCounterBackgroundColor(int dialogsCounterBackgroundColor) {
        this.dialogsCounterBackgroundColor = dialogsCounterBackgroundColor;
    }

    public int getTextPrimaryInvColor() {
        return textPrimaryInvColor;
    }

    public void setTextPrimaryInvColor(int textPrimaryInvColor) {
        this.textPrimaryInvColor = textPrimaryInvColor;
    }

    public int getPrimaryAltColor() {
        return primaryAltColor;
    }

    public void setPrimaryAltColor(int primaryAltColor) {
        this.primaryAltColor = primaryAltColor;
    }

    public int getPrimaryAltHoveredColor() {
        return primaryAltHoveredColor;
    }

    public void setPrimaryAltHoveredColor(int primaryAltHoveredColor) {
        this.primaryAltHoveredColor = primaryAltHoveredColor;
    }

    public int getPrimaryAltPressedColor() {
        return primaryAltPressedColor;
    }

    public void setPrimaryAltPressedColor(int primaryAltPressedColor) {
        this.primaryAltPressedColor = primaryAltPressedColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    public int getAccentHoveredColor() {
        return accentHoveredColor;
    }

    public void setAccentHoveredColor(int accentHoveredColor) {
        this.accentHoveredColor = accentHoveredColor;
    }

    public int getAccentPressedColor() {
        return accentPressedColor;
    }

    public void setAccentPressedColor(int accentPressedColor) {
        this.accentPressedColor = accentPressedColor;
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

    public int getDialogsDividerColor() {
        if (dialogsDividerColor != 0) {
            return dialogsDividerColor;
        } else {
            return getDividerColor();
        }
    }

    public void setDialogsDividerColor(int dialogsDividerColor) {
        this.dialogsDividerColor = dialogsDividerColor;
    }

    public int getDialogsTitleColor() {
        if (dialogsTitleColor != 0) {
            return dialogsTitleColor;
        } else {
            return getTextPrimaryColor();
        }

    }

    public void setDialogsTitleColor(int dialogsTitleColor) {
        this.dialogsTitleColor = dialogsTitleColor;
    }

    public int getDialogsTextColor() {
        if (dialogsTextColor != 0) {
            return dialogsTextColor;
        } else {
            return getTextSecondaryColor();
        }
    }

    public void setDialogsTextColor(int dialogsTextColor) {
        this.dialogsTextColor = dialogsTextColor;
    }

    public int getDialogsTimeColor() {
        if (dialogsTimeColor != 0) {
            return dialogsTimeColor;
        } else {
            return getTextSecondaryColor();
        }
    }

    public void setDialogsTimeColor(int dialogsTimeColor) {
        this.dialogsTimeColor = dialogsTimeColor;
    }

    public int getDialogsTypingColor() {
        if (dialogsTypingColor != 0) {
            return dialogsTypingColor;
        } else {
            return getMainColor();
        }
    }

    public void setDialogsTypingColor(int dialogsTypingColor) {
        this.dialogsTypingColor = dialogsTypingColor;
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public int getDividerInvColor() {
        return dividerInvColor;
    }

    public void setDividerInvColor(int dividerInvColor) {
        this.dividerInvColor = dividerInvColor;
    }

    public void setDividerColor(int divider) {
        this.dividerColor = divider;
    }

    public int getTextPrimaryColor() {
        return textPrimaryColor;
    }

    public void setTextPrimaryColor(int textPrimaryColor) {
        this.textPrimaryColor = textPrimaryColor;
    }

    public int getTextSecondaryColor() {
        return textSecondaryColor;
    }

    public void setTextSecondaryColor(int textSecondaryColor) {
        this.textSecondaryColor = textSecondaryColor;
    }

    public int getTextSecondaryAccentColor() {
        if (textSecondaryAccentColor != 0) {
            return getTextSecondaryColor();
        } else {
            return textSecondaryAccentColor;
        }
    }

    public void setTextSecondaryAccentColor(int textSecondaryAccentColor) {
        this.textSecondaryAccentColor = textSecondaryAccentColor;
    }

    public int getTextHintColor() {
        return textHintColor;
    }

    public void setTextHintColor(int textHintColor) {
        this.textHintColor = textHintColor;
    }

    public int getTextSubHeaderColor() {
        return textSubHeaderColor;
    }

    public void setTextSubHeaderColor(int textSubHeaderColor) {
        this.textSubHeaderColor = textSubHeaderColor;
    }

    public int getTextSecondaryInvColor() {
        return textSecondaryInvColor;
    }

    public void setTextSecondaryInvColor(int textSecondaryInvColor) {
        this.textSecondaryInvColor = textSecondaryInvColor;
    }

    public int getTextHintInvColor() {
        return textHintInvColor;
    }

    public void setTextHintInvColor(int textHintInvColor) {
        this.textHintInvColor = textHintInvColor;
    }

    public int getTextSubheaderInvColor() {
        return textSubheaderInvColor;
    }

    public void setTextSubheaderInvColor(int textSubheaderInvColor) {
        this.textSubheaderInvColor = textSubheaderInvColor;
    }

    public int getListActionColor() {
        if (listActionColor != 0) {
            return listActionColor;
        } else {
            return getMainColor();
        }
    }

    public void setListActionColor(int listActionColor) {
        this.listActionColor = listActionColor;
    }

    public int getSectonIconSmallColor() {
        return sectonIconSmallColor;
    }

    public void setSectonIconSmallColor(int sectonIconSmallColor) {
        this.sectonIconSmallColor = sectonIconSmallColor;
    }

    public int getSectionIconLargeColor() {
        if (sectionIconLargeColor != 0) {
            return sectionIconLargeColor;
        } else {
            return getMainColor();
        }
    }

    public void setSectionIconLargeColor(int sectionIconLargeColor) {
        this.sectionIconLargeColor = sectionIconLargeColor;
    }

    public int getSettingsMainTitleColor() {
        if (listActionColor != 0) {
            return listActionColor;
        } else {
            return getSettingsCategoryTextColor();
        }
    }

    public void setSettingsMainTitleColor(int settingsMainTitleColor) {
        this.settingsMainTitleColor = settingsMainTitleColor;
    }

    public int getSettingsIconColor() {
        if (settingsIconColor != 0) {
            return settingsIconColor;
        } else {
            return getSectonIconSmallColor();
        }
    }

    public void setSettingsIconColor(int settingsIconColor) {
        this.settingsIconColor = settingsIconColor;
    }

    public int getSettingsTitleColor() {
        if (settingsTitleColor != 0) {
            return settingsTitleColor;
        } else {
            return getTextPrimaryColor();
        }
    }

    public void setSettingsTitleColor(int settingsTitleColor) {
        this.settingsTitleColor = settingsTitleColor;
    }

    public int getSettingsSubtitleColor() {
        if (settingsSubtitleColor != 0) {
            return settingsSubtitleColor;
        } else {
            return getTextSecondaryColor();
        }
    }

    public void setSettingsSubtitleColor(int settingsSubtitleColor) {
        this.settingsSubtitleColor = settingsSubtitleColor;
    }

    public int getProfileTitleColor() {
        if (profileTitleColor != 0) {
            return profileTitleColor;
        } else {
            return getTextPrimaryInvColor();
        }
    }

    public void setProfileTitleColor(int profileTitleColor) {
        this.profileTitleColor = profileTitleColor;
    }

    public int getProfileSubtitleColor() {
        if (profileSubtitleColor != 0) {
            return profileSubtitleColor;
        } else {
            return getTextSecondaryInvColor();
        }
    }

    public void setProfileSubtitleColor(int profileSubtitleColor) {
        this.profileSubtitleColor = profileSubtitleColor;
    }

    public int getProfilleIconColor() {
        if (profilleIconColor != 0) {
            return profilleIconColor;
        } else {
            return getSectionIconLargeColor();
        }
    }

    public void setProfilleIconColor(int profilleIconColor) {
        this.profilleIconColor = profilleIconColor;
    }

    public int getGroupAdminColor() {
        if (groupAdminColor != 0) {
            return groupAdminColor;
        } else {
            return getAccentColor();
        }
    }

    public void setGroupAdminColor(int groupAdminColor) {
        this.groupAdminColor = groupAdminColor;
    }

    public int getGroupActionAddIconColor() {
        if (groupActionAddIconColor != 0) {
            return groupActionAddIconColor;
        } else {
            return getListActionColor();
        }
    }

    public void setGroupActionAddIconColor(int groupActionAddIconColor) {
        this.groupActionAddIconColor = groupActionAddIconColor;
    }

    public int getGroupActionAddTextColor() {
        if (groupActionAddTextColor != 0) {
            return groupActionAddTextColor;
        } else {
            return getListActionColor();
        }
    }

    public void setGroupActionAddTextColor(int groupActionAddTextColor) {
        this.groupActionAddTextColor = groupActionAddTextColor;
    }

    public int getContactDividerColor() {
        if (contactDividerColor != 0) {
            return contactDividerColor;
        } else {
            return getDividerColor();
        }
    }

    public void setContactDividerColor(int contactDividerColor) {
        this.contactDividerColor = contactDividerColor;
    }

    public int getMainFabbgColor() {
        return mainFabbgColor;
    }

    public void setMainFabbgColor(int mainFabbgColor) {
        this.mainFabbgColor = mainFabbgColor;
    }

    public int getConvSendEnabledColor() {
        if (convSendEnabledColor != 0) {
            return convSendEnabledColor;
        } else {
            return getMainColor();
        }
    }

    public void setConvSendEnabledColor(int convSendEnabledColor) {
        this.convSendEnabledColor = convSendEnabledColor;
    }

    public int getConvSendDisabledColor() {
        return convSendDisabledColor;
    }

    public void setConvSendDisabledColor(int convSendDisabledColor) {
        this.convSendDisabledColor = convSendDisabledColor;
    }

    public int getConvDateLineColor() {
        return convDateLineColor;
    }

    public void setConvDateLineColor(int convDateLineColor) {
        this.convDateLineColor = convDateLineColor;
    }

    public int getConvDatetextColor() {
        return convDatetextColor;
    }

    public void setConvDatetextColor(int convDatetextColor) {
        this.convDatetextColor = convDatetextColor;
    }

    public int getConvDateBgColor() {
        return convDateBgColor;
    }

    public void setConvDateBgColor(int convDateBgColor) {
        this.convDateBgColor = convDateBgColor;
    }

    public int getConvTextColor() {
        if (convTextColor != 0) {
            return convTextColor;
        } else {
            return getTextPrimaryColor();
        }
    }

    public void setConvTextColor(int convTextColor) {
        this.convTextColor = convTextColor;
    }

    public int getConvTimeColor() {
        return convTimeColor;
    }

    public void setConvTimeColor(int convTimeColor) {
        this.convTimeColor = convTimeColor;
    }

    public int getConvStatePendingColor() {
        return convStatePendingColor;
    }

    public void setConvStatePendingColor(int convStatePendingColor) {
        this.convStatePendingColor = convStatePendingColor;
    }

    public int getConvStateSentColor() {
        return convStateSentColor;
    }

    public void setConvStateSentColor(int convStateSentColor) {
        this.convStateSentColor = convStateSentColor;
    }

    public int getConvStateDeliveredColor() {
        return convStateDeliveredColor;
    }

    public void setConvStateDeliveredColor(int convStateDeliveredColor) {
        this.convStateDeliveredColor = convStateDeliveredColor;
    }

    public int getConvStateReadColor() {
        return convStateReadColor;
    }

    public void setConvStateReadColor(int convStateReadColor) {
        this.convStateReadColor = convStateReadColor;
    }

    public int getConvStateErrorColor() {
        return convStateErrorColor;
    }

    public void setConvStateErrorColor(int convStateErrorColor) {
        this.convStateErrorColor = convStateErrorColor;
    }

    public int getConvStateWarrningColor() {
        return convStateWarrningColor;
    }

    public void setConvStateWarrningColor(int convStateWarrningColor) {
        this.convStateWarrningColor = convStateWarrningColor;
    }

    public int getConvMediaDateBgColor() {
        return convMediaDateBgColor;
    }

    public void setConvMediaDateBgColor(int convMediaDateBgColor) {
        this.convMediaDateBgColor = convMediaDateBgColor;
    }

    public int getConvMediaStatePendingColor() {
        return convMediaStatePendingColor;
    }

    public void setConvMediaStatePendingColor(int convMediaStatePendingColor) {
        this.convMediaStatePendingColor = convMediaStatePendingColor;
    }

    public int getConvMediaStateSentColor() {
        return convMediaStateSentColor;
    }

    public void setConvMediaStateSentColor(int convMediaStateSentColor) {
        this.convMediaStateSentColor = convMediaStateSentColor;
    }

    public int getConvMediaStateDeliveredColor() {
        return convMediaStateDeliveredColor;
    }

    public void setConvMediaStateDeliveredColor(int convMediaStateDeliveredColor) {
        this.convMediaStateDeliveredColor = convMediaStateDeliveredColor;
    }

    public int getConvMediaStateReadColor() {
        return convMediaStateReadColor;
    }

    public void setConvMediaStateReadColor(int convMediaStateReadColor) {
        this.convMediaStateReadColor = convMediaStateReadColor;
    }

    public int getConvMediaStateErrorColor() {
        return convMediaStateErrorColor;
    }

    public void setConvMediaStateErrorColor(int convMediaStateErrorColor) {
        this.convMediaStateErrorColor = convMediaStateErrorColor;
    }

    public int getConvMediaStateWarrningColor() {
        return convMediaStateWarrningColor;
    }

    public void setConvMediaStateWarrningColor(int convMediaStateWarrningColor) {
        this.convMediaStateWarrningColor = convMediaStateWarrningColor;
    }

    public static int getDarkenArgb(int color, double percent) {
        return Color.argb(Color.alpha(color), (int) Math.round(Color.red(color) * percent), (int) Math.round(Color.green(color) * percent), (int) Math.round(Color.blue(color) * percent));
    }

    public int getGroupOnlineColor() {
        return groupOnlineColor;
    }

    public void setGroupOnlineColor(int groupOnlineColor) {
        this.groupOnlineColor = groupOnlineColor;
    }

    public int getConvLikeColor() {
        return convLikeColor;
    }

    public void setConvLikeColor(int convLikeColor) {
        this.convLikeColor = convLikeColor;
    }

    public int[] getDefaultBackgrouds() {
        return defaultBackgrouds;
    }

    public void setDefaultBackgrouds(int[] defaultBackgrouds) {
        this.defaultBackgrouds = defaultBackgrouds;
    }
}
