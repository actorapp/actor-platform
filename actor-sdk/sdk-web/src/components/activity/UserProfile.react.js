/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { assign } from 'lodash';
import React, { Component, PropTypes } from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import classnames from 'classnames';
import { lightbox } from '../../utils/ImageUtils';

import ActorClient from '../../utils/ActorClient';
import confirm from '../../utils/confirm'
import { escapeWithEmoji } from '../../utils/EmojiUtils';

import ContactActionCreators from '../../actions/ContactActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';
import NotificationsActionCreators from '../../actions/NotificationsActionCreators';

import PeerStore from '../../stores/PeerStore';
import DialogStore from '../../stores/DialogStore';
import NotificationsStore from '../../stores/NotificationsStore';
import OnlineStore from '../../stores/OnlineStore';

import AvatarItem from '../common/AvatarItem.react';
import Fold from '../common/Fold.React';

const getStateFromStores = (userId) => {
  const thisPeer = PeerStore.getUserPeer(userId);
  return {
    thisPeer: thisPeer,
    isNotificationsEnabled: NotificationsStore.isNotificationsEnabled(thisPeer),
    message: OnlineStore.getMessage()
  };
};

class UserProfile extends Component {
  static propTypes = {
    user: PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.state = assign({
      isActionsDropdownOpen: false
    }, getStateFromStores(props.user.id));

    NotificationsStore.addListener(this.onChange);
    DialogStore.addListener(this.onChange);
    OnlineStore.addListener(this.onChange);
  }

  addToContacts = () => ContactActionCreators.addContact(this.props.user.id);

  removeFromContacts = () => {
    const { user } = this.props;
    const confirmText = (
      <FormattedMessage message={this.getIntlMessage('modal.confirm.removeContact')}
                        name={user.name}/>
    );
    confirm(confirmText, {
      abortLabel: this.getIntlMessage('button.cancel'),
      confirmLabel: this.getIntlMessage('button.ok')
    }).then(
      () => ContactActionCreators.removeContact(user.id),
      () => {}
    );
  };

  onNotificationChange = (event) => {
    const { thisPeer } = this.state;
    NotificationsActionCreators.changeNotificationsEnabled(thisPeer, event.target.checked);
  };

  onChange = () => {
    const { user } = this.props;
    this.setState(getStateFromStores(user.id));
  };

  toggleActionsDropdown = () => {
    const { isActionsDropdownOpen } = this.state;

    if (!isActionsDropdownOpen) {
      this.setState({isActionsDropdownOpen: true});
      document.addEventListener('click', this.closeActionsDropdown, false);
    } else {
      this.closeActionsDropdown();
    }
  };

  closeActionsDropdown = () => {
    this.setState({isActionsDropdownOpen: false});
    document.removeEventListener('click', this.closeActionsDropdown, false);
  };

  clearChat = (uid) => {
    confirm(this.getIntlMessage('modal.confirm.clear'), {
      abortLabel: this.getIntlMessage('button.cancel'),
      confirmLabel: this.getIntlMessage('button.ok')
    }).then(
      () => {
        const peer = ActorClient.getUserPeer(uid);
        DialogActionCreators.clearChat(peer);
      },
      () => {}
    );
  };

  deleteChat = (uid) => {
    confirm(this.getIntlMessage('modal.confirm.delete'), {
      abortLabel: this.getIntlMessage('button.cancel'),
      confirmLabel: this.getIntlMessage('button.ok')
    }).then(
      () => {
        const peer = ActorClient.getUserPeer(uid);
        DialogActionCreators.deleteChat(peer);
      },
      () => {}
    );
  };

  handleAvatarClick = () => lightbox.open(this.props.user.bigAvatar);

  render() {
    const { user } = this.props;
    const { isNotificationsEnabled, isActionsDropdownOpen, message } = this.state;

    const dropdownClassNames = classnames('dropdown pull-left', {
      'dropdown--opened': isActionsDropdownOpen
    });

    const nickname = user.nick ? (
      <li>
        <svg className="icon icon--pink"
             dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#username"/>'}}/>
        <span className="title">{user.nick}</span>
        <span className="description">{this.getIntlMessage('profile.nickname')}</span>
      </li>
    ) : null;

    const email = user.emails[0] ? (
      <li>
        <svg className="icon icon--blue"
             dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#envelope"/>'}}/>
        <span className="title"><a href={'mailto:' + user.emails[0].email}>{user.emails[0].email}</a></span>
        <span className="description">{this.getIntlMessage('profile.email')}</span>
      </li>
    ) : null;

    const phone = user.phones[0] ? (
      <li>
        <i className="material-icons icon icon--green">call</i>
        <span className="title"><a href={'tel:+' + user.phones[0].number}>{'+' + user.phones[0].number}</a></span>
        <span className="description">{this.getIntlMessage('profile.phone')}</span>
      </li>
    ) : null;

    return (
      <div className="activity__body user_profile">

        <ul className="profile__list">
          <li className="profile__list__item user_profile__meta">
            <header>
              <AvatarItem image={user.bigAvatar}
                          placeholder={user.placeholder}
                          size="large"
                          title={user.name}
                          onClick={this.handleAvatarClick}/>

              <h3 className="user_profile__meta__title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(user.name)}}/>
              <div className="user_profile__meta__message">{message}</div>
            </header>

            {
              user.about
                ? <div className="user_profile__meta__about"
                       dangerouslySetInnerHTML={{__html: escapeWithEmoji(user.about).replace(/\n/g, '<br/>')}}/>
                : null
            }

            <footer>
              <div className={dropdownClassNames}>
                <button className="dropdown__button button button--flat" onClick={this.toggleActionsDropdown}>
                  <i className="material-icons">more_horiz</i>
                  {this.getIntlMessage('actions')}
                </button>
                <ul className="dropdown__menu dropdown__menu--left">
                  {
                    user.isContact
                      ? <li className="dropdown__menu__item" onClick={this.removeFromContacts}>
                          {this.getIntlMessage('removeFromContacts')}
                        </li>
                      : <li className="dropdown__menu__item" onClick={this.addToContacts}>
                          {this.getIntlMessage('addToContacts')}
                        </li>
                  }
                  <li className="dropdown__menu__item" onClick={() => this.clearChat(user.id)}>
                    {this.getIntlMessage('clearConversation')}
                  </li>
                  <li className="dropdown__menu__item" onClick={() => this.deleteChat(user.id)}>
                    {this.getIntlMessage('deleteConversation')}
                  </li>
                </ul>
              </div>
            </footer>
          </li>

          <li className="profile__list__item user_profile__contact_info no-p">
            <ul className="user_profile__contact_info__list">
              {phone}
              {email}
              {nickname}
            </ul>
          </li>

          <li className="profile__list__item user_profile__media no-p hide">
            <Fold icon="attach_file" iconClassName="icon--gray" title={this.getIntlMessage('sharedMedia')}>
              <ul>
                <li><a>230 Shared Photos and Videos</a></li>
                <li><a>49 Shared Links</a></li>
                <li><a>49 Shared Files</a></li>
              </ul>
            </Fold>
          </li>

          <li className="profile__list__item user_profile__notifications no-p">
            <label htmlFor="notifications">
              <i className="material-icons icon icon--squash">notifications_none</i>
              {this.getIntlMessage('notifications')}
              <div className="switch pull-right">
                <input checked={isNotificationsEnabled}
                       id="notifications"
                       onChange={this.onNotificationChange}
                       type="checkbox"/>
                <label htmlFor="notifications"/>
              </div>
            </label>
          </li>

        </ul>
      </div>
    );
  }
}

ReactMixin.onClass(UserProfile, IntlMixin);

export default UserProfile;
