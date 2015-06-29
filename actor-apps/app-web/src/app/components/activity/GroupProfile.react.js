import React from 'react';

import DialogActionCreators from '../../actions/DialogActionCreators';

import LoginStore from '../../stores/LoginStore';
import PeerStore from '../../stores/PeerStore';
import DialogStore from '../../stores/DialogStore';
import InviteUserActions from '../../actions/InviteUserActions';

import AvatarItem from '../common/AvatarItem.react';
import InviteUser from '../modals/InviteUser.react';
import GroupProfileMembers from './GroupProfileMembers.react';

const getStateFromStores = (groupId) => {
  const thisPeer = PeerStore.getGroupPeer(groupId);
  return {
    thisPeer: thisPeer,
    isNotificationsEnabled: DialogStore.isNotificationsEnabled(thisPeer)
  };
};

class GroupProfile extends React.Component {
  static propTypes = {
    group: React.PropTypes.object.isRequired
  };

  componentWillMount() {
    DialogStore.addNotificationsListener(this.whenNotificationChanged);
  }

  componentWillUnmount() {
    DialogStore.removeNotificationsListener(this.whenNotificationChanged);
  }

  constructor(props) {
    super(props);

    this.onNotificationChange = this.onNotificationChange.bind(this);

    this.state = getStateFromStores(this.props.group.id);
  }

  onAddMemberClick(group) {
    InviteUserActions.modalOpen(group);
  }

  onLeaveGroupClick(groupId) {
    DialogActionCreators.leaveGroup(groupId);
  }

  onNotificationChange(event) {
    DialogActionCreators.changeNotificationsEnabled(this.state.thisPeer, event.target.checked);
  }

  whenNotificationChanged = () => {
    this.setState(getStateFromStores(this.props.group.id));
  };

  render() {
    const group = this.props.group;
    const myId = LoginStore.getMyId();
    const isNotificationsEnabled = this.state.isNotificationsEnabled;

    let memberArea;
    let adminControls;

    if (group.adminId === myId) {
      adminControls = (
        <li className="profile__list__item">
          <a className="red">Delete group</a>
        </li>
      );
    }

    if (DialogStore.isGroupMember(group)) {
      memberArea = (
        <div>
          <div className="notifications">
            <label htmlFor="notifications">Enable Notifications</label>

            <div className="switch pull-right">
              <input checked={isNotificationsEnabled} id="notifications" onChange={this.onNotificationChange.bind(this)} type="checkbox"/>
              <label htmlFor="notifications"></label>
            </div>
          </div>

          <GroupProfileMembers groupId={group.id} members={group.members}/>

          <ul className="profile__list profile__list--controls">
            <li className="profile__list__item">
              <a className="" onClick={this.onAddMemberClick.bind(this, group)}>Add member</a>
            </li>
            <li className="profile__list__item">
              <a className="red" onClick={this.onLeaveGroupClick.bind(this, group.id)}>Leave group</a>
            </li>
              {adminControls}
          </ul>
          {/*
          <footer className="profile__controls">
            <a className="button button--wide" onClick={this.onAddMemberClick.bind(this, group)}>Add member</a>
            <a className="button button--wide" onClick={this.onLeaveGroupClick.bind(this, group.id)}>Leave group</a>

          </footer>
           */}

          <InviteUser/>
        </div>
      );
    }

    return (
      <div className="activity__body profile">
        <AvatarItem image={group.bigAvatar}
                    placeholder={group.placeholder}
                    size="huge"
                    title={group.name}/>

        <h3 className="profile__name">{group.name}</h3>

        {memberArea}
      </div>
    );
  }
}

export default GroupProfile;
