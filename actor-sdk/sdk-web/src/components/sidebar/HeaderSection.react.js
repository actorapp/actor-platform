/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';
import classnames from 'classnames';
import ActorClient from '../../utils/ActorClient';
import { escapeWithEmoji } from '../../utils/EmojiUtils'
import confirm from '../../utils/confirm'

import MyProfileActions from '../../actions/MyProfileActionCreators';
import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import LoginActionCreators from '../../actions/LoginActionCreators';
import HelpActionCreators from '../../actions/HelpActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';
import PreferencesActionCreators from '../../actions/PreferencesActionCreators';

import MyProfileStore from '../../stores/MyProfileStore';
import CreateGroupStore from '../../stores/CreateGroupStore';
import AddContactStore from '../../stores/AddContactStore';
import PreferencesStore from '../../stores/PreferencesStore';

import AvatarItem from '../common/AvatarItem.react';
import CreateGroupModal from '../modals/CreateGroup';
import MyProfileModal from '../modals/MyProfile.react';
import AddContactModal from '../modals/AddContact.react';
import PreferencesModal from '../modals/Preferences.react';

class HeaderSection extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [MyProfileStore, CreateGroupStore, AddContactStore, PreferencesStore];

  static calculateState() {
    return {
      profile: MyProfileStore.getProfile(),
      isMyProfileOpen: MyProfileStore.isModalOpen(),
      isAddContactsOpen: AddContactStore.isOpen(),
      isCreateGroupOpen: CreateGroupStore.isModalOpen(),
      isPreferencesOpen: PreferencesStore.isOpen()
    }
  }

  componentWillMount() {
    this.setState({isOpened: false});
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
  openCreateGroup = () => CreateGroupActionCreators.open();
  openHelpDialog = () => HelpActionCreators.open();
  openAddContactModal = () => AddContactActionCreators.open();
  onSettingsOpen = () => PreferencesActionCreators.show();
  openTwitter = (event) => {
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open('https://twitter.com/actorapp')
    }
  };
  setLogout = () => {
    confirm(this.getIntlMessage('modal.confirm.logout'), {
      abortLabel: this.getIntlMessage('button.cancel'),
      confirmLabel: this.getIntlMessage('button.ok')
    }).then(
      () => LoginActionCreators.setLoggedOut(),
      () => {}
    );
  };

  render() {
    const { profile, isOpened, isMyProfileOpen, isCreateGroupOpen, isAddContactsOpen, isPreferencesOpen } = this.state;

    if (profile) {

      const headerClass = classnames('sidebar__header', 'sidebar__header--clickable', {
        'sidebar__header--opened': isOpened
      });
      const menuClass = classnames('dropdown', {
        'dropdown--opened': isOpened
      });

      return (
        <header className={headerClass}>
          <div className="sidebar__header__user row" onClick={this.toggleHeaderMenu}>
            <AvatarItem image={profile.avatar}
                        placeholder={profile.placeholder}
                        size="tiny"
                        title={profile.name} />
            <span className="sidebar__header__user__name col-xs"
                  dangerouslySetInnerHTML={{__html: escapeWithEmoji(profile.name)}}/>
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
                <li className="dropdown__menu__item" onClick={this.openCreateGroup}>
                  <i className="material-icons">group_add</i>
                  {this.getIntlMessage('menu.createGroup')}
                </li>
                <li className="dropdown__menu__separator"/>
                <li className="dropdown__menu__item" onClick={this.openHelpDialog}>
                  <i className="material-icons">help</i>
                  {this.getIntlMessage('menu.helpAndFeedback')}
                </li>
                <li className="dropdown__menu__item" onClick={this.openTwitter}>
                  <svg className="icon icon--dropdown"
                       style={{marginLeft: -34}}
                       dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#twitter"/>'}}/>
                  {this.getIntlMessage('menu.twitter')}
                </li>
                <li className="dropdown__menu__item" onClick={this.onSettingsOpen}>
                  <i className="material-icons">settings</i>
                  {this.getIntlMessage('menu.preferences')}
                </li>
                <li className="dropdown__menu__separator"/>
                <li className="dropdown__menu__item" onClick={this.setLogout}>
                  {this.getIntlMessage('menu.signOut')}
                </li>
              </ul>
            </div>
          </div>


          {/* Modals */}
          {isMyProfileOpen ? <MyProfileModal/> : null}
          {isCreateGroupOpen ? <CreateGroupModal/> : null}
          {isAddContactsOpen ? <AddContactModal/> : null}
          {isPreferencesOpen ? <PreferencesModal/> : null}

        </header>
      );
    } else {
      return null;
    }
  }
}

ReactMixin.onClass(HeaderSection, IntlMixin);

export default Container.create(HeaderSection);
