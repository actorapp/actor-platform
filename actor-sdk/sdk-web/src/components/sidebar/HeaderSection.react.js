/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import ActorClient from '../../utils/ActorClient';
import { escapeWithEmoji } from '../../utils/EmojiUtils'
import confirm from '../../utils/confirm'
import SharedContainer from '../../utils/SharedContainer';
import { twitter, homePage } from '../../constants/ActorAppConstants';


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

    const SharedActor = SharedContainer.get();
    this.twitter = SharedActor.twitter ? SharedActor.twitter : twitter;
    this.homePage = SharedActor.homePage ? SharedActor.homePage : homePage;
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

  static contextTypes = {
    intl: PropTypes.object
  };

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
    event.preventDefault();
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(`https://twitter.com/${this.twitter}`, '_blank');
    }
  };
  openHomePage = (event) => {
    event.preventDefault();
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(this.homePage, '_blank');
    }
  };
  setLogout = () => {
    const { intl } = this.context;
    confirm(intl.messages['modal.confirm.logout'], {
      abortLabel: intl.messages['button.cancel'],
      confirmLabel: intl.messages['button.ok']
    }).then(
      () => LoginActionCreators.setLoggedOut(),
      () => {}
    );
  };

  render() {
    const { profile, isOpened, isMyProfileOpen, isCreateGroupOpen, isAddContactsOpen, isPreferencesOpen } = this.state;
    const { intl } = this.context;

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
                  {intl.messages['menu.editProfile']}
                </li>
                <li className="dropdown__menu__item" onClick={this.openAddContactModal}>
                  <i className="material-icons">person_add</i>
                  {intl.messages['menu.addToContacts']}
                </li>
                <li className="dropdown__menu__item" onClick={this.openCreateGroup}>
                  <i className="material-icons">group_add</i>
                  {intl.messages['menu.createGroup']}
                </li>
                <li className="dropdown__menu__separator"/>
                <li className="dropdown__menu__item" onClick={this.onSettingsOpen}>
                  <i className="material-icons">settings</i>
                  {intl.messages['menu.preferences']}
                </li>
                <li className="dropdown__menu__item" onClick={this.openHelpDialog}>
                  <i className="material-icons">help</i>
                  {intl.messages['menu.helpAndFeedback']}
                </li>
                <li className="dropdown__menu__item">
                  <a href={`https://twitter.com/${this.twitter}`} onClick={this.openTwitter}>
                    <svg className="icon icon--dropdown"
                         style={{marginLeft: -34}}
                         dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#twitter"/>'}}/>
                    {intl.messages['menu.twitter']}
                  </a>
                </li>
                <li className="dropdown__menu__item">
                  <a href={this.homePage} onClick={this.openHomePage}>
                    <i className="material-icons">public</i>
                    {intl.messages['menu.homePage']}
                  </a>
                </li>
                <li className="dropdown__menu__separator"/>
                <li className="dropdown__menu__item" onClick={this.setLogout}>
                  {intl.messages['menu.signOut']}
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

export default Container.create(HeaderSection);
