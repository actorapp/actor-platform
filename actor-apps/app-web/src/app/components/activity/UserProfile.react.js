/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import _ from 'lodash';
import React from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import classnames from 'classnames';

import ActorClient from 'utils/ActorClient';
import confirm from 'utils/confirm'
import { escapeWithEmoji } from 'utils/EmojiUtils';

import ContactActionCreators from 'actions/ContactActionCreators';
import DialogActionCreators from 'actions/DialogActionCreators';

import PeerStore from 'stores/PeerStore';
import DialogStore from 'stores/DialogStore';

import AvatarItem from 'components/common/AvatarItem.react';
import Fold from 'components/common/Fold.React';

const getStateFromStores = (userId) => {
  const thisPeer = PeerStore.getUserPeer(userId);
  return {
    thisPeer: thisPeer,
    isNotificationsEnabled: DialogStore.isNotificationsEnabled(thisPeer)
  };
};

@ReactMixin.decorate(IntlMixin)
class UserProfile extends React.Component {
  static propTypes = {
    user: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.state = _.assign({
      isActionsDropdownOpen: false
    }, getStateFromStores(props.user.id));

    DialogStore.addNotificationsListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeNotificationsListener(this.onChange);
  }

  componentWillReceiveProps(newProps) {
    this.setState(getStateFromStores(newProps.user.id));
  }

  addToContacts = () => {
    ContactActionCreators.addContact(this.props.user.id);
  };

  removeFromContacts = () => {
    const { user } = this.props;
    const confirmText = 'You really want to remove ' + user.name + ' from your contacts?';

    confirm(confirmText).then(
      () => ContactActionCreators.removeContact(user.id)
    );
  };

  onNotificationChange = (event) => {
    const { thisPeer } = this.state;
    DialogActionCreators.changeNotificationsEnabled(thisPeer, event.target.checked);
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
    confirm('Do you really want to delete this conversation?').then(
      () => {
        const peer = ActorClient.getUserPeer(uid);
        DialogActionCreators.clearChat(peer);
      },
      () => {}
    );
  };

  deleteChat = (uid) => {
    confirm('Do you really want to delete this conversation?').then(
      () => {
        const peer = ActorClient.getUserPeer(uid);
        DialogActionCreators.deleteChat(peer);
      },
      () => {}
    );
  };


  render() {
    const { user } = this.props;
    const { isNotificationsEnabled, isActionsDropdownOpen } = this.state;

    const actions = (user.isContact === false) ? (
      <li className="dropdown__menu__item" onClick={this.addToContacts}>
        <FormattedMessage message={this.getIntlMessage('addToContacts')}/>
      </li>
    ) : (
      <li className="dropdown__menu__item" onClick={this.removeFromContacts}>
        <FormattedMessage message={this.getIntlMessage('removeFromContacts')}/>
      </li>
    );

    const dropdownClassNames = classnames('dropdown pull-left', {
      'dropdown--opened': isActionsDropdownOpen
    });

    const about = user.about ? (
      <div className="user_profile__meta__about"
           dangerouslySetInnerHTML={{__html: escapeWithEmoji(user.about).replace(/\n/g, '<br/>')}}/>
    ) : null;

    const nickname = user.nick ? (
      <li>
        <svg className="icon icon--pink"
             dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#username"/>'}}/>
        <span className="title">{user.nick}</span>
        <span className="description">nickname</span>
      </li>
    ) : null;

    const email = user.email ? (
      <li className="hide">
        <i className="material-icons icon icon--blue">mail</i>
        <span className="title">{user.email}</span>
        <span className="description">email</span>
      </li>
    ) : null;

    const phone = user.phones[0] ? (
      <li>
        <i className="material-icons icon icon--green">call</i>
        <span className="title">{'+' + user.phones[0].number}</span>
        <span className="description">mobile</span>
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
                          title={user.name}/>

              <h3 className="user_profile__meta__title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(user.name)}}/>
              <div className="user_profile__meta__presence">{user.presence}</div>
            </header>
            {about}
            <footer>
              <div className={dropdownClassNames}>
                <button className="dropdown__button button button--flat" onClick={this.toggleActionsDropdown}>
                  <i className="material-icons">more_horiz</i>
                  <FormattedMessage message={this.getIntlMessage('actions')}/>
                </button>
                <ul className="dropdown__menu dropdown__menu--left">
                  {actions}
                  <li className="dropdown__menu__item" onClick={() => this.clearChat(user.id)}>
                    <FormattedMessage message={this.getIntlMessage('clearConversation')}/>
                  </li>
                  <li className="dropdown__menu__item" onClick={() => this.deleteChat(user.id)}>
                    <FormattedMessage message={this.getIntlMessage('deleteConversation')}/>
                  </li>
                </ul>
              </div>
            </footer>
          </li>

          <li className="profile__list__item user_profile__contact_info no-p">
            <ul className="user_profile__contact_info__list">
              {nickname}
              {phone}
              {email}
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
              <FormattedMessage message={this.getIntlMessage('notifications')}/>
              <div className="switch pull-right">
                <input checked={isNotificationsEnabled}
                       id="notifications"
                       onChange={this.onNotificationChange}
                       type="checkbox"/>
                <label htmlFor="notifications"></label>
              </div>
            </label>
          </li>

        </ul>
      </div>
    );
  }
}

export default UserProfile;
