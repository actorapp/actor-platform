/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import { lightbox } from '../../utils/ImageUtils';
import { Container } from 'flux/utils';
import Scrollbar from '../common/Scrollbar.react';

import { escapeWithEmoji } from '../../utils/EmojiUtils'

import NotificationsActionCreators from '../../actions/NotificationsActionCreators';

import DialogStore from '../../stores/DialogStore';
import NotificationsStore from '../../stores/NotificationsStore';
import GroupStore from '../../stores/GroupStore';
import UserStore from '../../stores/UserStore';
import OnlineStore from '../../stores/OnlineStore';

import SvgIcon from '../common/SvgIcon.react';
import AvatarItem from '../common/AvatarItem.react';
import GroupProfileMembers from '../activity/GroupProfileMembers.react';
import Fold from '../common/Fold.react';
import ToggleNotifications from '../common/ToggleNotifications.react';

class GroupProfile extends Component {
  static propTypes = {
    group: PropTypes.object.isRequired
  };

  static getStores() {
    return [NotificationsStore, GroupStore, OnlineStore];
  }

  static calculateState(prevState, nextProps) {
    const gid = nextProps.group.id;
    const peer = gid ? GroupStore.getGroup(gid) : null;
    return {
      peer,
      // should not require to pass a peer
      isNotificationsEnabled: peer ? NotificationsStore.isNotificationsEnabled(peer) : true,
      integrationToken: GroupStore.getToken(),
      message: OnlineStore.getMessage()
    };
  }

  constructor(props) {
    super(props);

    this.handleNotificationChange = this.handleNotificationChange.bind(this);
    this.handleTokenSelect = this.handleTokenSelect.bind(this);
    this.handleAvatarClick = this.handleAvatarClick.bind(this);
  }

  handleNotificationChange(event) {
    const { peer } = this.state;
    NotificationsActionCreators.changeNotificationsEnabled(peer, event.target.checked);
  }

  handleTokenSelect(event) {
    event.target.select();
  }

  handleAvatarClick() {
    lightbox.open(this.props.group.bigAvatar);
  }

  renderMainInfo() {
    const { group } = this.props;
    const admin = UserStore.getUser(group.adminId);

    return (
      <header>
        <AvatarItem
          className="profile__avatar"
          size="large"
          image={group.bigAvatar}
          placeholder={group.placeholder}
          title={group.name}
          onClick={this.handleAvatarClick}
        />

        <h3
          className="group_profile__meta__title"
          dangerouslySetInnerHTML={{ __html: escapeWithEmoji(group.name) }}
        />

        <div className="group_profile__meta__created">
          <FormattedMessage id="createdBy"/>
          &nbsp;
          <span dangerouslySetInnerHTML={{ __html: escapeWithEmoji(admin.name) }}/>
        </div>
      </header>
    );
  }

  renderAbout() {
    const { group: { about } } = this.props;

    if (!about) {
      return null;
    }

    return (
      <div
        className="group_profile__meta__description"
        dangerouslySetInnerHTML={{ __html: escapeWithEmoji(about).replace(/\n/g, '<br/>') }}
      />
    );
  }

  renderToken() {
    const { group: { adminId } } = this.props;
    const { integrationToken } = this.state;
    const myId = UserStore.getMyId();

    if (adminId !== myId) {
      return null;
    }

    return (
      <li className="profile__list__item group_profile__integration no-p">
        <Fold icon="power" iconClassName="icon--pink" title={<FormattedMessage id="integrationToken"/>}>

          <div className="info info--light">
            <p><FormattedMessage id="integrationTokenHint"/></p>
            <a href="https://actor.readme.io/docs/simple-integration" target="_blank"><FormattedMessage id="integrationTokenHelp"/></a>
          </div>

          <textarea
            className="textarea"
            onClick={this.handleTokenSelect}
            readOnly
            row="3"
            value={integrationToken}/>
        </Fold>
      </li>
    );
  }

  render() {
    const { group } = this.props;
    const { isNotificationsEnabled, message } = this.state;
    const isMember = DialogStore.isMember();

    const iconElement = (
      <SvgIcon className="icon icon--green" glyph="members" />
    );

    if (!isMember) {
      return (
        <div className="activity__body group_profile">
          <ul className="profile__list">
            <li className="profile__list__item group_profile__meta">
              {this.renderMainInfo()}
              {this.renderAbout()}
            </li>
          </ul>
        </div>
      );
    }

    return (
      <div className="activity__body group_profile">
        <Scrollbar>
          <ul className="profile__list">
            <li className="profile__list__item group_profile__meta">
              {this.renderMainInfo()}
              {this.renderAbout()}
            </li>

            <li className="profile__list__item group_profile__notifications no-p">
              <ToggleNotifications isNotificationsEnabled={isNotificationsEnabled} onNotificationChange={this.handleNotificationChange}/>
            </li>

            <li className="profile__list__item group_profile__members no-p">
              <Fold iconElement={iconElement} title={message}>
                <GroupProfileMembers groupId={group.id} members={group.members}/>
              </Fold>
            </li>

            {this.renderToken()}
          </ul>
        </Scrollbar>
      </div>
    );
  }
}

export default Container.create(GroupProfile, { withProps: true, pure: false });
