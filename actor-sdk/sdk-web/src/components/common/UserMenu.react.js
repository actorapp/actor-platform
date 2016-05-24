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
import { FormattedMessage } from 'react-intl';

import ProfileActionCreators from '../../actions/ProfileActionCreators';
import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';
import LoginActionCreators from '../../actions/LoginActionCreators';
import HelpActionCreators from '../../actions/HelpActionCreators';
import AddContactActionCreators from '../../actions/AddContactActionCreators';
import PreferencesActionCreators from '../../actions/PreferencesActionCreators';

import ProfileStore from '../../stores/ProfileStore';

import SvgIcon from '../common/SvgIcon.react';
import AvatarItem from '../common/AvatarItem.react';

class UserMenu extends Component {
  static propTypes = {
    className: PropTypes.string
  }

  static getStores() {
    return [ProfileStore];
  }

  static calculateState() {
    return {
      profile: ProfileStore.getProfile()
    }
  }

  constructor(props) {
    super(props);

    this.state = {
      isOpened: false
    }

    this.openHelp = this.openHelp.bind(this);
    this.openTwitter = this.openTwitter.bind(this);
    this.openFacebook = this.openFacebook.bind(this);
    this.openHomePage = this.openHomePage.bind(this);
    this.setLogout = this.setLogout.bind(this);
    this.toggleHeaderMenu = this.toggleHeaderMenu.bind(this);
    this.closeHeaderMenu = this.closeHeaderMenu.bind(this);
    this.openMyProfile = this.openMyProfile.bind(this);
    this.openCreateGroup = this.openCreateGroup.bind(this);
    this.openAddContactModal = this.openAddContactModal.bind(this);
    this.onSettingsOpen = this.onSettingsOpen.bind(this);
  }

  toggleHeaderMenu() {
    const { isOpened } = this.state;

    if (!isOpened) {
      this.setState({ isOpened: true });
      document.addEventListener('click', this.closeHeaderMenu, false);
    } else {
      this.closeHeaderMenu();
    }
  }

  closeHeaderMenu() {
    this.setState({ isOpened: false });
    document.removeEventListener('click', this.closeHeaderMenu, false);
  }

  openMyProfile() {
    ProfileActionCreators.show();
  }

  openCreateGroup() {
    CreateGroupActionCreators.open();
  }

  openAddContactModal() {
    AddContactActionCreators.open();
  }

  onSettingsOpen() {
    PreferencesActionCreators.show();
  }

  openHelp() {
    HelpActionCreators.open()
  }

  openTwitter(event) {
    const { twitter } = SharedContainer.get();

    event.preventDefault();
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(`https://twitter.com/${twitter}`, '_blank');
    }
  }

  openFacebook(event) {
    const { facebook } = SharedContainer.get();

    event.preventDefault();
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(`https://facebook.com/${facebook}`, '_blank');
    }
  }

  openHomePage(event) {
    const { homePage } = SharedContainer.get();

    event.preventDefault();
    if (ActorClient.isElectron()) {
      ActorClient.handleLinkClick(event);
    } else {
      window.open(homePage, '_blank');
    }
  }

  setLogout() {
    confirm(<FormattedMessage id="modal.confirm.logout"/>).then(
      () => LoginActionCreators.setLoggedOut(),
      () => {}
    );
  }

  renderTwitterLink() {
    const { twitter } = SharedContainer.get();
    if (!twitter) return null;

    return (
      <li className="dropdown__menu__item">
        <a href={`https://twitter.com/${twitter}`} onClick={this.openTwitter}>
          <SvgIcon className="icon icon--dropdown sidebar__header__twitter" glyph="twitter" />
          <FormattedMessage id="menu.twitter"/>
        </a>
      </li>
    );
  }

  renderFacebookLink() {
    const { facebook } = SharedContainer.get();
    if (!facebook) return null;

    return (
      <li className="dropdown__menu__item">
        <a href={`https://facebook.com/${facebook}`} onClick={this.openFacebook}>
          <SvgIcon className="icon icon--dropdown sidebar__header__facebook" glyph="facebook" />
          <FormattedMessage id="menu.facebook"/>
        </a>
      </li>
    );
  }

  renderHomeLink() {
    const { homePage } = SharedContainer.get();
    if (!homePage) return null;

    return (
      <li className="dropdown__menu__item">
        <a href={homePage} onClick={this.openHomePage}>
          <i className="material-icons">public</i>
          <FormattedMessage id="menu.homePage"/>
        </a>
      </li>
    );
  }

  renderHelpLink() {
    const { helpPhone } = SharedContainer.get();
    if (!helpPhone) return null;

    if (/@/.test(helpPhone)) {
      return (
        <li className="dropdown__menu__item">
          <a href={`mailto:${helpPhone}`}>
            <i className="material-icons">help</i>
            <FormattedMessage id="menu.helpAndFeedback"/>
          </a>
        </li>
      );
    } else {
      return (
        <li className="dropdown__menu__item" onClick={this.openHelp}>
          <i className="material-icons">help</i>
          <FormattedMessage id="menu.helpAndFeedback"/>
        </li>
      );
    }
  }

  renderMenu() {
    const { isOpened } = this.state;

    const menuClass = classnames('dropdown user-menu__dropdown', {
      'dropdown--opened': isOpened
    });

    return (
      <div className={menuClass}>
        <ul className="dropdown__menu dropdown__menu--right">
          <li className="dropdown__menu__item" onClick={this.openMyProfile}>
            <i className="material-icons">edit</i>
            <FormattedMessage id="menu.editProfile"/>
          </li>
          <li className="dropdown__menu__item" onClick={this.openAddContactModal}>
            <i className="material-icons">person_add</i>
            <FormattedMessage id="menu.addToContacts"/>
          </li>
          <li className="dropdown__menu__item" onClick={this.openCreateGroup}>
            <i className="material-icons">group_add</i>
            <FormattedMessage id="menu.createGroup"/>
          </li>

          <li className="dropdown__menu__separator"/>

          <li className="dropdown__menu__item" onClick={this.onSettingsOpen}>
            <i className="material-icons">settings</i>
            <FormattedMessage id="menu.preferences"/>
          </li>
          {this.renderHelpLink()}
          {this.renderTwitterLink()}
          {this.renderFacebookLink()}
          {this.renderHomeLink()}

          <li className="dropdown__menu__separator"/>

          <li className="dropdown__menu__item" onClick={this.setLogout}>
            <FormattedMessage id="menu.signOut"/>
          </li>
        </ul>
      </div>

    );
  }

  render() {
    const { className } = this.props;
    const { profile, isOpened } = this.state;

    if (!profile) return null;

    const usermenuClassName = classnames('user-menu', className, {
      'user-menu--opened': isOpened
    });

    return (
      <div className={usermenuClassName} onClick={this.toggleHeaderMenu}>
        <AvatarItem
          className="user-menu__avatar"
          image={profile.avatar}
          placeholder={profile.placeholder}
          size="tiny"
          title={profile.name}
        />

        <div
          className="user-menu__name"
          dangerouslySetInnerHTML={{ __html: escapeWithEmoji(profile.name) }}
        />

        <i className="user-menu__icon material-icons">arrow_drop_down</i>

        {this.renderMenu()}
      </div>
    );
  }
}

export default Container.create(UserMenu);
