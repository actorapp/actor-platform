/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import mixpanel from 'utils/Mixpanel';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
import classnames from 'classnames';
import ActorClient from 'utils/ActorClient';
import { escapeWithEmoji } from 'utils/EmojiUtils'
import confirm from 'utils/confirm'

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


  openMyProfile = () => MyProfileActions.show();
  openHelpDialog = () => HelpActionCreators.open();
  openAddContactModal = () => AddContactActionCreators.openModal();
  onSettingsOpen = () => PreferencesActionCreators.show();
  openTwitter = () => window.open('https://twitter.com/actorapp');
  setLogout = () => {
    confirm('Do you really want to leave?').then(
      () => LoginActionCreators.setLoggedOut(),
      () => {}
    );
  };

  render() {
    const { profile, isOpened } = this.state;

    if (profile) {

      const headerClass = classnames('sidebar__header', 'sidebar__header--clickable', {
        'sidebar__header--opened': isOpened
      });
      const menuClass = classnames('dropdown', {
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
                <li className="dropdown__menu__item" onClick={this.openMyProfile}>
                  <i className="material-icons">edit</i>
                  {this.getIntlMessage('menu.editProfile')}
                </li>
                <li className="dropdown__menu__item" onClick={this.openAddContactModal}>
                  <i className="material-icons">person_add</i>
                  {this.getIntlMessage('menu.addToContacts')}
                </li>
                <li className="dropdown__menu__separator"></li>
                <li className="dropdown__menu__item" onClick={this.openHelpDialog}>
                  <i className="material-icons">help</i>
                  {this.getIntlMessage('menu.helpAndFeedback')}
                </li>
                <li className="dropdown__menu__item" onClick={this.openTwitter}>
                  <svg className="icon icon--dropdown"
                       style={{marginLeft: -34}}
                       dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/img/sprite/icons.svg#twitter"/>'}}/>
                  {this.getIntlMessage('menu.twitter')}
                </li>
                <li className="dropdown__menu__item" onClick={this.onSettingsOpen}>
                  <i className="material-icons">settings</i>
                  {this.getIntlMessage('menu.preferences')}
                </li>
                <li className="dropdown__menu__separator"></li>
                <li className="dropdown__menu__item" onClick={this.setLogout}>
                  {this.getIntlMessage('menu.signOut')}
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
