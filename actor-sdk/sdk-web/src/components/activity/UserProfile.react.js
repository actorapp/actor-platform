/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import { lightbox } from '../../utils/ImageUtils';

import { escapeWithEmoji } from '../../utils/EmojiUtils';

import NotificationsActionCreators from '../../actions/NotificationsActionCreators';

import UserStore from '../../stores/UserStore';
import NotificationsStore from '../../stores/NotificationsStore';
import OnlineStore from '../../stores/OnlineStore';

import AvatarItem from '../common/AvatarItem.react';
import ContactDetails from '../common/ContactDetails.react';
import ToggleNotifications from '../common/ToggleNotifications.react';

class UserProfile extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  };

  static propTypes = {
    user: PropTypes.object.isRequired
  };

  static getStores() {
    return [NotificationsStore, OnlineStore];
  }

  static calculateState(prevState, nextProps) {
    const uid = nextProps.user.id;
    const peer = uid ? UserStore.getUser(uid) : null;

    return {
      ...prevState,
      peer,
      isNotificationsEnabled: peer ? NotificationsStore.isNotificationsEnabled(peer) : true,
      message: OnlineStore.getMessage()
    };
  }

  constructor(props) {
    super(props);

    this.onNotificationChange = this.onNotificationChange.bind(this);
    this.handleAvatarClick = this.handleAvatarClick.bind(this);
  }

  onNotificationChange(event) {
    const { peer } = this.state;
    NotificationsActionCreators.changeNotificationsEnabled(peer, event.target.checked);
  }

  handleAvatarClick() {
    lightbox.open(this.props.user.bigAvatar)
  }

  renderAbout() {
    const { about } = this.props.user;
    if (!about) return null;

    return (
      <div
        className="user_profile__meta__about"
        dangerouslySetInnerHTML={{ __html: escapeWithEmoji(about).replace(/\n/g, '<br/>') }}/>
    )
  }

  renderToggleContact() {
    const { isContact } = this.props.user;

    if (isContact) {
      return (
        <li className="dropdown__menu__item" onClick={this.onRemoveFromContacts}>
          <FormattedMessage id="removeFromContacts"/>
        </li>
      );
    }

    return (
      <li className="dropdown__menu__item" onClick={this.onAddToContacts}>
        <FormattedMessage id="addToContacts"/>
      </li>
    );
  }

  renderBlockUser() {
    if (!this.context.delegate.features.blocking) {
      return null;
    }

    return (
      <li className="dropdown__menu__item" onClick={this.onBlockUser}>
        <FormattedMessage id="blockUser"/>
      </li>
    );
  }

  render() {
    const { user } = this.props;
    const { isNotificationsEnabled, message } = this.state;

    return (
      <div className="activity__body user_profile">

        <ul className="profile__list">
          <li className="profile__list__item user_profile__meta">
            <header>
              <AvatarItem
                className="profile__avatar"
                size="large"
                image={user.bigAvatar}
                placeholder={user.placeholder}
                title={user.name}
                onClick={this.handleAvatarClick}
              />

              <h3 className="user_profile__meta__title" dangerouslySetInnerHTML={{ __html: escapeWithEmoji(user.name) }}/>
              <div className="user_profile__meta__message">{message}</div>
            </header>

            {this.renderAbout()}
          </li>

          <li className="profile__list__item user_profile__contact_info no-p">
            <ContactDetails peerInfo={user}/>
          </li>

          <li className="profile__list__item user_profile__notifications no-p">
            <ToggleNotifications
              isNotificationsEnabled={isNotificationsEnabled}
              onNotificationChange={this.onNotificationChange}/>
          </li>

        </ul>
      </div>
    );
  }
}

export default Container.create(UserProfile, { withProps: true, pure: false });
