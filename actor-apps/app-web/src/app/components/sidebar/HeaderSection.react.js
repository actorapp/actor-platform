/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import mixpanel from 'utils/Mixpanel';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import classNames from 'classnames';
import ActorClient from 'utils/ActorClient';
import { escapeWithEmoji } from 'utils/EmojiUtils'

import MyProfileActions from 'actions/MyProfileActionCreators';
import LoginActionCreators from 'actions/LoginActionCreators';
import HelpActionCreators from 'actions/HelpActionCreators';
import AddContactActionCreators from 'actions/AddContactActionCreators';
import PreferencesActionCreators from 'actions/PreferencesActionCreators';

import MyProfileStore from 'stores/MyProfileStore'

import AvatarItem from 'components/common/AvatarItem.react';
import MyProfileModal from 'components/modals/MyProfile.react';
import AddContactModal from 'components/modals/AddContact.react';
import PreferencesModal from 'components/modals/Preferences.react';

@ReactMixin.decorate(IntlMixin)
class HeaderSection extends Component {
  static getStores = () => [MyProfileStore];
  static calculateState() {
    return {
      isOpened: false,
      profile: MyProfileStore.getProfile()
    }
  }

  toggleHeaderMenu = () => {
    const { isOpened } = this.state;

    if (!isOpened) {
      this.setState({isOpened: true});
      document.addEventListener('click', this.closeHeaderMenu, false);
    } else {
      this.closeHeaderMenu();
    }
  };

  closeHeaderMenu = () => {
    this.setState({isOpened: false});
    document.removeEventListener('click', this.closeHeaderMenu, false);
  };


  setLogout = () => LoginActionCreators.setLoggedOut();
  openMyProfile = () => MyProfileActions.show();
  openHelpDialog = () => HelpActionCreators.open();
  openAddContactModal = () => AddContactActionCreators.openModal();
  onSettingsOpen = () => PreferencesActionCreators.show();
  openTwitter = () => window.open('https://twitter.com/actorapp');

  render() {
    const { profile, isOpened } = this.state;

    if (profile) {

      const headerClass = classNames('sidebar__header', 'sidebar__header--clickable', {
        'sidebar__header--opened': isOpened
      });
      const menuClass = classNames('dropdown', {
        'dropdown--opened': isOpened
      });

      const profileName = escapeWithEmoji(profile.name);

      return (
        <header className={headerClass}>
          <div className="sidebar__header__user row" onClick={this.toggleHeaderMenu}>
            <AvatarItem image={profile.avatar}
                        placeholder={profile.placeholder}
                        size="tiny"
                        title={profile.name} />
            <span className="sidebar__header__user__name col-xs"
                  dangerouslySetInnerHTML={{__html: profileName}}/>
            <div className={menuClass}>
              <span className="dropdown__button">
                <i className="material-icons">arrow_drop_down</i>
              </span>
              <ul className="dropdown__menu dropdown__menu--right">
                <li className="dropdown__menu__item hide">
                  <i className="material-icons">photo_camera</i>
                  <FormattedMessage message={this.getIntlMessage('setProfilePhoto')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.openMyProfile}>
                  <i className="material-icons">edit</i>
                  <FormattedMessage message={this.getIntlMessage('editProfile')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.openAddContactModal}>
                  <i className="material-icons">person_add</i>
                  <FormattedMessage message={this.getIntlMessage('addContact')}/>
                </li>
                <li className="dropdown__menu__separator"></li>
                <li className="dropdown__menu__item hide">
                  <svg className="icon icon--dropdown"
                       dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#integration"/>'}}/>
                  <FormattedMessage message={this.getIntlMessage('configureIntegrations')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.openHelpDialog}>
                  <i className="material-icons">help</i>
                  <FormattedMessage message={this.getIntlMessage('helpAndFeedback')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.openTwitter}>
                  <svg className="icon icon--dropdown"
                       style={{marginLeft: -34}}
                       dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#twitter"/>'}}/>
                  <FormattedMessage message={this.getIntlMessage('twitter')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.onSettingsOpen}>
                  <i className="material-icons">settings</i>
                  <FormattedMessage message={this.getIntlMessage('preferences')}/>
                </li>
                <li className="dropdown__menu__separator"></li>
                <li className="dropdown__menu__item" onClick={this.setLogout}>
                  <FormattedMessage message={this.getIntlMessage('signOut')}/>
                </li>
              </ul>
            </div>
          </div>

          <MyProfileModal/>
          <AddContactModal/>
          <PreferencesModal/>
        </header>
      );
    } else {
      return null;
    }
  }
}

export default Container.create(HeaderSection);
