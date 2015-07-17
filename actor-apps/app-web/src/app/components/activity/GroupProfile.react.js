import React from 'react';

import DialogActionCreators from 'actions/DialogActionCreators';

import LoginStore from 'stores/LoginStore';
import PeerStore from 'stores/PeerStore';
import DialogStore from 'stores/DialogStore';
import GroupStore from 'stores/GroupStore';
import InviteUserActions from 'actions/InviteUserActions';

import AvatarItem from 'components/common/AvatarItem.react';
import InviteUser from 'components/modals/InviteUser.react';
import GroupProfileMembers from 'components/activity/GroupProfileMembers.react';

const getStateFromStores = (groupId) => {
  const thisPeer = PeerStore.getGroupPeer(groupId);
  return {
    thisPeer: thisPeer,
    isNotificationsEnabled: DialogStore.isNotificationsEnabled(thisPeer),
    integrationToken: GroupStore.getIntegrationToken()
  };
};

class GroupProfile extends React.Component {
  static propTypes = {
    group: React.PropTypes.object.isRequired
  };

  componentWillUnmount() {
    DialogStore.removeNotificationsListener(this.onChange);
    GroupStore.addChangeListener(this.onChange);
  }

  componentWillReceiveProps(newProps) {
    this.setState(getStateFromStores(newProps.group.id));
  }

  constructor(props) {
    super(props);

    DialogStore.addNotificationsListener(this.onChange);
    GroupStore.addChangeListener(this.onChange);

    this.state = getStateFromStores(props.group.id);
  }

  onAddMemberClick = group => {
    InviteUserActions.modalOpen(group);
  }

  onLeaveGroupClick = groupId => {
    DialogActionCreators.leaveGroup(groupId);
  }

  onNotificationChange = event => {
    DialogActionCreators.changeNotificationsEnabled(this.state.thisPeer, event.target.checked);
  }

  onChange = () => {
    this.setState(getStateFromStores(this.props.group.id));
  };

  render() {
    const group = this.props.group;
    const myId = LoginStore.getMyId();
    const isNotificationsEnabled = this.state.isNotificationsEnabled;
    const integrationToken = this.state.integrationToken;

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
              <a className="link__blue" onClick={this.onAddMemberClick.bind(this, group)}>Add member</a>
            </li>
            <li className="profile__list__item">
              <a className="link__red" onClick={this.onLeaveGroupClick.bind(this, group.id)}>Leave group</a>
            </li>
              {adminControls}
          </ul>

          <InviteUser/>
        </div>
      );
    }

    return (
      <div className="activity__body profile">
        <div className="profile__name">
          <AvatarItem image={group.bigAvatar}
                      placeholder={group.placeholder}
                      size="medium"
                      title={group.name}/>
          <h3>{group.name}</h3>

        </div>

        {memberArea}

        {integrationToken}
      </div>
    );
  }
}

export default GroupProfile;
