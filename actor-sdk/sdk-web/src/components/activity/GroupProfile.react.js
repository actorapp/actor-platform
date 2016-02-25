/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { assign } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import classnames from 'classnames';
import { lightbox } from '../../utils/ImageUtils';
import { Container } from 'flux/utils';
import Scrollbar from '../common/Scrollbar.react';

import ActorClient from '../../utils/ActorClient';
import confirm from '../../utils/confirm'
import { escapeWithEmoji } from '../../utils/EmojiUtils'

import DialogActionCreators from '../../actions/DialogActionCreators';
import InviteUserActions from '../../actions/InviteUserActions';
import EditGroupActionCreators from '../../actions/EditGroupActionCreators';
import NotificationsActionCreators from '../../actions/NotificationsActionCreators';
import CallActionCreators from '../../actions/CallActionCreators';

import DialogStore from '../../stores/DialogStore';
import NotificationsStore from '../../stores/NotificationsStore';
import GroupStore from '../../stores/GroupStore';
import UserStore from '../../stores/UserStore';
import OnlineStore from '../../stores/OnlineStore';

import AvatarItem from '../common/AvatarItem.react';
import GroupProfileMembers from '../activity/GroupProfileMembers.react';
import Fold from '../common/Fold.react';

const MAX_GROUP_CALL_SIZE = 25;

const getStateFromStores = (gid) => {
  const thisPeer = gid ? GroupStore.getGroup(gid) : null;
  return {
    thisPeer,
    // should not require to pass a peer
    isNotificationsEnabled: thisPeer ? NotificationsStore.isNotificationsEnabled(thisPeer) : true,
    integrationToken: GroupStore.getToken(),
    message: OnlineStore.getMessage()
  };
};

class GroupProfile extends Component {
  static propTypes = {
    group: PropTypes.object.isRequired
  };

  static getStores() {
    return [NotificationsStore, GroupStore, OnlineStore];
  }

  static calculateState(prevState) {
    return getStateFromStores((prevState && prevState.group) ? prevState.group.id : null);
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  constructor(props) {
    super(props);

    this.state = {
      isMoreDropdownOpen: false,
      group: props.group // hack to be able to access groupId in getStateFromStores
    }
  }

  // hack for groupId in getStateFromStores
  componentWillReceiveProps(nextProps) {
    this.setState({group: nextProps.group});
  }

  onAddMemberClick = group => InviteUserActions.show(group);

  onLeaveGroupClick = gid => {
    const { intl } = this.context;
    confirm(intl.messages['modal.confirm.leave']).then(
      () => DialogActionCreators.leaveGroup(gid),
      () => {}
    );
  };

  onNotificationChange = event => {
    const { thisPeer } = this.state;
    NotificationsActionCreators.changeNotificationsEnabled(thisPeer, event.target.checked);
  };

  selectToken = (event) => event.target.select();

  toggleMoreDropdown = () => {
    const { isMoreDropdownOpen } = this.state;

    if (!isMoreDropdownOpen) {
      this.setState({isMoreDropdownOpen: true});
      document.addEventListener('click', this.closeMoreDropdown, false);
    } else {
      this.closeMoreDropdown();
    }
  };

  closeMoreDropdown = () => {
    this.setState({isMoreDropdownOpen: false});
    document.removeEventListener('click', this.closeMoreDropdown, false);
  };

  onClearGroupClick = (gid) => {
    const { intl } = this.context;
    confirm(intl.messages['modal.confirm.clear']).then(
      () => {
        const peer = ActorClient.getGroupPeer(gid);
        DialogActionCreators.clearChat(peer)
      },
      () => {}
    );
  };

  onDeleteGroupClick = (gid) => {
    const { intl } = this.context;
    confirm(intl.messages['modal.confirm.delete']).then(
      () => {
        const peer = ActorClient.getGroupPeer(gid);
        DialogActionCreators.deleteChat(peer);
      },
      () => {}
    );
  };

  onEditGroupClick = (gid) => EditGroupActionCreators.show(gid);

  handleAvatarClick = () => lightbox.open(this.props.group.bigAvatar);

  makeCall = () => {
    const { group } = this.props;
    CallActionCreators.makeCall(group.id);
  };

  render() {
    const { group } = this.props;
    const {
      isNotificationsEnabled,
      integrationToken,
      isMoreDropdownOpen,
      message
    } = this.state;
    const { intl } = this.context;

    const myId = UserStore.getMyId();
    const admin = UserStore.getUser(group.adminId);
    const isMember = DialogStore.isMember();

    const dropdownClassNames = classnames('dropdown', {
      'dropdown--opened': isMoreDropdownOpen
    });

    const iconElement = (
      <svg className="icon icon--green"
           dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#members"/>'}}/>
    );

    const groupMeta = [
      <header key={1}>
        <AvatarItem image={group.bigAvatar}
                    placeholder={group.placeholder}
                    size="large"
                    title={group.name}
                    onClick={this.handleAvatarClick}/>

        <h3 className="group_profile__meta__title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(group.name)}}/>
        <div className="group_profile__meta__created">
          {intl.messages['createdBy']}
          &nbsp;
          <span dangerouslySetInnerHTML={{__html: escapeWithEmoji(admin.name)}}/>
        </div>
      </header>
    ,
      group.about ? (
        <div className="group_profile__meta__description" key={2}
             dangerouslySetInnerHTML={{__html: escapeWithEmoji(group.about).replace(/\n/g, '<br/>')}}/>
      ) : null
    ];

    const token = (group.adminId === myId) ? (
      <li className="profile__list__item group_profile__integration no-p">
        <Fold icon="power" iconClassName="icon--pink" title={intl.messages['integrationToken']}>
          <div className="info info--light">
            <p>{intl.messages['integrationTokenHint']}</p>
            <a href="https://actor.readme.io/docs/simple-integration" target="_blank">{intl.messages['integrationTokenHelp']}</a>
          </div>
          <textarea className="textarea" onClick={this.selectToken} readOnly row="3" value={integrationToken}/>
        </Fold>
      </li>
    ) : null;

    if (isMember) {
      return (
        <div className="activity__body group_profile">
          <Scrollbar>
            <ul className="profile__list">
              <li className="profile__list__item group_profile__meta">
                {groupMeta}
                <footer className="row">
                  <div className="col-xs">
                    {
                      group.members.length < MAX_GROUP_CALL_SIZE
                        ? <button className="button button--green button--wide" onClick={this.makeCall}>
                            <i className="material-icons">phone</i>
                            {intl.messages['button.call']}
                          </button>
                        : <button className="button button--flat button--wide"
                                  onClick={() => this.onAddMemberClick(group)}>
                            <i className="material-icons">person_add</i>
                            {intl.messages['addPeople']}
                          </button>
                    }
                  </div>
                  <div style={{width: 10}}/>
                  <div className="col-xs">
                    <div className={dropdownClassNames}>
                      <button className="dropdown__button button button--flat button--wide"
                              onClick={this.toggleMoreDropdown}>
                        <i className="material-icons">more_horiz</i>
                        {intl.messages['more']}
                      </button>
                      <ul className="dropdown__menu dropdown__menu--right">
                        <li className="dropdown__menu__item" onClick={() => this.onEditGroupClick(group.id)}>
                          <i className="material-icons">mode_edit</i>
                          {intl.messages['editGroup']}
                        </li>
                        {
                          group.members.length < MAX_GROUP_CALL_SIZE
                            ? <li className="dropdown__menu__item" onClick={() => this.onAddMemberClick(group)}>
                                <i className="material-icons">person_add</i>
                                {intl.messages['addPeople']}
                              </li>
                            : null
                        }
                        <li className="dropdown__menu__item"
                            onClick={() => this.onLeaveGroupClick(group.id)}>
                          {intl.messages['leaveGroup']}
                        </li>
                        <li className="dropdown__menu__item"
                            onClick={() => this.onClearGroupClick(group.id)}>
                          {intl.messages['clearGroup']}
                        </li>
                        <li className="dropdown__menu__item"
                            onClick={() => this.onDeleteGroupClick(group.id)}>
                          {intl.messages['deleteGroup']}
                        </li>
                      </ul>
                    </div>
                  </div>
                </footer>
              </li>

              <li className="profile__list__item group_profile__media no-p hide">
                <Fold icon="attach_file" iconClassName="icon--gray" title={intl.messages['sharedMedia']}>
                  <ul>
                    <li><a>230 Shared Photos and Videos</a></li>
                    <li><a>49 Shared Links</a></li>
                    <li><a>49 Shared Files</a></li>
                  </ul>
                </Fold>
              </li>

              <li className="profile__list__item group_profile__notifications no-p">
                <label htmlFor="notifications">
                  <i className="material-icons icon icon--squash">notifications_none</i>
                  {intl.messages['notifications']}

                  <div className="switch pull-right">
                    <input checked={isNotificationsEnabled}
                           id="notifications"
                           onChange={this.onNotificationChange}
                           type="checkbox"/>
                    <label htmlFor="notifications"/>
                  </div>
                </label>
              </li>

              <li className="profile__list__item group_profile__members no-p">
                <Fold iconElement={iconElement}
                      title={message}>
                  <GroupProfileMembers groupId={group.id} members={group.members}/>
                </Fold>
              </li>

              {token}
            </ul>
          </Scrollbar>
        </div>
      );
    } else {
      return (
        <div className="activity__body group_profile">
          <ul className="profile__list">
            <li className="profile__list__item group_profile__meta">
              {groupMeta}
            </li>
          </ul>
        </div>
      );
    }

  }
}

export default Container.create(GroupProfile);
