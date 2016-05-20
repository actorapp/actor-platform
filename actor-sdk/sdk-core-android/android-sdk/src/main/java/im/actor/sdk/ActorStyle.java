package im.actor.sdk;

import android.graphics.Color;

import im.actor.sdk.util.Screen;

/**
 * Actor Styling class
 */
@SuppressWarnings("unused")
public class ActorStyle {

    //////////////////////////
    //     BACKGROUNDS      //
    //////////////////////////

    private int[] defaultBackgrouds = new int[]{R.drawable.img_chat_background_default, R.drawable.bg_1, R.drawable.bg_2, R.drawable.bg_3};

    public int[] getDefaultBackgrouds() {
        return defaultBackgrouds;
    }

    public void setDefaultBackgrouds(int[] defaultBackgrouds) {
        this.defaultBackgrouds = defaultBackgrouds;
    }

    //////////////////////////
    //        COLORS        //
    //////////////////////////

    //
    // Global colors
    //

    //main color
    private int mainColor = 0xff4d74a6;

    public int getMainColor() {
        return mainColor;
    }

    public void setMainColor(int mainColor) {
        this.mainColor = mainColor;
    }

    // primary alt colors
    private int primaryAltColor = 0xff4ca665;

    public int getPrimaryAltColor() {
        return primaryAltColor;
    }

    public void setPrimaryAltColor(int primaryAltColor) {
        this.primaryAltColor = primaryAltColor;
    }

    private int primaryAltHoveredColor = 0xff3d8652;

    public int getPrimaryAltHoveredColor() {
        return primaryAltHoveredColor;
    }

    public void setPrimaryAltHoveredColor(int primaryAltHoveredColor) {
        this.primaryAltHoveredColor = primaryAltHoveredColor;
    }

    private int primaryAltPressedColor = 0xff5cca7b;

    public int getPrimaryAltPressedColor() {
        return primaryAltPressedColor;
    }

    public void setPrimaryAltPressedColor(int primaryAltPressedColor) {
        this.primaryAltPressedColor = primaryAltPressedColor;
    }

    // accent colors
    private int accentColor = 0xffd94335;

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    private int accentHoveredColor = 0xffb3372c;

    public int getAccentHoveredColor() {
        return accentHoveredColor;
    }

    public void setAccentHoveredColor(int accentHoveredColor) {
        this.accentHoveredColor = accentHoveredColor;
    }

    private int accentPressedColor = 0xffca4a3f;

    public int getAccentPressedColor() {
        return accentPressedColor;
    }

    public void setAccentPressedColor(int accentPressedColor) {
        this.accentPressedColor = accentPressedColor;
    }

    //
    // Shared UI colors
    //

    private int toolBarColor = 0;

    public int getToolBarColor() {
        return getColorWithFallback(toolBarColor, getMainColor());
    }

    public void setToolBarColor(int toolBarColor) {
        this.toolBarColor = toolBarColor;
    }

    private int fabColor = 0;

    public int getFabColor() {
        return getColorWithFallback(fabColor, getMainColor());
    }

    public void setFabColor(int fabColor) {
        this.fabColor = fabColor;
    }

    private int fabPressedColor = 0;

    public int getFabPressedColor() {
        return getColorWithFallback(fabPressedColor, getDarkenArgb(getFabColor(), 0.95));
    }

    public void setFabPressedColor(int fabPressedColor) {
        this.fabPressedColor = fabPressedColor;
    }

    private int mainFabbgColor = 0xccffffff;

    public int getMainFabbgColor() {
        return mainFabbgColor;
    }

    public void setMainFabbgColor(int mainFabbgColor) {
        this.mainFabbgColor = mainFabbgColor;
    }

    private int actionShareColor = 0;

    public int getActionShareColor() {
        return getColorWithFallback(actionShareColor, getMainColor());
    }

    public void setActionShareColor(int actionShareColor) {
        this.actionShareColor = actionShareColor;
    }

    private int mainBackgroundColor = 0xffffffff;

    public int getMainBackgroundColor() {
        return mainBackgroundColor;
    }

    public void setMainBackgroundColor(int mainBackgroundColor) {
        this.mainBackgroundColor = mainBackgroundColor;
    }

    private int backyardBackgroundColor = 0;

    public int getBackyardBackgroundColor() {
        return getColorWithFallback(backyardBackgroundColor, getDarkenArgb(getMainBackgroundColor(), 0.9375));
    }

    public void setBackyardBackgroundColor(int backyardBackgroundColor) {
        this.backyardBackgroundColor = backyardBackgroundColor;
    }

    private int avatarBackgroundColor = 0;

    public void setAvatarBackgroundColor(int avatarBackgroundColor) {
        this.avatarBackgroundColor = avatarBackgroundColor;
    }

    public int getAvatarBackgroundColor() {
        return getColorWithFallback(avatarBackgroundColor, getMainColor());
    }

    private int avatarBackgroundResource = 0;

    public int getAvatarBackgroundResourse() {
        return avatarBackgroundResource;
    }

    public void setAvatarBackgroundResource(int avatarBackgroundResource) {
        this.avatarBackgroundResource = avatarBackgroundResource;
    }

    private int verifiedColor = 0;

    public int getVerifiedColor() {
        return getColorWithFallback(verifiedColor, getProfileTitleColor());
    }

    public void setVerifiedColor(int verifiedColor) {
        this.verifiedColor = verifiedColor;
    }

    //
    // List Styles
    //

    // Shared lists colors
    private int listActionColor = 0;

    public int getListActionColor() {
        return getColorWithFallback(listActionColor, getMainColor());
    }

    public void setListActionColor(int listActionColor) {
        this.listActionColor = listActionColor;
    }

    //Contacts
    private int contactFastTitleColor = 0;

    public int getContactFastTitleColor() {
        return getColorWithFallback(contactFastTitleColor, getMainColor());
    }

    public void setContactFastTitleColor(int contactFastTitleColor) {
        this.contactFastTitleColor = contactFastTitleColor;
    }

    private int actionAddContactColor = 0;

    public int getActionAddContactColor() {
        return getColorWithFallback(actionAddContactColor, getMainColor());
    }

    public void setActionAddContactColor(int actionAddContactColor) {
        this.actionAddContactColor = actionAddContactColor;
    }

    private int contactDividerColor = 0;

    public int getContactDividerColor() {
        return getColorWithFallback(contactDividerColor, getDividerColor());
    }

    public void setContactDividerColor(int contactDividerColor) {
        this.contactDividerColor = contactDividerColor;
    }

    //Dialogs
    private int dialogsStatePendingColor = 0x40000000;

    public int getDialogsStatePendingColor() {
        return dialogsStatePendingColor;
    }

    public void setDialogsStatePendingColor(int dialogsStatePendingColor) {
        this.dialogsStatePendingColor = dialogsStatePendingColor;
    }

    private int dialogsTimeColor = 0;

    public int getDialogsTimeColor() {
        return getColorWithFallback(dialogsTimeColor, getTextSecondaryColor());
    }

    public void setDialogsTimeColor(int dialogsTimeColor) {
        this.dialogsTimeColor = dialogsTimeColor;
    }

    private int dialogsStateSentColor = 0x40000000;

    public int getDialogsStateSentColor() {
        return dialogsStateSentColor;
    }

    public void setDialogsStateSentColor(int dialogsStateSentColor) {
        this.dialogsStateSentColor = dialogsStateSentColor;
    }

    private int dialogsStateDeliveredColor = 0x40000000;

    public int getDialogsStateDeliveredColor() {
        return dialogsStateDeliveredColor;
    }

    public void setDialogsStateDeliveredColor(int dialogsStateDeliveredColor) {
        this.dialogsStateDeliveredColor = dialogsStateDeliveredColor;
    }

    private int dialogsStateReadColor = 0xff7ea8ef;

    public int getDialogsStateReadColor() {
        return dialogsStateReadColor;
    }

    public void setDialogsStateReadColor(int dialogsStateReadColor) {
        this.dialogsStateReadColor = dialogsStateReadColor;
    }

    private int dialogsStateErrorColor = 0xffd24a43;

    public int getDialogsStateErrorColor() {
        return dialogsStateErrorColor;
    }

    public void setDialogsStateErrorColor(int dialogsStateErrorColor) {
        this.dialogsStateErrorColor = dialogsStateErrorColor;
    }

    private int dialogsCounterTextColor = 0;

    public int getDialogsCounterTextColor() {
        return getColorWithFallback(dialogsCounterTextColor, getTextPrimaryInvColor());
    }

    public void setDialogsCounterTextColor(int dialogsCounterTextColor) {
        this.dialogsCounterTextColor = dialogsCounterTextColor;
    }

    private int dialogsCounterBackgroundColor = 0;

    public int getDialogsCounterBackgroundColor() {
        return getColorWithFallback(dialogsCounterBackgroundColor, getPrimaryAltColor());
    }

    public void setDialogsCounterBackgroundColor(int dialogsCounterBackgroundColor) {
        this.dialogsCounterBackgroundColor = dialogsCounterBackgroundColor;
    }

    private int dialogsDividerColor = 0;

    public int getDialogsDividerColor() {
        return getColorWithFallback(dialogsDividerColor, getDividerColor());
    }

    public void setDialogsDividerColor(int dialogsDividerColor) {
        this.dialogsDividerColor = dialogsDividerColor;
    }

    private int dialogsTitleColor = 0;

    public int getDialogsTitleColor() {
        return getColorWithFallback(dialogsTitleColor, getTextPrimaryColor());
    }

    public void setDialogsTitleColor(int dialogsTitleColor) {
        this.dialogsTitleColor = dialogsTitleColor;
    }

    private int dialogsTextColor = 0;

    public int getDialogsTextColor() {
        return getColorWithFallback(dialogsTextColor, getTextSecondaryColor());
    }

    public void setDialogsTextColor(int dialogsTextColor) {
        this.dialogsTextColor = dialogsTextColor;
    }

    private int dialogsTypingColor = 0;

    public int getDialogsTypingColor() {
        return getColorWithFallback(dialogsTypingColor, getMainColor());
    }

    public void setDialogsTypingColor(int dialogsTypingColor) {
        this.dialogsTypingColor = dialogsTypingColor;
    }

    private int dialogsActionColor = 0xff5882ac;

    public int getDialogsActiveTextColor() {
        return getColorWithFallback(dialogsActionColor, getDialogsTextColor());
    }

    public void setDialogsActiveTextColor(int dialogsActionColor) {
        this.dialogsActionColor = dialogsActionColor;
    }

    //Dividers
    private int dividerColor = 0x1E000000;

    public int getDividerColor() {
        return dividerColor;
    }

    public int getDividerInvColor() {
        return dividerInvColor;
    }

    private int dividerInvColor = 0x1Effffff;

    public void setDividerInvColor(int dividerInvColor) {
        this.dividerInvColor = dividerInvColor;
    }

    public void setDividerColor(int divider) {
        this.dividerColor = divider;
    }

    //Text colors
    private int textPrimaryInvColor = 0xDEffffff;

    public int getTextPrimaryInvColor() {
        return textPrimaryInvColor;
    }

    public void setTextPrimaryInvColor(int textPrimaryInvColor) {
        this.textPrimaryInvColor = textPrimaryInvColor;
    }

    private int textPrimaryColor = 0xDE000000;

    public int getTextPrimaryColor() {
        return textPrimaryColor;
    }

    public void setTextPrimaryColor(int textPrimaryColor) {
        this.textPrimaryColor = textPrimaryColor;
    }

    private int textSecondaryColor = 0x7A000000;

    public int getTextSecondaryColor() {
        return textSecondaryColor;
    }

    public void setTextSecondaryColor(int textSecondaryColor) {
        this.textSecondaryColor = textSecondaryColor;
    }

    private int textSecondaryAccentColor = 0;

    public int getTextSecondaryAccentColor() {
        return getColorWithFallback(textSecondaryAccentColor, getTextSecondaryColor());
    }

    public void setTextSecondaryAccentColor(int textSecondaryAccentColor) {
        this.textSecondaryAccentColor = textSecondaryAccentColor;
    }

    private int textHintColor = 0x42000000;

    public int getTextHintColor() {
        return textHintColor;
    }

    public void setTextHintColor(int textHintColor) {
        this.textHintColor = textHintColor;
    }

    private int textSubHeaderColor = 0x6F000000;

    public int getTextSubHeaderColor() {
        return textSubHeaderColor;
    }

    public void setTextSubHeaderColor(int textSubHeaderColor) {
        this.textSubHeaderColor = textSubHeaderColor;
    }

    private int textSecondaryInvColor = 0x8Affffff;

    public int getTextSecondaryInvColor() {
        return textSecondaryInvColor;
    }

    public void setTextSecondaryInvColor(int textSecondaryInvColor) {
        this.textSecondaryInvColor = textSecondaryInvColor;
    }

    private int textHintInvColor = 0x42ffffff;

    public int getTextHintInvColor() {
        return textHintInvColor;
    }

    public void setTextHintInvColor(int textHintInvColor) {
        this.textHintInvColor = textHintInvColor;
    }

    private int textSubheaderInvColor = 0x8Fffffff;

    public int getTextSubheaderInvColor() {
        return textSubheaderInvColor;
    }

    public void setTextSubheaderInvColor(int textSubheaderInvColor) {
        this.textSubheaderInvColor = textSubheaderInvColor;
    }

    //Settings
    private int settingsMainTitleColor = 0;

    public int getSettingsMainTitleColor() {
        return getColorWithFallback(settingsMainTitleColor, getListActionColor());
    }

    public void setSettingsMainTitleColor(int settingsMainTitleColor) {
        this.settingsMainTitleColor = settingsMainTitleColor;
    }

    private int sectonIconSmallColor = 0xffb5b6b7;

    public int getSectonIconSmallColor() {
        return sectonIconSmallColor;
    }

    public void setSectonIconSmallColor(int sectonIconSmallColor) {
        this.sectonIconSmallColor = sectonIconSmallColor;
    }

    private int sectionIconLargeColor = 0;

    public int getSectionIconLargeColor() {
        return getColorWithFallback(sectionIconLargeColor, getMainColor());
    }

    public void setSectionIconLargeColor(int sectionIconLargeColor) {
        this.sectionIconLargeColor = sectionIconLargeColor;
    }

    private int settingsIconColor = 0;

    public int getSettingsIconColor() {
        return getColorWithFallback(settingsIconColor, getSectonIconSmallColor());
    }

    public void setSettingsIconColor(int settingsIconColor) {
        this.settingsIconColor = settingsIconColor;
    }

    private int settingsTitleColor = 0;

    public int getSettingsTitleColor() {
        return getColorWithFallback(settingsTitleColor, getTextPrimaryColor());
    }

    public void setSettingsTitleColor(int settingsTitleColor) {
        this.settingsTitleColor = settingsTitleColor;
    }

    private int settingsSubtitleColor = 0;

    public int getSettingsSubtitleColor() {
        return getColorWithFallback(settingsSubtitleColor, getTextSecondaryColor());
    }

    public void setSettingsSubtitleColor(int settingsSubtitleColor) {
        this.settingsSubtitleColor = settingsSubtitleColor;
    }

    private int settingsCategoryTextColor = 0;

    public int getSettingsCategoryTextColor() {
        return getColorWithFallback(settingsCategoryTextColor, getMainColor());
    }

    public void setSettingsCategoryTextColor(int settingsCategoryTextColor) {
        this.settingsCategoryTextColor = settingsCategoryTextColor;
    }

    //Profile
    private int profileTitleColor = 0;

    public int getProfileTitleColor() {
        return getColorWithFallback(profileTitleColor, getTextPrimaryInvColor());
    }

    public void setProfileTitleColor(int profileTitleColor) {
        this.profileTitleColor = profileTitleColor;
    }

    private int profileSubtitleColor = 0;

    public int getProfileSubtitleColor() {
        return getColorWithFallback(profileSubtitleColor, getTextSecondaryInvColor());
    }

    public void setProfileSubtitleColor(int profileSubtitleColor) {
        this.profileSubtitleColor = profileSubtitleColor;
    }

    private int profileIconColor = 0;

    public int getProfilleIconColor() {
        return getColorWithFallback(profileIconColor, getSectionIconLargeColor());

    }

    public void setProfilleIconColor(int profilleIconColor) {
        this.profileIconColor = profilleIconColor;
    }

    private int profileContactIconColor = 0;

    public int getProfileContactIconColor() {
        return getColorWithFallback(profileContactIconColor, getListActionColor());
    }

    public void setProfileContactIconColor(int profileContactIconColor) {
        this.profileContactIconColor = profileContactIconColor;
    }

    private int profileRecordIconTintColor = 0;

    public int getProfileRecordIconTintColor() {
        return getColorWithFallback(profileRecordIconTintColor, getMainColor());
    }

    public void setProfileRecordIconTintColor(int profileRecordIconTintColor) {
        this.profileRecordIconTintColor = profileRecordIconTintColor;
    }

    //Group Profile
    private int groupAdminColor = 0;

    public int getGroupAdminColor() {
        return getColorWithFallback(groupAdminColor, getAccentColor());
    }

    public void setGroupAdminColor(int groupAdminColor) {
        this.groupAdminColor = groupAdminColor;
    }

    private int groupActionAddIconColor = 0;

    public int getGroupActionAddIconColor() {
        return getColorWithFallback(groupActionAddIconColor, getListActionColor());
    }

    public void setGroupActionAddIconColor(int groupActionAddIconColor) {
        this.groupActionAddIconColor = groupActionAddIconColor;
    }

    private int groupActionAddTextColor = 0;

    public int getGroupActionAddTextColor() {
        return getColorWithFallback(groupActionAddTextColor, getListActionColor());
    }

    public void setGroupActionAddTextColor(int groupActionAddTextColor) {
        this.groupActionAddTextColor = groupActionAddTextColor;
    }

    private int groupOnlineColor = 0xff7ea8ef;

    public int getGroupOnlineColor() {
        return groupOnlineColor;
    }

    public void setGroupOnlineColor(int groupOnlineColor) {
        this.groupOnlineColor = groupOnlineColor;
    }

    //Conversation
    private int convSendEnabledColor = 0;

    public int getConvSendEnabledColor() {
        return getColorWithFallback(convSendEnabledColor, getMainColor());
    }

    public void setConvSendEnabledColor(int convSendEnabledColor) {
        this.convSendEnabledColor = convSendEnabledColor;
    }

    private int convSendDisabledColor = 0x42000000;

    public int getConvSendDisabledColor() {
        return convSendDisabledColor;
    }

    public void setConvSendDisabledColor(int convSendDisabledColor) {
        this.convSendDisabledColor = convSendDisabledColor;
    }

    private int convDateLineColor = 0x14000000;

    public int getConvDateLineColor() {
        return convDateLineColor;
    }

    public void setConvDateLineColor(int convDateLineColor) {
        this.convDateLineColor = convDateLineColor;
    }

    private int convDatetextColor = 0xffffffff;

    public int getConvDatetextColor() {
        return convDatetextColor;
    }

    public void setConvDatetextColor(int convDatetextColor) {
        this.convDatetextColor = convDatetextColor;
    }

    private int convDateBgColor = 0x99000000;

    public int getConvDateBgColor() {
        return convDateBgColor;
    }

    public void setConvDateBgColor(int convDateBgColor) {
        this.convDateBgColor = convDateBgColor;
    }

    private int convTextColor = 0;

    public int getConvTextColor() {
        return getColorWithFallback(convTextColor, getTextPrimaryColor());
    }

    public void setConvTextColor(int convTextColor) {
        this.convTextColor = convTextColor;
    }

    private int convTimeColor = 0x60000000;

    public int getConvTimeColor() {
        return convTimeColor;
    }

    public void setConvTimeColor(int convTimeColor) {
        this.convTimeColor = convTimeColor;
    }

    private int convStatePendingColor = 0x40000000;

    public int getConvStatePendingColor() {
        return convStatePendingColor;
    }

    public void setConvStatePendingColor(int convStatePendingColor) {
        this.convStatePendingColor = convStatePendingColor;
    }

    private int convStateSentColor = 0x40000000;

    public int getConvStateSentColor() {
        return convStateSentColor;
    }

    public void setConvStateSentColor(int convStateSentColor) {
        this.convStateSentColor = convStateSentColor;
    }

    private int convStateDeliveredColor = 0x40000000;

    public int getConvStateDeliveredColor() {
        return convStateDeliveredColor;
    }

    public void setConvStateDeliveredColor(int convStateDeliveredColor) {
        this.convStateDeliveredColor = convStateDeliveredColor;
    }

    private int convStateReadColor = 0xff7ea8ef;

    public int getConvStateReadColor() {
        return convStateReadColor;
    }

    public void setConvStateReadColor(int convStateReadColor) {
        this.convStateReadColor = convStateReadColor;
    }

    private int convStateErrorColor = 0xffd24a43;

    public int getConvStateErrorColor() {
        return convStateErrorColor;
    }

    public void setConvStateErrorColor(int convStateErrorColor) {
        this.convStateErrorColor = convStateErrorColor;
    }

    private int convStateWarrningColor = 0xffd24a43;

    public int getConvStateWarrningColor() {
        return convStateWarrningColor;
    }

    public void setConvStateWarrningColor(int convStateWarrningColor) {
        this.convStateWarrningColor = convStateWarrningColor;
    }

    private int convMediaDateBgColor = 0x99000000;

    public int getConvMediaDateBgColor() {
        return convMediaDateBgColor;
    }

    public void setConvMediaDateBgColor(int convMediaDateBgColor) {
        this.convMediaDateBgColor = convMediaDateBgColor;
    }

    private int convMediaStatePendingColor = 0xffffffff;

    public int getConvMediaStatePendingColor() {
        return convMediaStatePendingColor;
    }

    public void setConvMediaStatePendingColor(int convMediaStatePendingColor) {
        this.convMediaStatePendingColor = convMediaStatePendingColor;
    }

    private int convMediaStateSentColor = 0xffffffff;

    public int getConvMediaStateSentColor() {
        return convMediaStateSentColor;
    }

    public void setConvMediaStateSentColor(int convMediaStateSentColor) {
        this.convMediaStateSentColor = convMediaStateSentColor;
    }

    private int convMediaStateDeliveredColor = 0xffffffff;

    public int getConvMediaStateDeliveredColor() {
        return convMediaStateDeliveredColor;
    }

    public void setConvMediaStateDeliveredColor(int convMediaStateDeliveredColor) {
        this.convMediaStateDeliveredColor = convMediaStateDeliveredColor;
    }

    private int convMediaStateReadColor = 0xff7ea8ef;

    public int getConvMediaStateReadColor() {
        return convMediaStateReadColor;
    }

    public void setConvMediaStateReadColor(int convMediaStateReadColor) {
        this.convMediaStateReadColor = convMediaStateReadColor;
    }

    private int convMediaStateErrorColor = 0xffed534b;

    public int getConvMediaStateErrorColor() {
        return convMediaStateErrorColor;
    }

    public void setConvMediaStateErrorColor(int convMediaStateErrorColor) {
        this.convMediaStateErrorColor = convMediaStateErrorColor;
    }

    private int convMediaStateWarrningColor = 0xffed534b;

    public int getConvMediaStateWarrningColor() {
        return convMediaStateWarrningColor;
    }

    public void setConvMediaStateWarrningColor(int convMediaStateWarrningColor) {
        this.convMediaStateWarrningColor = convMediaStateWarrningColor;
    }

    private int convLikeColor = 0xffe2264d;

    public int getConvLikeColor() {
        return convLikeColor;
    }

    public void setConvLikeColor(int convLikeColor) {
        this.convLikeColor = convLikeColor;
    }

    //
    // Color utils
    //

    public int getMainPressedColor() {
        return getDarkenArgb(getMainColor(), 0.95);
    }

    public static int getDarkenArgb(int color, double percent) {
        return Color.argb(Color.alpha(color), (int) Math.round(Color.red(color) * percent), (int) Math.round(Color.green(color) * percent), (int) Math.round(Color.blue(color) * percent));
    }

    /**
     * Get color with fallback to default - if color is 0, returns fallback color
     *
     * @param baseColor     base color
     * @param fallbackColor fallback color
     * @return base or fallback color if base color is set to 0
     */
    public int getColorWithFallback(int baseColor, int fallbackColor) {
        if (baseColor != 0) {
            return baseColor;
        } else {
            return fallbackColor;
        }
    }

    //////////////////////////
    //      DIMENSIONS      //
    //////////////////////////

    // DialogsFragment layout settings
    private int dialogsPaddingTop = Screen.dp(8);

    public int getDialogsPaddingTop() {
        return dialogsPaddingTop;
    }

    public void setDialogsPaddingTop(int dialogsPaddingTop) {
        this.dialogsPaddingTop = dialogsPaddingTop;
    }

    // ContactsFragment layout settings
    private int contactsMainPaddingTop = 0;

    public int getContactsMainPaddingTop() {
        return contactsMainPaddingTop;
    }

    public void setContactsMainPaddingTop(int contactsMainPaddingTop) {
        this.contactsMainPaddingTop = contactsMainPaddingTop;
    }

}
