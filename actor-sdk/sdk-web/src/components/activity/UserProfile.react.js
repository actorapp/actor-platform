/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { FormattedMessage } from 'react-intl';
import classnames from 'classnames';
import { lightbox } from '../../utils/ImageUtils';

import ActorClient from '../../utils/ActorClient';
import confirm from '../../utils/confirm';
import { escapeWithEmoji } from '../../utils/EmojiUtils';

import ContactActionCreators from '../../actions/ContactActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';
import NotificationsActionCreators from '../../actions/NotificationsActionCreators';
import CallActionCreators from '../../actions/CallActionCreators';
import BlockedUsersActionCreators from '../../actions/BlockedUsersActionCreators';

import UserStore from '../../stores/UserStore';
import NotificationsStore from '../../stores/NotificationsStore';
import OnlineStore from '../../stores/OnlineStore';

import AvatarItem from '../common/AvatarItem.react';
import ContactDetails from '../common/ContactDetails.react';
import ToggleNotifications from '../common/ToggleNotifications.react';
import Fold from '../common/Fold.react';

class UserProfile extends Component {
  static contextTypes = {
    intl: PropTypes.object
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

    this.state = {
      isMoreDropdownOpen: false
    };

    this.onClearChat = this.onClearChat.bind(this);
    this.onDeleteChat = this.onDeleteChat.bind(this);
    this.onBlockUser = this.onBlockUser.bind(this);
    this.onRemoveFromContacts = this.onRemoveFromContacts.bind(this);
  }

  addToContacts = () => ContactActionCreators.addContact(this.props.user.id);

  onNotificationChange = (event) => {
    const { peer } = this.state;
    NotificationsActionCreators.changeNotificationsEnabled(peer, event.target.checked);
  };

  toggleActionsDropdown = () => {
    const { isActionsDropdownOpen } = this.state;

    if (!isActionsDropdownOpen) {
      this.setState({ isActionsDropdownOpen: true });
      document.addEventListener('click', this.closeActionsDropdown, false);
    } else {
      this.closeActionsDropdown();
    }
  };

  closeActionsDropdown = () => {
    this.setState({ isActionsDropdownOpen: false });
    document.removeEventListener('click', this.closeActionsDropdown, false);
  };

  onClearChat() {
    const { user } = this.props;
    confirm(
      <FormattedMessage id="modal.confirm.user.clear" values={{ name: user.name }} />
    ).then(
      () => {
        const peer = ActorClient.getUserPeer(user.id);
        DialogActionCreators.clearChat(peer);
      },
      () => {}
    );
  }

  onRemoveFromContacts() {
    const { user } = this.props;
    confirm(
      <FormattedMessage id="modal.confirm.user.removeContact" values={{ name: user.name }}/>
    ).then(
      () => ContactActionCreators.removeContact(user.id),
      () => {}
    );
  }

  onDeleteChat() {
    const { user } = this.props;

    confirm(
      <FormattedMessage id="modal.confirm.user.delete" values={{ name: user.name }} />
    ).then(
      () => {
        const peer = ActorClient.getUserPeer(user.id);
        DialogActionCreators.deleteChat(peer);
      },
      () => {}
    );
  }

  onBlockUser() {
    const { user } = this.props;

    confirm(
      <FormattedMessage id="modal.confirm.user.block" values={{ name: user.name }} />
    ).then(
      () => BlockedUsersActionCreators.blockUser(user.id),
      () => {}
    );
  }

  handleAvatarClick = () => lightbox.open(this.props.user.bigAvatar);

  makeCall = () => {
    const { user } = this.props;
    CallActionCreators.makeCall(user.id);
  };

  render() {
    const { user } = this.props;
    const { intl } = this.context;
    const { isNotificationsEnabled, isActionsDropdownOpen, message } = this.state;

    const dropdownClassNames = classnames('dropdown', {
      'dropdown--opened': isActionsDropdownOpen
    });

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

              <h3 className="user_profile__meta__title" dangerouslySetInnerHTML={{ __html: escapeWithEmoji(user.name) }}/>
              <div className="user_profile__meta__message">{message}</div>
            </header>

            {
              user.about
                ? <div className="user_profile__meta__about"
                       dangerouslySetInnerHTML={{ __html: escapeWithEmoji(user.about).replace(/\n/g, '<br/>') }}/>
                : null
            }

            <footer className="row">
              <div className="col-xs">
                <button className="button button--green button--wide" onClick={this.makeCall}>
                  <i className="material-icons">phone</i>
                  {intl.messages['button.call']}
                </button>
              </div>
              <div style={{ width: 10 }}/>
              <div className="col-xs">
                <div className={dropdownClassNames}>
                  <button className="dropdown__button button button--flat button--wide" onClick={this.toggleActionsDropdown}>
                    <i className="material-icons">more_horiz</i>
                    {intl.messages['actions']}
                  </button>
                  <ul className="dropdown__menu dropdown__menu--right">
                    {
                      user.isContact
                        ? <li className="dropdown__menu__item" onClick={this.onRemoveFromContacts}>
                            {intl.messages['removeFromContacts']}
                          </li>
                        : <li className="dropdown__menu__item" onClick={this.addToContacts}>
                            {intl.messages['addToContacts']}
                          </li>
                    }
                    <li className="dropdown__menu__item" onClick={this.onBlockUser}>
                      {intl.messages['blockUser']}
                    </li>
                    <li className="dropdown__menu__item" onClick={this.onClearChat}>
                      {intl.messages['clearConversation']}
                    </li>
                    <li className="dropdown__menu__item" onClick={this.onDeleteChat}>
                      {intl.messages['deleteConversation']}
                    </li>
                  </ul>
                </div>
              </div>
            </footer>
          </li>

          <li className="profile__list__item user_profile__contact_info no-p">
            <ContactDetails peerInfo={user}/>
          </li>

          <li className="profile__list__item user_profile__media no-p hide">
            <Fold icon="attach_file" iconClassName="icon--gray" title={intl.messages['sharedMedia']}>
              <ul>
                <li><a>230 Shared Photos and Videos</a></li>
                <li><a>49 Shared Links</a></li>
                <li><a>49 Shared Files</a></li>
              </ul>
            </Fold>
          </li>

          <li className="profile__list__item user_profile__notifications no-p">
            <ToggleNotifications isNotificationsEnabled={isNotificationsEnabled} onNotificationChange={this.onNotificationChange}/>
          </li>

        </ul>
      </div>
    );
  }
}

export default Container.create(UserProfile, { pure:false, withProps: true });
